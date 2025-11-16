

package codeqr.code.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// import java.util.Optional;

import codeqr.code.dto.CourseCreateDto;
import codeqr.code.dto.CourseDto;

// import java.util.logging.Course;

import codeqr.code.model.Course;
import codeqr.code.repository.CourseRepository;
import codeqr.code.service.CourseService;
// --------------- Course Controller -----------------
@RestController
@RequestMapping("/api/admin/courses")
public class CourseController {

    private final CourseService service;
    private final CourseRepository servicerepo;
    
   

    public CourseController(CourseService service, CourseRepository servicerepo) {
        this.service = service;
        this.servicerepo = servicerepo;
    }

//    @GetMapping("/prof/courses")
// public List<Course> getCourses(@RequestParam Long teacherId, @RequestParam Long levelId, @RequestParam Long specialtyId) {
//     return servicerepo.findByTeacherAndLevelAndSpecialty(teacherId, levelId, specialtyId);
// }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

   @GetMapping
    public List<CourseDto> list() { return service.listAll(); }

    @PostMapping
    public CourseDto create(@RequestBody CourseCreateDto in) { return service.create(in); }

    @PutMapping("/{id}")
    public CourseDto update(@PathVariable Long id, @RequestBody CourseCreateDto in) { return service.update(id, in); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }

//     @GetMapping("/filter")
//     public ResponseEntity<List<Course>> filterCourses(
//             @RequestParam Long teacherId,
//             @RequestParam Long levelId,
//             @RequestParam Long specialtyId
//     ) {
//         return ResponseEntity.ok(servicerepo.findByTeacherYearProfile_Teacher_IdAndTeacherYearProfile_Level_IdAndTeacherYearProfile_Specialty_Id(teacherId, levelId, specialtyId));
//     }
}
