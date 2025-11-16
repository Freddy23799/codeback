

package codeqr.code.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codeqr.code.dto.EnrollmentDTO;
import codeqr.code.dto.EnrollmentListDTO;
import codeqr.code.dto.SessionDTO;
import codeqr.code.dto.SessionListDTO;
import codeqr.code.dto.SimpleDTO;
import codeqr.code.dto.StudentDTO;
import codeqr.code.dto.StudentListDTO;
import codeqr.code.dto.StudentRequest;
import codeqr.code.dto.StudentWithEnrollmentsDTO;
// import codeqr.code.dto2.StudentRequest;
import codeqr.code.exception.NotFoundException;
import codeqr.code.model.AcademicYear;
import codeqr.code.model.Attendance;
import codeqr.code.model.Enrollment;
import codeqr.code.model.Level;
import codeqr.code.model.Role;
import codeqr.code.model.Session;
import codeqr.code.model.Sexe;
import codeqr.code.model.Specialty;
import codeqr.code.model.Student;
import codeqr.code.model.StudentYearProfile;
import codeqr.code.model.Teacher;
import codeqr.code.model.User;
import codeqr.code.repository.*;
import lombok.*;
@Data
@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentYearProfileRepository profileRepository;
    private final AcademicYearRepository academicYearRepository;
    private final EnrollmentRepository enrollmentRepo;
    private final LevelRepository levelRepo;
    private final SpecialtyRepository specialtyRepo;
    private final UserRepository userRepository;
    private final MySessionRepository sessionRepository;
     private final SexeRepository sexeRepository;
  private final TeacherRepository teacherRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /* ---------------------- Student CRUD ---------------------- */
     private StudentDTO toDTO(Student s) {
        StudentDTO dto = new StudentDTO();
        dto.setId(s.getId());
        dto.setFullName(s.getFullName());
        dto.setEmail(s.getEmail());
        dto.setMatricule(s.getMatricule());
        dto.setUsername(s.getUser() != null ? s.getUser().getUsername() : null);
         dto.setSexeName(s.getSexe() != null ? s.getSexe().getName() : null);
        dto.setEnrollments(getEnrollmentsByStudent(s.getId()));
        return dto;
    }
    // public record StudentDTO(Long id,String fullName,String email,String sexeName,String matricule){}
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public StudentDTO getStudent(Long id) {
        return studentRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new NotFoundException("Student not found: " + id));
    }

    public StudentDTO createStudent(StudentRequest req) {
        Student s = new Student();
        s.setFullName(req.getFullName());
        s.setEmail(req.getEmail());
        s.setMatricule(req.getMatricule());

Sexe sexe =
    sexeRepository.findById(req.getSexeId())
    .orElseThrow(() ->
    new NotFoundException("sexe not found:" +req.getSexeId()));
    s.setSexe(sexe);

        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(passwordEncoder.encode(req.getPassword()));
        u.setRole(Role.ETUDIANT);

        s.setUser(u);
        u.setStudent(s);

        userRepository.save(u);
        studentRepository.save(s);
        return toDTO(s);
    }

    public StudentDTO updateStudent(Long id, StudentRequest req) {
        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student not found: " + id));

        s.setFullName(req.getFullName());
        s.setEmail(req.getEmail());
        s.setMatricule(req.getMatricule());

if(req.getSexeId() !=null){
    Sexe sexe =
    sexeRepository.findById(req.getSexeId())
    .orElseThrow(() ->
    new NotFoundException("sexe not found:" +req.getSexeId()));
    s.setSexe(sexe);
}




        User u = s.getUser();
        if (u != null) {
            u.setUsername(req.getUsername());
            if (req.getPassword() != null && !req.getPassword().isBlank()) {
                u.setPassword(passwordEncoder.encode(req.getPassword()));
            }
            userRepository.save(u);
        }

        studentRepository.save(s);
        return toDTO(s);
    }

    public void deleteStudent(Long id) {
        Student s = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student not found: " + id));
        User u = s.getUser();
        studentRepository.delete(s);
        if (u != null) userRepository.delete(u);
    }

    /* ---------------------- Enrollment CRUD ---------------------- */


     public List<EnrollmentDTO> getEnrollmentsByStudent(Long studentId) {
        List<StudentYearProfile> profiles = profileRepository.findByStudentId(studentId);
        return profiles.stream()
                .flatMap(p -> p.getEnrollments().stream())
                .map(this::toEnrollmentDTO)
                .collect(Collectors.toList());
    }
   
     public EnrollmentDTO addEnrollment(Long studentId, Long specialtyId, Long levelId, Long academicYearId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        Specialty sp = specialtyRepo.findById(specialtyId)
                .orElseThrow(() -> new NotFoundException("Specialty not found"));
        Level lvl = levelRepo.findById(levelId)
                .orElseThrow(() -> new NotFoundException("Level not found"));
        AcademicYear ay = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new NotFoundException("Academic year not found"));

        StudentYearProfile profile = profileRepository.findByStudentIdAndSpecialtyAndLevelAndAcademicYear(studentId, specialtyId, levelId, academicYearId)
                .orElseGet(() -> {
                    StudentYearProfile p = new StudentYearProfile();
                    p.setStudent(student);
                    p.setSpecialty(sp);
                    p.setLevel(lvl);
                    p.setAcademicYear(ay);
                    p.setActive(true);
                    return profileRepository.save(p);
                });

        Enrollment e = new Enrollment();
        e.setStudentYearProfile(profile);
        enrollmentRepo.save(e);

        return toEnrollmentDTO(e);
    }
   
    public EnrollmentDTO updateEnrollment(Long enrollmentId, Long specialtyId, Long levelId, Long academicYearId) {
        Enrollment e = enrollmentRepo.findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));

        StudentYearProfile profile = e.getStudentYearProfile();
        Specialty sp = specialtyRepo.findById(specialtyId).orElseThrow(() -> new NotFoundException("Specialty not found"));
        Level lvl = levelRepo.findById(levelId).orElseThrow(() -> new NotFoundException("Level not found"));
        AcademicYear ay = academicYearRepository.findById(academicYearId).orElseThrow(() -> new NotFoundException("Academic year not found"));

        profile.setSpecialty(sp);
        profile.setLevel(lvl);
        profile.setAcademicYear(ay);
        profileRepository.save(profile);

        return toEnrollmentDTO(e);
    }

    public void deleteEnrollment(Long enrollmentId) {
        Enrollment e = enrollmentRepo.findById(enrollmentId).orElseThrow(() -> new NotFoundException("Enrollment not found"));
        enrollmentRepo.delete(e);
    }

    // @Transactional(readOnly = true)
    // public List<SessionDTO> getSessionsForEnrollment(Long enrollmentId) {
    //     Enrollment enrollment = enrollmentRepo.findById(enrollmentId)
    //             .orElseThrow(() -> new NotFoundException("Enrollment not found: " + enrollmentId));

    //     StudentYearProfile profile = enrollment.getStudentYearProfile();
    //     if (profile == null) return List.of();

    //     Long levelId = profile.getLevel() != null ? profile.getLevel().getId() : null;
    //     Long specialtyId = profile.getSpecialty() != null ? profile.getSpecialty().getId() : null;
    //     Long academicYearId = profile.getAcademicYear() != null ? profile.getAcademicYear().getId() : null;

    //     if (levelId == null || specialtyId == null || academicYearId == null) return List.of();
    //     return null;

      
    // }

    private SessionDTO toSessionDTO(Session s) {
        SessionDTO dto = new SessionDTO();
        dto.setId(s.getId());
        if (s.getCourse() != null) { dto.setCourseId(s.getCourse().getId()); dto.setCourseName(s.getCourse().getTitle()); }
        if (s.getCampus() != null) { dto.setCampusId(s.getCampus().getId()); dto.setCampusName(s.getCampus().getName()); }
        if (s.getRoom() != null) { dto.setRoomId(s.getRoom().getId()); dto.setRoomName(s.getRoom().getName()); }
        // if (s.getTeacherYearProfile() != null && s.getTeacherYearProfile().getTeacher() != null)
        //     dto.setTeacherName(s.getTeacherYearProfile().getTeacher().getFullName());

        dto.setStartTime(s.getStartTime());
        dto.setEndTime(s.getEndTime());
        // dto.setClosed(s.isClosed());

        try {
            if (s.getStartTime() != null && s.getEndTime() != null) {
                long minutes = Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();
                dto.setDuration(minutes + " min");
            } else {
                dto.setDuration("");
            }
        } catch (Exception ex) {
            dto.setDuration("");
        }

        // dto.setStatus(s.isClosed() ? "closed" : "open");
        return dto;
    }

  

     public List<StudentWithEnrollmentsDTO> getAllStudentsWithEnrollments() {
        List<Student> students = studentRepository.findAllWithEnrollments();

        return students.stream().map(s -> {
            List<EnrollmentDTO> enrollments = s.getStudentYearProfiles().stream()
                .flatMap(p -> p.getEnrollments().stream())
                .map(this::toEnrollmentDTO)
                .toList();

            return new StudentWithEnrollmentsDTO(
                s.getId(),
                s.getFullName(),
                s.getMatricule(),
                s.getEmail(),
                s.getUser() != null ? s.getUser().getUsername() : null,
                s.getSexe() != null ? s.getSexe().getName() : null,
                enrollments
            );
        }).toList();
    }
