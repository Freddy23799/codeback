package codeqr.code.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "emploi_temps",
       uniqueConstraints = @UniqueConstraint(name = "uq_emploi_unique_per_week",
               columnNames = {"semaine_id", "specialite_id", "niveau_id", "annee_academique_id"}),
       indexes = {@Index(name = "idx_emploi_semaine", columnList = "semaine_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmploiTemps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // parent semaine
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semaine_id", nullable = false)
    private SemaineEmploiTemps semaine;

    @Column(name = "specialite_id", nullable = false)
    private Long specialiteId;

    @Column(name = "niveau_id", nullable = false)
    private Long niveauId;

    @Column(name = "annee_academique_id", nullable = false)
    private Long anneeAcademiqueId;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "DRAFT";

    // copie du créateur pour requêtes rapides (même user que semaine.createdBy)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private Responsable createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "title")
    private String title;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "emploiTemps", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneEmploiTemps> lignes = new ArrayList<>();

    /* helpers */
    public void addLigne(LigneEmploiTemps ligne) {
        ligne.setEmploiTemps(this);
        this.lignes.add(ligne);
    }

    public void removeLigne(LigneEmploiTemps ligne) {
        ligne.setEmploiTemps(null);
        this.lignes.remove(ligne);
    }
}
