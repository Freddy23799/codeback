package codeqr.code.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import codeqr.code.dto.UpdateUserDTO;
import codeqr.code.dto.AdminDTO;
import codeqr.code.dto.AdminRequest;
import codeqr.code.dto.PagedResponse;
import codeqr.code.dto.PagedResult;
import codeqr.code.dto.ResponsableListDTO;
import codeqr.code.dto.ResponsableRequest;
import codeqr.code.dto.StudentInSessionDTO;
import codeqr.code.dto.SurveillantListDTO;
import codeqr.code.dto.SurveillantRequest;
import codeqr.code.dto.TeacherLightDTO;
import codeqr.code.dto.TeacherListDTO;
import codeqr.code.dto.TeacherRequest;
import codeqr.code.dto.UpdateUserRequest;
import codeqr.code.dto2.ResponsableDTO;
import codeqr.code.dto2.SurveillantDTO;
import codeqr.code.dto2.TeacherDTO;
import codeqr.code.model.Admin;
import codeqr.code.model.Attendance;
import codeqr.code.model.Level;
import codeqr.code.model.Student;
import codeqr.code.repository.AttendanceRepository;
import codeqr.code.repository.CourseRepository;
import codeqr.code.repository.EnrollmentRepository;
import codeqr.code.repository.LevelRepository;
import codeqr.code.repository.NotificationRepository;
import codeqr.code.repository.ResponsableRepository;
import codeqr.code.repository.SessionRepository;
import codeqr.code.repository.SpecialtyRepository;
import codeqr.code.repository.StudentRepository;
import codeqr.code.repository.StudentYearProfileRepository;
import codeqr.code.repository.SurveillantRepository;
import codeqr.code.repository.TeacherRepository;
import codeqr.code.repository.UserRepository;
import codeqr.code.service.AdminService;
import codeqr.code.service.ResponsableService;
import codeqr.code.service.SurveillantService;
import codeqr.code.service.TeacherReadService;
import codeqr.code.service.TeacherService;
import codeqr.code.service.UserUpdateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:9000")
public class AdminController {

    private final TeacherService teacherService;
    private final StudentYearProfileRepository studentYearProfileRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final NotificationRepository notificationRepository;
    private final SurveillantService surveillantService;
    private final LevelRepository levelRepository;
    private final AdminService adminService;
    private final SpecialtyRepository specialtyRepository;
    private final SessionRepository sessionRepository;
    private final TeacherReadService teacherReadService;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
        private final   UserUpdateService  userUpdateService;
        private final   ResponsableService responsableService;
      private final  ResponsableRepository responsableRepository;
      private final  SurveillantRepository surveillantRepository;
      
    public AdminController(
            UserRepository userRepository,
            AdminService adminService,
            AttendanceRepository attendanceRepository,
            StudentYearProfileRepository studentYearProfileRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            TeacherService teacherService,
            CourseRepository courseRepository,
            NotificationRepository notificationRepository,
            EnrollmentRepository enrollmentRepository,
            LevelRepository levelRepository,
            SessionRepository sessionRepository,
            TeacherReadService teacherReadService,
            SurveillantService surveillantService,
            UserUpdateService  userUpdateService,
            SpecialtyRepository specialtyRepository,
            ResponsableService responsableService,
            ResponsableRepository responsableRepository,
            SurveillantRepository surveillantRepository
    ) {
        this.attendanceRepository = attendanceRepository;
        this.teacherService = teacherService;
        this.adminService = adminService;
        this.surveillantService = surveillantService;
        this.studentYearProfileRepository = studentYearProfileRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.courseRepository = courseRepository;
        this.notificationRepository = notificationRepository;
        this.sessionRepository = sessionRepository;
        this.levelRepository = levelRepository;
        this.specialtyRepository = specialtyRepository;
        this.teacherReadService = teacherReadService;
        this.userRepository = userRepository;
         this.userUpdateService = userUpdateService;
         this.surveillantRepository = surveillantRepository;
         this.responsableRepository = responsableRepository;
         this.responsableService = responsableService;
    }


@Cacheable(cacheNames = "teachers", key = "'allTeachers'")
   @GetMapping("/professors")
public List<Map<String, Object>> listTeachers() {
    List<Object[]> results = teacherRepository.findAllIdAndFullName();
    List<Map<String, Object>> teachers = new ArrayList<>();
    
    for (Object[] row : results) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", row[0]);
        map.put("fullName", row[1]);
        teachers.add(map);
    }
    
