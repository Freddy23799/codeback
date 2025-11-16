package codeqr.code.repository;

import codeqr.code.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveillantSessionRepository extends JpaRepository<Session, Long>, SurveillantSessionRepositoryCustom {
    // autres méthodes Spring Data si tu en as
}
