package codeqr.code.repository;

import codeqr.code.model.SemaineEmploiTemps;
import codeqr.code.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SemaineEmploiTempsRepository extends JpaRepository<SemaineEmploiTemps, Long> {
    Optional<SemaineEmploiTemps> findTopByCreatedByOrderByDateDebutDesc(User createdBy);
    List<SemaineEmploiTemps> findByCreatedByOrderByDateDebutDesc(User createdBy, Pageable pageable);
    Optional<SemaineEmploiTemps> findByCreatedBy_IdAndDateDebut(Long userId, LocalDate dateDebut);
    Optional<SemaineEmploiTemps> findTopByCreatedBy_IdOrderByDateDebutDesc(Long userId);

}
