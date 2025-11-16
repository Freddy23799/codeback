package codeqr.code.repository;

import codeqr.code.model.LigneEmploiTemps;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LigneEmploiTempsRepository extends JpaRepository<LigneEmploiTemps, Long> {

    /**
     * Récupère les ids des semaines (distinct) où ce professeur intervient,
     * triés par date_debut de la semaine (desc). Pageable permet limit(2).
     */
    @Query("SELECT s.id FROM LigneEmploiTemps l " +
           "JOIN l.emploiTemps e JOIN e.semaine s " +
           "WHERE l.professeurId = :profId " +
           "GROUP BY s.id, s.dateDebut " +
           "ORDER BY s.dateDebut DESC")
    List<Long> findDistinctWeekIdsForProfessor(@Param("profId") Long profId, Pageable pageable);

    /**
     * Récupère les lignes pour le professeur pour un ensemble de semaines.
     * EntityGraph permet de charger emploiTemps (+ semaine) en une seule requête.
     */
    @EntityGraph(attributePaths = {"emploiTemps", "emploiTemps.semaine", "emploiTemps.createdBy"})
    List<LigneEmploiTemps> findByProfesseurIdAndEmploiTempsSemaineIdIn(
            @Param("professeurId") Long professeurId,
            @Param("semaineIds") Iterable<Long> semaineIds
    );
}
