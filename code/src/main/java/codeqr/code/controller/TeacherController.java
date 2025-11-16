




package codeqr.code.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import codeqr.code.model.Course;
// import codeqr.code.dto2.TeacherDto;
// import org.springframework.security.core.Authentication;
import codeqr.code.model.Teacher;
import codeqr.code.model.User;
import codeqr.code.model.TeacherYearProfile;
import codeqr.code.repository.UserRepository;
import codeqr.code.repository.TeacherRepository;
import codeqr.code.service.*;
import lombok.*;
import codeqr.code.dto.*;
import codeqr.code.PdfUtil.PdfUtil; // Add this import if PdfUtil exists in util package

@RestController
@RequestMapping("/api")
 @PreAuthorize("hasRole('ADMIN','PROFESSEUR')")


 @Data
@RequiredArgsConstructor
public class TeacherController {
 private final TeacherService teacherService;
 private final SessionService sessionService;

private final UserRepository userRepository;


private final TeacherRepository teacherRepository;








  private final StatsService statsService;

    // Search (server-side keyset)
    @GetMapping("/teachers/search")
    public ResponseEntity<TeacherSearchResultDto> searchTeachers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "80") int limit,
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) Long levelId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long courseId
    ) {
        TeacherSearchResultDto res = teacherService.searchTeachers(q, cursor, limit, specialtyId, levelId, academicYearId, courseId);
        return ResponseEntity.ok(res);
    }








