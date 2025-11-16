




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
public interface ResponsableRepository extends JpaRepository<Responsable, Long> ,ResponsableRepositoryCustom {

    Page<Responsable> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUser_UsernameContainingIgnoreCaseOrMatriculeContainingIgnoreCase(
            String fullName, String email, String username, String matricule, Pageable pageable);
 Optional<Responsable> findById(Long id);

  Optional<Responsable> findByUserId(Long id);

Optional<Responsable> findByUser_Id(Long userId);

   @Query("SELECT s FROM Responsable s " +
           "JOIN FETCH s.sexe sx " +
           "JOIN FETCH s.user u " +
           "WHERE s.user.id = :userId")
    Optional<Responsable> findByUserIdWithSexe(@Param("userId") Long userId);
}
