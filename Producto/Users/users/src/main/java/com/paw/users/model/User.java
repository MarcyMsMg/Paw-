package com.paw.users.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.paw.users.enums.AccountStatus;
import com.paw.users.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "first_name", length = 80)
    private String firstName;

    @Column(name = "last_name", length = 80)
    private String lastName;

    @Column(name = "ngo_name", length = 150)
    private String ngoName;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "foundation_year")
    private Integer foundationYear;

    @Column(name = "rescued_animals_count")
    private Integer rescuedAnimalsCount;

    @Column(name = "volunteers_count")
    private Integer volunteersCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = AccountStatus.ACTIVE;
        }
    }
}