@GetMapping("/{id}/cour")
public ResponseEntity<List<Course>> getCoursesByProfessor(@PathVariable Long id) {
    Teacher teacher = teacherRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professeur non trouvé"));
    return ResponseEntity.ok(teacher.getCourses());
}





     @GetMapping("/teacherss")
    public ResponseEntity<List<Course>> getCoursesForCurrentTeacher(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        Teacher teacher = user.getTeacher();
        if (teacher == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<Course> courses = teacher.getCourses();
        return ResponseEntity.ok(courses);
    }
    // Sessions keyset
    @GetMapping("/teacher/{teacherId}/sessions")
    public ResponseEntity<?> getSessions(@PathVariable Long teacherId,
                                         @RequestParam(required = false) Long cursor,
                                         @RequestParam(defaultValue = "80") int limit) {
        return ResponseEntity.ok(teacherService.getSessionsForTeacher(teacherId, cursor, limit));
    }

    // Course stats
    @GetMapping("/teacher/{teacherId}/course-stats")
    public ResponseEntity<CourseStatsDto> getCourseStats(@PathVariable Long teacherId,
                                                         @RequestParam(required = false) Long specialtyId,
                                                         @RequestParam(required = false) Long levelId,
                                                         @RequestParam(required = false) Long academicYearId,
                                                         @RequestParam(required = false) Long courseId) {
        CourseStatsDto dto = statsService.computeCourseStats(teacherId, specialtyId, levelId, academicYearId, courseId);
        return ResponseEntity.ok(dto);
    }

    // PDF endpoint server-side (optional): stream PDF bytes
    @GetMapping("/teacher/{teacherId}/report.pdf")
    public ResponseEntity<byte[]> getTeacherPdf(@PathVariable Long teacherId,
                                               @RequestParam(required = false) Long specialtyId,
                                               @RequestParam(required = false) Long levelId,
                                               @RequestParam(required = false) Long academicYearId,
                                               @RequestParam(required = false) Long courseId) {
        // For brevity -> reuse StatsService.computeCourseStats and build a simple PDF
        CourseStatsDto stats = statsService.computeCourseStats(teacherId, specialtyId, levelId, academicYearId, courseId);
        byte[] pdf = PdfUtil.buildSimplePdf(stats); // implement PdfUtil to return byte[]
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"report-" + teacherId + ".pdf\"")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
   
    // Créer un professeur
    // @GetMapping("/professeur/seances")
    // public List<SessionDTO> getSeancesByTeacher(@RequestParam Long teacherId) {
    //     return sessionService.getSessionsByTeacherId(teacherId);
    // }

    // Chercher un professeur par ID
   @GetMapping("/teachers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PROFESSEUR')")
    public ResponseEntity<Map<String,Object>> getTeacherById(@PathVariable Long id) {
        Optional<Teacher> opt = teacherService.getById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Teacher t = opt.get();

        Map<String, Object> out = new HashMap<>();
        out.put("id", t.getId());
        out.put("fullName", t.getFullName());
        out.put("firstName", null);
        out.put("lastName", null);
        out.put("matricule", null);
        out.put("avatarUrl", null);
        out.put("sexe",t.getSexe() !=null ?  t.getSexe().getName() :null);
        out.put("email", t.getEmail());
        out.put("roleLabel", t.getUser() != null && t.getUser().getRole() != null ? t.getUser().getRole().name() : "PROFESSEUR");

        return ResponseEntity.ok(out);
    }

    // Supprimer un professeur
    

    // private TeacherDto toDto(Teacher t) {
    //     throw new UnsupportedOperationException("Not supported yet.");
    // }

    // DTO pour la requête
   





    @GetMapping("/{id}")
       
    public Teacher getTeacher(@PathVariable Long id) {
        return teacherService.getById(id).orElseThrow(() -> new RuntimeException("Professeur non trouvé"));
    }


//    @GetMapping({"/teachers/me"})
//     @PreAuthorize("hasAnyRole('PROFESSEUR','ADMIN')")
//     public ResponseEntity<Map<String, Object>> getMyProfile(Authentication authentication) {
//         if (authentication == null || authentication.getName() == null) {
//             return ResponseEntity.status(401).build();
//         }
//         String username = authentication.getName();
//         Optional<Teacher> opt = teacherService.findByUsername(username);
//         if (opt.isEmpty()) {
//             return ResponseEntity.notFound().build();
//         }
//         Teacher t = opt.get();

//         Map<String, Object> out = new HashMap<>();
//         out.put("id", t.getId());
//         out.put("fullName", t.getFullName());
//         // split fullName to first/last if needed
//         String first = null, last = null;
//         if (t.getFullName() != null && t.getFullName().trim().length() > 0) {
//             String[] parts = t.getFullName().trim().split("\\s+");
//             if (parts.length >= 1) first = parts[0];
//             if (parts.length >= 2) last = parts[parts.length - 1];
//         }
//         out.put("firstName", first);
//         out.put("lastName", last);

//         // Teacher model doesn't have matricule/avatarUrl by default: fallback to id or null
//         out.put("matricule", null); // si tu as un champ matricule, remplace par t.getMatricule()
//         out.put("avatarUrl", null); // si tu as avatarUrl, remplace
//         out.put("email", t.getEmail());

//         if (t.getUser() != null && t.getUser().getRole() != null) {
//             out.put("roleLabel", t.getUser().getRole().name());
//         } else {
//             out.put("roleLabel", "PROFESSEUR");
//         }
//         return ResponseEntity.ok(out);
//     }

    @GetMapping("teacher/me")
        @PreAuthorize("hasRole('ADMIN','PROFESSEUR')")
    public Teacher getCurrentTeacher(@RequestParam String email) {
        return teacherService.getByEmail(email).orElseThrow(() -> new RuntimeException("Professeur non trouvé"));
    }

    @GetMapping("/{id}/profiles")
        @PreAuthorize("hasRole('ADMIN','PROFESSEUR')")
    public List<TeacherYearProfile> getTeacherProfiles(@PathVariable Long id) {
        return teacherService.getProfiles(id);
    }

    @GetMapping("/{id}/profiles/active")
        @PreAuthorize("hasRole('ADMIN','PROFESSEUR')")
    public List<TeacherYearProfile> getActiveProfiles(@PathVariable Long id) {
        return teacherService.getActiveProfiles(id);
    }
}