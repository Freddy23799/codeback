package codeqr.code.repository;

import codeqr.code.model.EmploiTemps;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.List;

public interface EmploiTempsRepository extends JpaRepository<EmploiTemps, Long> {
    List<EmploiTemps> findBySemaineId(Long semaineId);




   /**
     * IMPORTANT : BETWEEN monday..sunday pour récupérer TOUTES les semaines de la semaine courante.
     * EntityGraph précharge 'lignes' et 'semaine' pour éviter N+1.
     */
    @EntityGraph(attributePaths = {"lignes", "semaine"})
    List<EmploiTemps> findByCreatedBy_IdAndSemaine_DateDebutBetweenOrderBySemaine_DateDebutDesc(Long createdById, LocalDate start, LocalDate end);

    long countByCreatedBy_Id(Long createdById);


@EntityGraph(attributePaths = {"lignes", "semaine"})
List<EmploiTemps> findBySemaine_IdOrderByIdDesc(Long semaineId);

   @EntityGraph(attributePaths = {"lignes", "semaine"})
    List<EmploiTemps> findByAnneeAcademiqueIdAndNiveauIdAndSpecialiteIdOrderByCreatedAtDesc(
            Long anneeAcademiqueId, Long niveauId, Long specialiteId);

    long countByCreatedBy_IdAndSemaine_DateDebutBetween(Long createdById, LocalDate start, LocalDate end);
}
