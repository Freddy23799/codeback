package codeqr.code.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import java.util.List;
import java.util.Optional;
import codeqr.code.model.Campus;


// --- Campus Repository ---
@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {
    Optional<Campus> findByName(String name);
   
}