private EnrollmentDTO toEnrollmentDTO(Enrollment e) {
    if (e == null || e.getStudentYearProfile() == null) return null;

    return new EnrollmentDTO(
        e.getId(),
        e.getStudentYearProfile().getAcademicYear() != null
            ? e.getStudentYearProfile().getAcademicYear().getLabel()
            : null,
        e.getStudentYearProfile().getSpecialty() != null
            ? e.getStudentYearProfile().getSpecialty().getName()
            : null,
        e.getStudentYearProfile().getLevel() != null
            ? e.getStudentYearProfile().getLevel().getName()
            : null
    );
}






   @Transactional(readOnly = true)
public List<SessionDTO> getSessionsForEnrollment(Long enrollmentId) {
    Enrollment enrollment = enrollmentRepo.findById(enrollmentId)
            .orElseThrow(() -> new NotFoundException("Enrollment not found: " + enrollmentId));

    StudentYearProfile profile = enrollment.getStudentYearProfile();
    if (profile == null) return List.of();

    // Récupérer toutes les sessions correspondant au profil
    List<Session> sessions = sessionRepository
        .findByExpectedLevelAndExpectedSpecialtyAndAcademicYear(
            profile.getLevel(),
            profile.getSpecialty(),
            profile.getAcademicYear()
        );

    return sessions.stream().map(s -> {
        SessionDTO dto = new SessionDTO();

        // ID de la session
        dto.setId(s.getId());

        // Nom du cours
        dto.setCourseName(s.getCourse() != null ? s.getCourse().getTitle() : "");

        // Nom du campus
        dto.setCampusName(s.getCampus() != null ? s.getCampus().getName() : "");

        // Nom de la salle
        dto.setRoomName(s.getRoom() != null ? s.getRoom().getName() : "");

        // Récupération du nom du professeur via la relation user et le repository
        String teacherName = "";
        if (s.getUser() != null) {
            teacherRepository.findByUser(s.getUser())
                             .ifPresent(teacher -> dto.setTeacherName(teacher.getFullName()));
        }

        // Dates et heures
        dto.setStartTime(s.getStartTime());
        dto.setEndTime(s.getEndTime());

        // Calculer la durée
        if (s.getStartTime() != null && s.getEndTime() != null) {
            long minutes = java.time.Duration.between(s.getStartTime(), s.getEndTime()).toMinutes();
            dto.setDuration(minutes + " min");
        } else {
            dto.setDuration("");
        }

        // Déterminer le statut pour cet étudiant
        String status = "open";
        Optional<Attendance> att = s.getAttendances().stream()
            .filter(a -> a.getStudentYearProfile().equals(profile))
            .findFirst();

        if (att.isPresent()) {
            status = att.get().getStatus().name().toLowerCase(); // present / absent / pending
        } else if (Boolean.TRUE.equals(s.getClosed())) {
            status = "closed";
        }
        dto.setStatus(status);

        return dto;
    }).toList();
}
















@Autowired
    private StudentRepositoryCustom repo;

    public List<StudentListDTO> listStudents(Long cursorId, int limit, String q, Long specialtyId, Long levelId) {
        return repo.fetchStudentsLight(cursorId, limit, q, specialtyId, levelId);
    }

    public List<EnrollmentListDTO> listEnrollments(Long studentId, int offset, int limit) {
        return repo.fetchEnrollmentsByStudent(studentId, offset, limit);
    }

    public List<SessionListDTO> listSessions(Long studentYearProfileId, LocalDateTime start, LocalDateTime end, LocalDateTime lastStartTime, Long lastId, int limit) {
        return repo.fetchSessionsByEnrollment(studentYearProfileId, start, end, lastStartTime, lastId, limit);
    }
}
