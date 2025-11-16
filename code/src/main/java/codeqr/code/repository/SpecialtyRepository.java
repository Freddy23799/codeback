package codeqr.code.repository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import codeqr.code.model.Specialty;

// import java.util.logging.Specialt;
// import java.util.logging.Level;
import java.util.*;


// --- Level Repository ---
@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
List<Specialty>findByDepartmentId(Long DepartmentId);
    Optional<Specialty> findByNameAndDepartmentId(String name, Long DepartmentId);
    @Override
    @EntityGraph(attributePaths={"department"})

    List<Specialty>findAll();
 Page<Specialty> findByNameContainingIgnoreCase(String name, Pageable pageable);



    List<Specialty> findAllByIdIn(Collection<Long> ids);

}




