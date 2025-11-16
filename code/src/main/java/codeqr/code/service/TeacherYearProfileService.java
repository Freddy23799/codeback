package codeqr.code.service;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codeqr.code.model.TeacherYearProfile;
import codeqr.code.repository.TeacherYearProfileRepository;


// --------------- TeacherYearProfile Service -----------------
@Service
@Transactional
public class TeacherYearProfileService {
    private final TeacherYearProfileRepository repo;

    public TeacherYearProfileService(TeacherYearProfileRepository repo) { this.repo = repo; }

    public List<TeacherYearProfile> findAll() { return repo.findAll(); }
    public Optional<TeacherYearProfile> findById(Long id) { return repo.findById(id); }
    public List<TeacherYearProfile> findByTeacherId(Long teacherId) { return repo.findByTeacherId(teacherId); }
    public Optional<TeacherYearProfile> findByTeacherIdAndAcademicYearId(Long teacherId, Long academicYearId) {
        return repo.findByTeacherIdAndAcademicYearId(teacherId, academicYearId);
    }
    public TeacherYearProfile save(TeacherYearProfile entity) { return repo.save(entity); }
    public void delete(Long id) { repo.deleteById(id); }
}