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
        name = "animals",
        schema = "adoptions",
        indexes = {
                @Index(name = "idx_animals_ngo", columnList = "ngo_id"),
                @Index(name = "idx_animals_status", columnList = "status"),
                @Index(name = "idx_animals_published", columnList = "published")
        }
)
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ngo_id", nullable = false)
    private UUID ngoId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String species;

    @Column(nullable = false, length = 80)
    private String age;

    @Column(nullable = false, length = 40)
    private String sex;

    @Column(nullable = false, length = 40)
    private String size;

    @Column(length = 160)
    private String location;

    @Column(name = "health_status", length = 500)
    private String healthStatus;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "adoption_requirements", length = 1000)
    private String adoptionRequirements;

    @Column(name = "form_template_id")
    private UUID formTemplateId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "animal_photos",
            schema = "adoptions",
            joinColumns = @JoinColumn(name = "animal_id")
    )
    @Column(name = "url", nullable = false, length = 1000)
    @OrderColumn(name = "position")
    private List<String> photoUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AnimalStatus status = AnimalStatus.AVAILABLE;

    @Column(nullable = false)
    private boolean published = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

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
