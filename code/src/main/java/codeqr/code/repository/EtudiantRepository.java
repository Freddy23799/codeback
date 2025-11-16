package codeqr.code.repository;

import codeqr.code.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtudiantRepository extends JpaRepository<Student, Long> { }