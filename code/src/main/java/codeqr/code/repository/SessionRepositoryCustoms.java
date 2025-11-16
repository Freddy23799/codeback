


package codeqr.code.repository;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codeqr.code.model.Session;

public interface SessionRepositoryCustoms {

    /**
     * Recherche paginée de sessions par utilisateur, texte libre et date.
     *
     * @param userId  l'id de l'utilisateur (nullable)
     * @param search  texte de recherche sur cours, salle, campus, niveau ou spécialité (nullable)
     * @param dateIso date de début de session (nullable)
     * @param pageable pagination et tri
     * @return page de sessions
     */
     Page<Session> findByUserIdWithSearch(Long userId, String search, LocalDate dateIso, Pageable pageable);
}
