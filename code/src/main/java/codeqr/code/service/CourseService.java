package codeqr.code.service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import codeqr.code.dto.CourseCreateDto;
import codeqr.code.dto.CourseDto;
import codeqr.code.model.Course;
import codeqr.code.repository.CourseRepository;




// --------------- Course Service -----------------
@Service
@Transactional
public class CourseService {
    private final CourseRepository repo;

    public CourseService(CourseRepository repo) { this.repo = repo; }
// public List<Course>  findByTeacherLevelSpecialty(){return repo.findByTeacherLevelSpecialty(findByTeacherLevelSpecialty(teacherId, levelId, specialtyId))};
    // public List<Course> findAll() { return repo.findAll(); }
    public Optional<Course> findById(Long id) { return repo.findById(id); }
    public Optional<Course> findByCode(String code) { return repo.findByCode(code); }
    // public List<Course> findByLevelId(Long levelId) { return repo.findByLevelId(levelId); }
    // public List<Course> findBySpecialtyId(Long specialtyId) { return repo.findBySpecialtyId(specialtyId); }
    public Course save(Course entity) { return repo.save(entity); }
    // public void delete(Long id) { repo.deleteById(id); }
    public Object findByTeacherLevelSpecialty(Long teacherId, Long levelId, Long specialtyId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByTeacherLevelSpecialty'");
    }



    @Transactional(readOnly = true)
     @Cacheable(cacheNames = "courses", key = "'all'")
    public List<CourseDto> listAll() {
        return repo.findAll().stream().map(c -> new CourseDto(c.getId(), c.getCode(), c.getTitle())).collect(Collectors.toList());
    }

    @Transactional
    public CourseDto create(CourseCreateDto in) {
        Course c = new Course();
        c.setCode(in.getCode());
        c.setTitle(in.getTitle());
        Course saved = repo.save(c);
        return new CourseDto(saved.getId(), saved.getCode(), saved.getTitle());
    }

    @Transactional
    public CourseDto update(Long id, CourseCreateDto in) {
        Course c = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        c.setCode(in.getCode());
        c.setTitle(in.getTitle());
        Course s = repo.save(c);
        return new CourseDto(s.getId(), s.getCode(), s.getTitle());
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        repo.deleteById(id);
    }


































    //  // -----------------------------
    // // 1) Trouver par ID
    // // -----------------------------
    // public Optional<Course> findById(Long id) {
    //     return repository.findById(id);
    // }

    // // -----------------------------
    // // 2) Lister tous les cours
    // // -----------------------------
    // public List<CourseDto> listAll() {
    //     return repository.findAll().stream()
    //             .map(course -> new CourseDto(
    //                     course.getId(),
    //                     course.getCode(),
    //                     course.getTitle()
    //             ))
    //             .collect(Collectors.toList());
    // }

    // // -----------------------------
    // // 3) Créer un nouveau cours
    // // -----------------------------
    // public CourseDto create(CourseCreateDto in) {
    //     Course course = new Course();
    //     course.setCode(in.getCode());
    //     course.setTitle(in.getTitle());

    //     Course saved = repository.save(course);
    //     return new CourseDto(saved.getId(), saved.getCode(), saved.getTitle());
    // }

    // // -----------------------------
    // // 4) Mettre à jour un cours existant
    // // -----------------------------
    // public CourseDto update(Long id, CourseCreateDto in) {
    //     Course course = repository.findById(id)
    //             .orElseThrow(() -> new RuntimeException("Course not found with id " + id));

    //     course.setCode(in.getCode());
    //     course.setTitle(in.getTitle());

    //     Course updated = repository.save(course);
    //     return new CourseDto(updated.getId(), updated.getCode(), updated.getTitle());
    // }

    // // -----------------------------
    // // 5) Supprimer un cours
    // // -----------------------------
    // public void delete(Long id) {
    //     if (!repository.existsById(id)) {
    //         throw new RuntimeException("Course not found with id " + id);
    //     }
    //     repository.deleteById(id);
    // }



}