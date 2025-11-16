package codeqr.code.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "semaine_emploi_temps",
       uniqueConstraints = @UniqueConstraint(name = "uq_semaine_user_date_debut", columnNames = {"created_by_id", "date_debut"}),
       indexes = {@Index(name = "idx_semaine_user_createdat", columnList = "created_by_id, created_at DESC")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemaineEmploiTemps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut; // doit être Lundi

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;   // = dateDebut + 6

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private Responsable createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

      @Column(name = "updated_at")
    private OffsetDateTime updatedAt = OffsetDateTime.now();
  

    @OneToMany(mappedBy = "semaine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmploiTemps> emplois = new ArrayList<>();

    /* helper */
    public void addEmploi(EmploiTemps emploi) {
        emploi.setSemaine(this);
        this.emplois.add(emploi);
    }

    public void removeEmploi(EmploiTemps emploi) {
        emploi.setSemaine(null);
        this.emplois.remove(emploi);
    }
}
