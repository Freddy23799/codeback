package codeqr.code.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import codeqr.code.model.AcademicYear;
import codeqr.code.model.Course;
import codeqr.code.model.Level;
import codeqr.code.model.Specialty;
import codeqr.code.repository.AcademicYearRepository;
import codeqr.code.repository.CourseRepository;
import codeqr.code.repository.LevelRepository;
import codeqr.code.repository.SpecialtyRepository;

/**
 * Contrôleur "tout-en-un" exposant des listes légères / recherches
 * pour le front Quasar : disciplines (anciennement specialties),
 * schoolYears (anciennement academicYears), courses, levels.
 *
 * DTOs légers définis comme classes internes pour tenir dans un seul fichier.
 */
@RestController
@RequestMapping("/api")
public class ReferenceDataController {

    private final SpecialtyRepository specialtyRepo;
    private final CourseRepository courseRepo;
    private final LevelRepository levelRepo;
    private final AcademicYearRepository yearRepo;

    public ReferenceDataController(SpecialtyRepository specialtyRepo,
                                   CourseRepository courseRepo,
                                   LevelRepository levelRepo,
                                   AcademicYearRepository yearRepo) {
        this.specialtyRepo = specialtyRepo;
        this.courseRepo = courseRepo;
        this.levelRepo = levelRepo;
        this.yearRepo = yearRepo;
    }

    // ---------------- admin endpoints (listes simples) ----------------

    /**
     * GET /admin/schoolYears
     * Retourne une liste simple [{id,label}, ...]
     */
    @GetMapping("/schoolYears")
    public List<SchoolYearDto> listSchoolYears() {
        return yearRepo.findAll().stream()
                .map(y -> new SchoolYearDto(y.getId(), y.getLabel()))
                .collect(Collectors.toList());
    }

    /**
     * GET /admin/levels
     * Retourne une liste simple [{id,name}, ...]
     */
    @GetMapping("/levels")
    public List<LevelDto> listLevels() {
        return levelRepo.findAll().stream()
                .map(l -> new LevelDto(l.getId(), l.getName()))
                .collect(Collectors.toList());
    }

    /**
     * GET /admin/disciplines
     * Retourne un listing (non paginé) utilisable pour selects (size param optional)
     */
    @GetMapping("/disciplines")
    public List<DisciplineDto> listDisciplines(@RequestParam Optional<Integer> size) {
        int s = size.orElse(200);
        Page<Specialty> p = specialtyRepo.findAll(PageRequest.of(0, s));
        return p.getContent().stream()
                .map(sp -> new DisciplineDto(sp.getId(), sp.getName()))
                .collect(Collectors.toList());
    }

    // ---------------- api endpoints (recherches paginées) ----------------

    /**
     * GET /api/disciplines/search?q=...&size=50
     * Renvoie Page<{id,name}>
     * ATTENTION: repository doit exposer findByNameContainingIgnoreCase
     */
    @GetMapping("/disciplines/search")
    public Page<DisciplineDto> searchDisciplines(
            @RequestParam Optional<String> q,
            @RequestParam Optional<Integer> size) {

        String query = q.orElse("");
        int s = size.orElse(50);
        Page<Specialty> page = specialtyRepo.findByNameContainingIgnoreCase(query, PageRequest.of(0, s));
        List<DisciplineDto> dtos = page.getContent().stream()
                .map(sp -> new DisciplineDto(sp.getId(), sp.getName()))
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    /**
     * GET /api/courses/search?q=...&size=20
     * Renvoie Page<{id,title,code}>
     * ATTENTION: repository doit exposer findByTitleContainingIgnoreCaseOrCodeContainingIgnoreCase
     */
    @GetMapping("/courses/search")
    public Page<CourseDto> searchCourses(
            @RequestParam Optional<String> q,
            @RequestParam Optional<Integer> size) {

        String query = q.orElse("");
        int s = size.orElse(20);
        Page<Course> page = courseRepo.findByTitleContainingIgnoreCaseOrCodeContainingIgnoreCase(query, query, PageRequest.of(0, s));
        List<CourseDto> dtos = page.getContent().stream()
                .map(c -> new CourseDto(c.getId(), c.getTitle(), c.getCode()))
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    // ------------------ DTOs internes ------------------

    public static record SchoolYearDto(Long id, String label) { }
    public static record DisciplineDto(Long id, String name) { }
    public static record CourseDto(Long id, String title, String code) { }
    public static record LevelDto(Long id, String name) { }
}
