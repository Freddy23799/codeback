package codeqr.code.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import codeqr.code.model.StudentYearProfile;
import codeqr.code.repository.StudentYearProfileRepository;




// --------------- StudentYearProfile Service -----------------
@Service
@Transactional
public class StudentYearProfileService {
    private final StudentYearProfileRepository repo;

    public StudentYearProfileService(StudentYearProfileRepository repo) { this.repo = repo; }

    public List<StudentYearProfile> findAll() { return repo.findAll(); }
    public Optional<StudentYearProfile> findById(Long id) { return repo.findById(id); }
    public List<StudentYearProfile> findByStudentId(Long studentId) { return repo.findByStudentId(studentId); }
    public Optional<StudentYearProfile> findByStudentIdAndAcademicYearId(Long studentId, Long academicYearId) {
        return repo.findByStudentIdAndAcademicYearId(studentId, academicYearId);
    }
    public StudentYearProfile save(StudentYearProfile entity) { return repo.save(entity); }
    public void delete(Long id) { repo.deleteById(id); }


      public List<StudentYearProfile> findByLevelIdAndSpecialtyId(Long levelId, Long specialtyId) {
        return repo.findByLevelIdAndSpecialtyId(levelId, specialtyId);
    }
}