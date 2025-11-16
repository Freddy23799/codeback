package codeqr.code.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// import java.util.List;
import java.util.Optional;
import codeqr.code.model.*;


// --- Campus Repository ---
@Repository
public interface SurveillantRepository extends JpaRepository<Surveillant, Long> , SurveillantRepositoryCustom {

    Page<Surveillant> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUser_UsernameContainingIgnoreCaseOrMatriculeContainingIgnoreCase(
            String fullName, String email, String username, String matricule, Pageable pageable);
 Optional<Surveillant> findById(Long id);

  Optional<Surveillant> findByUserId(Long id);



   @Query("SELECT s FROM Surveillant s " +
           "JOIN FETCH s.sexe sx " +
           "JOIN FETCH s.user u " +
           "WHERE s.user.id = :userId")
    Optional<Surveillant> findByUserIdWithSexe(@Param("userId") Long userId);
}
