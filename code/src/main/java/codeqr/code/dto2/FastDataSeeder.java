



// package codeqr.code.dto2;

// import codeqr.code.model.*;
// import codeqr.code.repository.*;
// import codeqr.code.security.qr.QrJwtService;
// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import lombok.RequiredArgsConstructor;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional;

// import jakarta.persistence.EntityManager;
// import java.time.*;
// import java.time.format.DateTimeFormatter;
// import java.time.temporal.ChronoUnit;
// import java.util.*;

// @Component
// @RequiredArgsConstructor
// public class FastDataSeeder implements CommandLineRunner {

//     private final UserRepository userRepository;
//     private final CourseRepository courseRepository;
//     private final AcademicYearRepository academicYearRepository;
//     private final CampusRepository campusRepository;
//     private final RoomRepository roomRepository;
//     private final LevelRepository levelRepository;
//     private final SpecialtyRepository specialtyRepository;
//     private final SessionRepository sessionRepository;
//     private final StudentYearProfileRepository studentYearProfileRepository;
//     private final AttendanceRepository attendanceRepository;
//     private final QrJwtService qrJwtService;
//     private final ObjectMapper objectMapper;
//     private final EntityManager entityManager;

//     private static final long QR_EXPIRATION_MS = 2 * 60 * 60 * 1000; // 2h
//     private static final int BATCH_SIZE = 500; // flush et clear tous les 500 sessions

//     @Override
//     public void run(String... args) throws Exception {
//         seedSessions(100_000);
//     }

//     @Transactional
//     public void seedSessions(int totalSessions) throws Exception {

//         List<User> profs = userRepository.findAllByRole(Role.PROFESSEUR);
//         List<Course> courses = courseRepository.findAll();
//         List<Campus> campuses = campusRepository.findAll();
//         List<Room> rooms = roomRepository.findAll();
//         List<Level> levels = levelRepository.findAll();
//         List<Specialty> specialties = specialtyRepository.findAll();
//         AcademicYear ay = academicYearRepository.getReferenceById(1L); // Année académique fixe

//         if (profs.isEmpty() || specialties.isEmpty() || levels.isEmpty()) {
//             throw new RuntimeException("Missing professors, specialties, or levels");
//         }

//         Random random = new Random();
//         List<Session> sessionBatch = new ArrayList<>();
//         List<Attendance> attendanceBatch = new ArrayList<>();

//         for (int i = 0; i < totalSessions; i++) {

//             User prof = profs.get(random.nextInt(profs.size()));
//             Course course = courses.get(random.nextInt(courses.size()));
//             Campus campus = campuses.get(random.nextInt(campuses.size()));
//             Room room = rooms.get(random.nextInt(rooms.size()));
//             Level level = levels.get(random.nextInt(levels.size()));
//             Specialty specialty = specialties.get(random.nextInt(specialties.size()));

//             Session session = new Session();
//             session.setUser(prof);
//             session.setCourse(course);
//             session.setAcademicYear(ay);
//             session.setCampus(campus);
//             session.setRoom(room);
//             session.setExpectedLevel(level);
//             session.setExpectedSpecialty(specialty);

//             LocalDateTime now = LocalDateTime.now().plus(i, ChronoUnit.MINUTES);
//             session.setStartTime(now);
//             session.setEndTime(now.plusMinutes(90));
//             session.setCreated(LocalDateTime.now());
//             session.setClosed(false);
//             session.setExpiryTime(session.getCreated().plusHours(2));
//             session.setNotified(false);

//             sessionRepository.save(session); // save pour générer l'ID rapidement

//             // QR token
//             String token = qrJwtService.generateQrToken(session.getId(), QR_EXPIRATION_MS);
//             session.setQrToken(token);

//             Map<String, Object> payloadMap = Map.of(
//                     "sessionId", session.getId(),
//                     "token", token,
//                     "issuedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
//                     "expected_specialty_id", specialty.getId(),
//                     "expected_level_id", level.getId()
//             );
//             session.setQrPayload(objectMapper.writeValueAsString(payloadMap));

//             sessionBatch.add(session);

//             // Pré-créer les attendances
//             List<StudentYearProfile> profiles = studentYearProfileRepository
//                     .findByLevelIdAndSpecialtyIdAndAcademicYearId(level.getId(), specialty.getId(), ay.getId());

//             for (StudentYearProfile profile : profiles) {
//                 Attendance att = new Attendance();
//                 att.setStudentYearProfile(profile);
//                 att.setSession(session);
//                 att.setStatus(Attendance.Status.ABSENT);
//                 att.setSource(Attendance.Source.qr);
//                 att.setTimestamp(Instant.now());
//                 att.setScannedAt(null);
//                 attendanceBatch.add(att);
//             }

//             // Flush et clear périodique
//             if ((i + 1) % BATCH_SIZE == 0) {
//                 attendanceRepository.saveAll(attendanceBatch);
//                 sessionRepository.saveAll(sessionBatch);
//                 entityManager.flush();
//                 entityManager.clear();
//                 sessionBatch.clear();
//                 attendanceBatch.clear();
//             }
//         }

//         // Sauvegarde finale des batches restants
//         if (!sessionBatch.isEmpty()) {
//             attendanceRepository.saveAll(attendanceBatch);
//             sessionRepository.saveAll(sessionBatch);
//             entityManager.flush();
//             entityManager.clear();
//         }

//         System.out.println("Finished seeding " + totalSessions + " sessions!");
//     }
// }