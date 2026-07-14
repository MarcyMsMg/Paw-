package com.paw.adoptions.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "adoption_form_templates",
        schema = "adoptions",
        indexes = @Index(name = "idx_form_templates_ngo", columnList = "ngo_id")
)
public class AdoptionFormTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ngo_id", nullable = false)
    private UUID ngoId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private int revision = 1;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<AdoptionFormField> fields = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long entityVersion;

    public void replaceFields(List<AdoptionFormField> replacement) {
        while (fields.size() > replacement.size()) {
            fields.removeLast();
        }
        for (int index = 0; index < replacement.size(); index++) {
            AdoptionFormField source = replacement.get(index);
            if (index >= fields.size()) {
                source.setTemplate(this);
                fields.add(source);
                continue;
            }
            AdoptionFormField target = fields.get(index);
            target.setFieldKey(source.getFieldKey());
            target.setLabel(source.getLabel());
            target.setType(source.getType());
            target.setRequired(source.isRequired());
            target.setPlaceholder(source.getPlaceholder());
            target.setDisplayOrder(source.getDisplayOrder());
            target.getOptions().clear();
            target.getOptions().addAll(source.getOptions());
        }
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
