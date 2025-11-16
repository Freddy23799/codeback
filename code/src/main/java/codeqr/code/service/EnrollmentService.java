package codeqr.code.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codeqr.code.exception.NotFoundException;
import codeqr.code.model.Enrollment;
import codeqr.code.model.Level;
// import codeqr.code.model.Session;
import codeqr.code.model.Specialty;
import codeqr.code.model.StudentYearProfile;
import codeqr.code.repository.EnrollmentRepository;
import codeqr.code.repository.StudentYearProfileRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentYearProfileRepository profileRepository;
    // private final SessionRepository sessionRepository;

    /* -------------------- CRUD -------------------- */

    @Transactional(readOnly = true)
    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Enrollment get(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Enrollment not found: " + id));
    }

    public Enrollment create(Long studentYearProfileId) {
        StudentYearProfile profile = profileRepository.findById(studentYearProfileId)
                .orElseThrow(() -> new NotFoundException("StudentYearProfile not found: " + studentYearProfileId));

        // Un seul enrollment par profile (si besoin)
        return enrollmentRepository.findByStudentYearProfileId(profile.getId())
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setStudentYearProfile(profile);
                    return enrollmentRepository.save(enrollment);
                });
    }

    public Enrollment update(Long enrollmentId, Long studentYearProfileId) {
        Enrollment enrollment = get(enrollmentId);

        if (studentYearProfileId != null) {
            StudentYearProfile profile = profileRepository.findById(studentYearProfileId)
                    .orElseThrow(() -> new NotFoundException("StudentYearProfile not found: " + studentYearProfileId));
            enrollment.setStudentYearProfile(profile);
        }

        return enrollmentRepository.save(enrollment);
    }

    public void delete(Long id) {
        if (!enrollmentRepository.existsById(id))
            throw new NotFoundException("Enrollment not found: " + id);
        enrollmentRepository.deleteById(id);
    }

    /* -------------------- Recherches -------------------- */

    /**
     * Récupère tous les enrollments d’un étudiant **avec** la liste complète des sessions
     * basées sur (academicYear, level, specialty) du profile associé.
     */
   

    /* -------------------- DTOs pour la vue -------------------- */

    @Data
    @AllArgsConstructor
    public static class SessionDTO {
        private Long id;
        private String course;   // course.title (fallback code)
        private String campus;   // campus.name
        private String room;     // room.name
        private String teacher;  // teacherYearProfile.teacher.fullName
        private String date;     // yyyy-MM-dd
        private String day;      // Lundi, Mardi, ...
        private String duration; // "2h" etc.
        private String status;   // "open"/"closed"
        private String startTimeIso; // ISO 8601 pour la vue (formatage client)
        private String endTimeIso;
    }

    @Data
    @AllArgsConstructor
    public static class EnrollmentDTO {
        private Long id;
        private String academicYear; // academicYear.label
        private String specialty;    // specialty.name
        private String level;        // level.name
        private List<SessionDTO> sessions;
    }

    // @Transactional(readOnly = true)
    // public List<EnrollmentDTO> findByStudentIdWithSessionsDTO(Long studentId) {
    //     DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    //     List<Enrollment> enrollments = findByStudentIdWithSessions(studentId);

    //     return enrollments.stream().map(enr -> {
    //         StudentYearProfile p = enr.getStudentYearProfile();

    //         String academicLabel = (p != null && p.getAcademicYear() != null) ? p.getAcademicYear().getLabel() : null;
    //         String specialtyName  = (p != null && p.getSpecialty() != null)   ? p.getSpecialty().getName() : null;
    //         String levelName      = (p != null && p.getLevel() != null)       ? p.getLevel().getName() : null;

    //         List<SessionDTO> sessionDTOs = (enr.getSessions() == null ? Collections.<Session>emptyList() : enr.getSessions())
    //                 .stream()
    //                 .map(s -> {
    //                     String courseTitle = null;
    //                     if (s.getCourse() != null) {
    //                         // title prioritaire, sinon code comme fallback
    //                         courseTitle = (s.getCourse().getTitle() != null && !s.getCourse().getTitle().isBlank())
    //                                 ? s.getCourse().getTitle()
    //                                 : s.getCourse().getCode();
    //                     }
    //                     String campusName = (s.getCampus() != null) ? s.getCampus().getName() : null;
    //                     String roomName   = (s.getRoom() != null)   ? s.getRoom().getName() : null;

    //                     String teacherName = null;
    //                     if (s.getTeacherYearProfile() != null && s.getTeacherYearProfile().getTeacher() != null) {
    //                         teacherName = s.getTeacherYearProfile().getTeacher().getFullName();
    //                     }

    //                     String date = (s.getStartTime() != null) ? s.getStartTime().toLocalDate().toString() : null;
    //                     String day  = (s.getStartTime() != null) ? s.getStartTime().getDayOfWeek().name() : null;

    //                     String duration = null;
    //                     String startIso = null;
    //                     String endIso   = null;
    //                     if (s.getStartTime() != null && s.getEndTime() != null) {
    //                         long hours = Math.max(1L, Duration.between(s.getStartTime(), s.getEndTime()).toHours());
    //                         duration = hours + "h";
    //                         startIso = s.getStartTime().format(iso);
    //                         endIso   = s.getEndTime().format(iso);
    //                     }

    //                     String status = s.isClosed() ? "closed" : "open";

    //                     return new SessionDTO(
    //                             s.getId(),
    //                             courseTitle,
    //                             campusName,
    //                             roomName,
    //                             teacherName,
    //                             date,
    //                             // Convertit MONDAY -> LUNDI etc. (simplement capitalisé ; ta vue peut re-formater)
    //                             day != null ? capitalizeFr(day) : null,
    //                             duration,
    //                             status,
    //                             startIso,
    //                             endIso
    //                     );
    //                 })
    //                 .collect(Collectors.toList());

    //         return new EnrollmentDTO(
    //                 enr.getId(),
    //                 academicLabel,
    //                 specialtyName,
    //                 levelName,
    //                 sessionDTOs
    //         );
    //     }).collect(Collectors.toList());
    // }

    // /* -------------------- Helpers -------------------- */

    // private static String capitalizeFr(String englishUpperDay) {
    //     // Transforme "MONDAY" en "Lundi" (approx — côté front on peut localiser proprement)
    //     String lower = englishUpperDay.toLowerCase(); // monday
    //     String cap = Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    //     switch (cap) {
    //         case "Monday": return "Lundi";
    //         case "Tuesday": return "Mardi";
    //         case "Wednesday": return "Mercredi";
    //         case "Thursday": return "Jeudi";
    //         case "Friday": return "Vendredi";
    //         case "Saturday": return "Samedi";
    //         case "Sunday": return "Dimanche";
    //         default: return cap;
    //     }
    // }


























    @Transactional(readOnly = true)
    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentYearProfile_Student_Id(studentId);
    }

    @Transactional(readOnly = true)
    public Enrollment getEnrollment(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Enrollment not found: " + id));
    }

    public Enrollment createEnrollment(Long studentId, Long specialtyId, Long levelId, Long academicYearId) {
        StudentYearProfile profile = profileRepository.findByStudentIdAndAcademicYearId(studentId, academicYearId)
                .orElseThrow(() -> new NotFoundException("Profile not found for this student and academic year"));

        profile.setLevel(new Level()); // TODO: inject actual level entity
        profile.setSpecialty(new Specialty()); // TODO: inject actual specialty entity

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentYearProfile(profile);
        return enrollmentRepository.save(enrollment);
    }

    public Enrollment updateEnrollment(Long id, Long specialtyId, Long levelId, Long academicYearId) {
        Enrollment enrollment = getEnrollment(id);
        StudentYearProfile profile = enrollment.getStudentYearProfile();
        profile.setLevel(new Level()); // TODO: inject actual level entity
        profile.setSpecialty(new Specialty()); // TODO: inject actual specialty entity
        return enrollmentRepository.save(enrollment);
    }

    public void deleteEnrollment(Long id) {
        Enrollment enrollment = getEnrollment(id);
        enrollmentRepository.delete(enrollment);
    }

  
}
