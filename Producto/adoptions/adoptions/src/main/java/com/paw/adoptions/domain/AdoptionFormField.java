package com.paw.adoptions.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "adoption_form_fields",
        schema = "adoptions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_form_field_key",
                columnNames = {"template_id", "field_key"}
        )
)
public class AdoptionFormField {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private AdoptionFormTemplate template;

    @Column(name = "field_key", nullable = false, length = 50)
    private String fieldKey;

    @Column(nullable = false, length = 160)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FormFieldType type;

    @Column(nullable = false)
    private boolean required;

    @Column(length = 200)
    private String placeholder;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "adoption_form_field_options",
            schema = "adoptions",
            joinColumns = @JoinColumn(name = "field_id")
    )
    @Column(name = "option_value", nullable = false, length = 160)
    @OrderColumn(name = "position")
    private List<String> options = new ArrayList<>();
}
