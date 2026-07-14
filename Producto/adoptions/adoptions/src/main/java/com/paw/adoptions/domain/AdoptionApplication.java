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
        name = "adoption_applications",
        schema = "adoptions",
        indexes = {
                @Index(name = "idx_applications_animal", columnList = "animal_id"),
                @Index(name = "idx_applications_ngo", columnList = "ngo_id"),
                @Index(name = "idx_applications_person", columnList = "person_id"),
                @Index(name = "idx_applications_status", columnList = "status")
        }
)
public class AdoptionApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "animal_id", nullable = false)
    private UUID animalId;

    @Column(name = "ngo_id", nullable = false)
    private UUID ngoId;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(nullable = false, length = 220)
    private String address;

    @Column(name = "housing_type", nullable = false, length = 120)
    private String housingType;

    @Column(name = "other_animals", nullable = false, length = 120)
    private String otherAnimals;

    @Column(nullable = false, length = 2000)
    private String motivation;

    @Column(length = 500)
    private String availability;

    @Column(name = "previous_experience", length = 1000)
    private String previousExperience;

    @Column(name = "ngo_response", length = 1000)
    private String ngoResponse;

    @Column(name = "form_template_id")
    private UUID formTemplateId;

    @Column(name = "form_template_revision")
    private Integer formTemplateRevision;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<AdoptionApplicationAnswer> customAnswers = new ArrayList<>();

    public void addCustomAnswer(AdoptionApplicationAnswer answer) {
        answer.setApplication(this);
        customAnswers.add(answer);
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AdoptionApplicationStatus status = AdoptionApplicationStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
