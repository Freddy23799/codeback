package codeqr.code.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.*;
import codeqr.code.model.Level;




// --- Level Repository ---
@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    Optional<Level> findByName(String name);
     List<Level> findAllByIdIn(Collection<Long> ids);
}