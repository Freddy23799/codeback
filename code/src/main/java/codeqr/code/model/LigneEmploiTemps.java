package codeqr.code.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ligne_emploi_temps",
       indexes = {@Index(name = "idx_ligne_emploi", columnList = "emploi_temps_id, jour, heure_debut")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneEmploiTemps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emploi_temps_id", nullable = false)
    private EmploiTemps emploiTemps;

    @Enumerated(EnumType.STRING)
    @Column(name = "jour", nullable = false, length = 16)
    private JourSemaine jour;

    @Column(name = "plage_horaire_id")
    private Long plageHoraireId; // optionnel : FK vers plages_horaires (si tu veux entité, change en @ManyToOne)

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @Column(name = "cours_id")
    private Long coursId;

    @Column(name = "professeur_id")
    private Long professeurId;

    @Column(name = "salle_id")
    private Long salleId;

    @Column(name = "ordre_small")
    private Integer ordreSmall = 0;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
