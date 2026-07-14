package com.paw.adoptions.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "adoption_application_answers", schema = "adoptions")
public class AdoptionApplicationAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private AdoptionApplication application;

    @Column(name = "field_key", nullable = false, length = 50)
    private String fieldKey;

    @Column(name = "label_snapshot", nullable = false, length = 160)
    private String labelSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_snapshot", nullable = false, length = 30)
    private FormFieldType typeSnapshot;

    @Column(name = "answer_value", nullable = false, length = 4000)
    private String answerValue;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