    return teachers;
}

@Cacheable(cacheNames = "stats", key = "'globalStats'")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("students", studentRepository.count());
        stats.put("teachers", teacherRepository.count());
        stats.put("courses", courseRepository.count());
        stats.put("notifications", notificationRepository.count());
        stats.put("levels", levelRepository.count());
        stats.put("specialties", specialtyRepository.count());
        stats.put("sessions", sessionRepository.count());
          stats.put("attendances", attendanceRepository.count());
          stats.put("surveillant", surveillantRepository.count());
          stats.put("responsables", responsableRepository.count());
        return stats;
    }
@Cacheable(cacheNames = "enrollmentsByLevel", key = "#yearId != null ? #yearId : 'all'")
    @GetMapping("/enrollments-by-level")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getEnrollmentsByLevel(@RequestParam(required = false) Long yearId) {
        List<Level> levels = studentYearProfileRepository.findAllLevels();
        List<Long> counts = new ArrayList<>();
        for (Level level : levels) {
            Long c = (yearId != null) ?
                    studentYearProfileRepository.countByLevelAndYear(level.getId(), yearId) :
                    studentYearProfileRepository.countByLevel(level.getId());
            counts.add(c == null ? 0L : c);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("levels", levels.stream().map(Level::getName).collect(Collectors.toList()));
        resp.put("counts", counts);
        return resp;
    }

    @PutMapping("/teachers/{id}")
    public ResponseEntity<TeacherDTO> updateTeacher(@PathVariable Long id, @RequestBody TeacherRequest req) {
        return ResponseEntity.ok(teacherService.updateTeacher(id, req));
    }

    @PutMapping("/surveillant/{id}")
    public ResponseEntity<SurveillantDTO> updateSurveillant(@PathVariable Long id, @RequestBody SurveillantRequest req) {
        return ResponseEntity.ok(teacherService.updateSurveillant(id, req));
    }






    @PutMapping("/responsable/{id}")
    public ResponseEntity<ResponsableDTO> updateResponsable(@PathVariable Long id, @RequestBody ResponsableRequest req) {
        return ResponseEntity.ok(teacherService.updateResponsable(id, req));
    }
    @PostMapping("/create-teacher")
    public ResponseEntity<TeacherDTO> createTeacher(@RequestBody TeacherRequest req) {
        return ResponseEntity.status(201).body(teacherService.createTeacher(req));
    }

    @PostMapping("/create-surveillant")
    public ResponseEntity<SurveillantDTO> createSurveillant(@RequestBody SurveillantRequest req) {
        return ResponseEntity.status(201).body(teacherService.createSurveillant(req));
    }



  @PostMapping("/create-responsable")
    public ResponseEntity<ResponsableDTO> createResponsable(@RequestBody ResponsableRequest req) {
        return ResponseEntity.status(201).body(teacherService.createResponsable(req));
    }


  

    @GetMapping("/sessions/{id}/students")
    public List<Map<String, Object>> getStudentsForSession(@PathVariable Long id) {
        List<Attendance> attendances = attendanceRepository.findBySession_Id(id);
        return attendances.stream().map(a -> {
            Student s = a.getStudentYearProfile().getStudent();
            Map<String, Object> m = new HashMap<>();
            m.put("studentId", s.getId());
            m.put("studentName", s.getFullName());
            m.put("studentMatricule", s.getMatricule());
            m.put("sexe", s.getSexe() != null ? s.getSexe().getName() : null);
            m.put("status", a.getStatus() != null ? a.getStatus().name() : "PENDING");
            return m;
        }).toList();
    }

    @DeleteMapping("/teachers/{id}")
    @PreAuthorize("hasRole('ADMIN','PROFESSEUR')")
    public ResponseEntity<String> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacherAndUser(id);
        return ResponseEntity.ok("Professeur supprimé avec succès");
    }

    @DeleteMapping("/surveillant/{id}")
    @PreAuthorize("hasRole('ADMIN','SURVEILLANT')")
    public ResponseEntity<String> deleteSurveillant(@PathVariable Long id) {
        teacherService.deleteSurveillantAndUser(id);
        return ResponseEntity.ok("Surveillant supprimé avec succès");
    }




    @DeleteMapping("/responsable/{id}")
    @PreAuthorize("hasRole('ADMIN','RESPONSABLE')")
    public ResponseEntity<String> deleteResponsable(@PathVariable Long id) {
        teacherService.deleteResponsableAndUser(id);
        return ResponseEntity.ok("Surveillant supprimé avec succès");
    }







    @GetMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN','PROFESSEUR')")
    public ResponseEntity<List<TeacherDTO>> getAllTeachers() {
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @GetMapping("/teacher")
    public ResponseEntity<?> listTeachers(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String q
    ) {
        PagedResponse<TeacherListDTO> page = teacherReadService.listTeachers(cursor, limit, q);
        return ResponseEntity.ok().body(page);
    }

    @GetMapping("/surveillant")
    public ResponseEntity<List<SurveillantListDTO>> listSurveillants(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(surveillantService.list(cursor, limit, q));
    }
    @GetMapping("/responsable")
    public ResponseEntity<List<ResponsableListDTO>> listResponsables(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(responsableService.list(cursor, limit, q));
    }
   
@PutMapping("/update")
public ResponseEntity<?> updateUser(@RequestBody @Valid UpdateUserDTO dto,
                                    HttpServletRequest request) {
    try {
        // 1️⃣ Mettre à jour l'utilisateur via le service
        userUpdateService.updateUser(
                dto.getProfileId(),
                dto.getUsername(),
                dto.getPassword(),
                dto.getRole()
        );

        // 2️⃣ Invalider la session actuelle si elle existe
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();  // détruit la session serveur
        }

        // 3️⃣ Retourner un message pour forcer le client à se reconnecter
        return ResponseEntity.ok("Profil mis à jour avec succès. Veuillez vous reconnecter.");
        
    } catch (RuntimeException e) {
        // Erreurs liées à l'entité ou au rôle
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
        // Erreurs serveur inattendues
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur serveur lors de la mise à jour du profil");
    }
}


    @GetMapping("/admins")
    public ResponseEntity<List<AdminDTO>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/students/{id}")
    public ResponseEntity<AdminDTO> getAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

    @PostMapping("/create-admin")
    public ResponseEntity<AdminDTO> createAdmin(@RequestBody AdminRequest req) {
        return ResponseEntity.status(201).body(adminService.createAdmin(req));
    }

    @PutMapping("/admins/{id}")
    public ResponseEntity<AdminDTO> updateAdmin(@PathVariable Long id, @RequestBody AdminRequest req) {
        return ResponseEntity.ok(adminService.updateAdmin(id, req));
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdminAndUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMyProfile(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        Admin admin = adminService.findByUsername(username);
        Map<String, Object> out = new HashMap<>();
        out.put("id", admin.getId());
        out.put("fullName", admin.getFullName());
        String first = null, last = null;
        if (admin.getFullName() != null && admin.getFullName().trim().length() > 0) {
            String[] parts = admin.getFullName().trim().split("\\s+");
            if (parts.length >= 1) first = parts[0];
            if (parts.length >= 2) last = parts[parts.length - 1];
        }
        out.put("firstName", first);
        out.put("lastName", last);
        out.put("matricule", null);
        out.put("avatarUrl", null);
        out.put("email", admin.getEmail());
        out.put("sexe", admin.getSexe() != null ? admin.getSexe().getName() : null);
        if (admin.getUser() != null && admin.getUser().getRole() != null) {
            out.put("roleLabel", admin.getUser().getRole().name());
        } else {
            out.put("roleLabel", "ADMIN");
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping("/teacher-session")
    public List<TeacherLightDTO> teacherSessions(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "500") int limit) {
        return adminService.listTeachersLight(q, limit);
    }

    @GetMapping("/teacher/{id}/sessions")
    public List<codeqr.code.dto.SessionLightDT> teacherSessions(
            @PathVariable("id") Long teacherId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastStart,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false, name = "academicYearId") Long academicYear,
            @RequestParam(required = false, name = "specialtyId") Long specialty,
            @RequestParam(required = false, name = "levelId") Long level) {
        return adminService.listSessionsByTeacher(teacherId, start, end, lastStart, lastId, limit, academicYear, specialty, level);
    }

    @GetMapping("/session/{id}/students")
    public PagedResult<StudentInSessionDTO> sessionStudents(
            @PathVariable("id") Long sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "150") int size,
            @RequestParam(required = false) String q) {
        return adminService.listStudentsBySession(sessionId, page, size, q);
    }
}
