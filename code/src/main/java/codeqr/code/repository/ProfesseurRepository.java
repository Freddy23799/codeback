package codeqr.code.repository;

import codeqr.code.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfesseurRepository extends JpaRepository<Teacher, Long> { }