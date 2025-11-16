package codeqr.code.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// import java.util.List;
import java.util.*;

import codeqr.code.model.AcademicYear;
// import codeqr.code.model.Campus;
// import codeqr.code.model.Room;
// import codeqr.code.model.Specialty;

// --- AcademicYear Repository ---
@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
  
 Optional<AcademicYear> findByLabel(String label);
    Optional<AcademicYear> findByActiveTrue();
    List<AcademicYear> findAllByIdIn(Collection<Long> ids);
}

