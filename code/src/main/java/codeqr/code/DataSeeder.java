// package codeqr.code;

// import java.time.Instant;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
// import java.util.Random;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// import com.fasterxml.jackson.databind.ObjectMapper;

// import codeqr.code.model.AcademicYear;
// import codeqr.code.model.Attendance;
// import codeqr.code.model.Campus;
// import codeqr.code.model.Course;
// import codeqr.code.model.Level;
// import codeqr.code.model.Role;
// import codeqr.code.model.Room;
// import codeqr.code.model.Session;
// import codeqr.code.model.Specialty;
// import codeqr.code.model.StudentYearProfile;
// import codeqr.code.model.User;
// import codeqr.code.repository.AcademicYearRepository;
// import codeqr.code.repository.AttendanceRepository;
// import codeqr.code.repository.CampusRepository;
// import codeqr.code.repository.CourseRepository;
// import codeqr.code.repository.LevelRepository;
// import codeqr.code.repository.RoomRepository;
// import codeqr.code.repository.SessionRepository;
// import codeqr.code.repository.SpecialtyRepository;
// import codeqr.code.repository.StudentYearProfileRepository;
// import codeqr.code.repository.UserRepository;
// import codeqr.code.security.qr.QrJwtService;

// /**
//  * DataSeeder corrigé : crée 10 sessions pour :
//  * expected_specialty_id = 1, expected_level_id = 1, academic_year_id = 1
//  *
//  * - Remplit explicitement academicYear/level/specialty (NOT NULL)
//  * - Génère qrToken via QrJwtService et sérialise qrPayload
//  * - Pré-crée attendances pour tous les student_year_profile du combo
//  *
//  * Colle ce fichier et démarre l'application.
//  */
// @Component
// public class DataSeeder implements CommandLineRunner {

//     @Autowired private UserRepository userRepository;
//     @Autowired private CourseRepository courseRepository;
//     @Autowired private CampusRepository campusRepository;
//     @Autowired private RoomRepository roomRepository;
//     @Autowired private SessionRepository sessionRepository;
//     @Autowired private AttendanceRepository attendanceRepository;
//     @Autowired private StudentYearProfileRepository studentYearProfileRepository;
//     @Autowired private LevelRepository levelRepository;
//     @Autowired private SpecialtyRepository specialtyRepository;
//     @Autowired private AcademicYearRepository academicYearRepository;
//     @Autowired private QrJwtService qrJwtService;

//     private final ObjectMapper objectMapper = new ObjectMapper();
//     private final Random random = new Random();

//     private static final int SESSIONS_TO_CREATE = 10;
//     private static final long QR_EXPIRATION_MS = 1000L * 60 * 60 * 2; // 2 hours
//     private static final Long EXPECTED_SPECIALTY_ID = 129L;
//     private static final Long EXPECTED_LEVEL_ID = 1L;
//     private static final Long ACADEMIC_YEAR_ID = 1L;

//     @Override
//     public void run(String... args) throws Exception {
//         System.out.println(">>> DataSeeder démarrage : création de " + SESSIONS_TO_CREATE + " sessions pour combo (specialty=1, level=1, year=1)");

//         // 1) vérifier et récupérer les entités obligatoires
//         Optional<Specialty> optSp = specialtyRepository.findById(EXPECTED_SPECIALTY_ID);
//         Optional<Level> optLv = levelRepository.findById(EXPECTED_LEVEL_ID);
//         Optional<AcademicYear> optAy = academicYearRepository.findById(ACADEMIC_YEAR_ID);

//         if (optSp.isEmpty() || optLv.isEmpty() || optAy.isEmpty()) {
//             System.err.println("[DataSeeder] ERROR: Specialty/Level/AcademicYear with id=1 not found. Aborting seed.");
//             if (optSp.isEmpty()) System.err.println(" - missing Specialty id=" + EXPECTED_SPECIALTY_ID);
//             if (optLv.isEmpty()) System.err.println(" - missing Level id=" + EXPECTED_LEVEL_ID);
//             if (optAy.isEmpty()) System.err.println(" - missing AcademicYear id=" + ACADEMIC_YEAR_ID);
//             return;
//         }

//         Specialty specialty = optSp.get();
//         Level level = optLv.get();
//         AcademicYear academicYear = optAy.get();

//         // 2) récupérer autres références utiles
//         List<User> professors = userRepository.findAllByRole(Role.PROFESSEUR);
//         if (professors == null || professors.isEmpty()) {
//             System.err.println("[DataSeeder] Aucun professeur trouvé (role=PROFESSEUR). Stop.");
//             return;
//         }

//         List<Course> courses = courseRepository.findAll();
//         List<Campus> campuses = campusRepository.findAll();
//         List<Room> rooms = roomRepository.findAll();

//         if (courses.isEmpty() || campuses.isEmpty() || rooms.isEmpty()) {
//             System.err.println("[DataSeeder] courses/campuses/rooms manquants. Stop.");
//             return;
//         }

//         // 3) pré-récupérer la liste des profils pour le combo (level=1, specialty=1, year=1)
//         List<StudentYearProfile> profilesForCombo =
//                 studentYearProfileRepository.findByLevelIdAndSpecialtyIdAndAcademicYearId(
//                         EXPECTED_LEVEL_ID, EXPECTED_SPECIALTY_ID, ACADEMIC_YEAR_ID);

//         System.out.println("[DataSeeder] profils pour combo -> " + (profilesForCombo == null ? 0 : profilesForCombo.size()));

//         int createdSessions = 0;
//         int createdAttendances = 0;

//         for (int i = 0; i < SESSIONS_TO_CREATE; i++) {
//             try {
//                 // pick random references
//                 User professor = professors.get(random.nextInt(professors.size()));
//                 Course course = courses.get(random.nextInt(courses.size()));
//                 Campus campus = campuses.get(random.nextInt(campuses.size()));
//                 Room room = rooms.get(random.nextInt(rooms.size()));

//                 LocalDateTime created = LocalDateTime.now();
//                 LocalDateTime startTime = created.plusDays(random.nextInt(14)); // within next 0..13 days
//                 LocalDateTime endTime = startTime.plusHours(2);

//                 // Construire la session et remplir TOUTES les colonnes NOT NULL
//                 Session session = new Session();
//                 session.setUser(professor);
//                 session.setCourse(course);
//                 session.setAcademicYear(academicYear);        // IMPORTANT : non-null
//                 session.setCampus(campus);
//                 session.setRoom(room);
//                 session.setExpectedLevel(level);              // IMPORTANT : non-null
//                 session.setExpectedSpecialty(specialty);      // IMPORTANT : non-null
//                 session.setStartTime(startTime);
//                 session.setEndTime(endTime);
//                 session.setCreated(created);
//                 session.setClosed(false);
//                 session.setExpiryTime(created.plusHours(2));
//                 session.setNotified(false);

//                 // Persist initial session pour obtenir l'ID (et respecter contraintes NOT NULL)
//                 session = sessionRepository.saveAndFlush(session);

//                 // Générer QR token + payload (comme dans createSession)
//                 String token = qrJwtService.generateQrToken(session.getId(), QR_EXPIRATION_MS);
//                 session.setQrToken(token);

//                 Map<String, Object> payloadMap = Map.of(
//                         "sessionId", session.getId(),
//                         "token", token,
//                         "issuedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
//                         "expected_specialty_id", specialty.getId(),
//                         "expected_level_id", level.getId()
//                 );
//                 session.setQrPayload(objectMapper.writeValueAsString(payloadMap));

//                 // Save session update (with QR fields)
//                 sessionRepository.saveAndFlush(session);

//                 // Pré-créer les attendances pour chaque profil du combo
//                 List<Attendance> attendances = new ArrayList<>();
//                 if (profilesForCombo != null && !profilesForCombo.isEmpty()) {
//                     for (StudentYearProfile profile : profilesForCombo) {
//                         Attendance att = new Attendance();
//                         att.setStudentYearProfile(profile);
//                         att.setSession(session);
//                         att.setStatus(Attendance.Status.ABSENT);
//                         att.setSource(Attendance.Source.qr);
//                         att.setTimestamp(Instant.now());
//                         att.setScannedAt(null);
//                         attendances.add(att);
//                     }
//                 }

//                 if (!attendances.isEmpty()) {
//                     attendanceRepository.saveAllAndFlush(attendances);
//                 }

//                 createdSessions++;
//                 createdAttendances += attendances.size();

//                 System.out.println("[DataSeeder] Session créée id=" + session.getId()
//                         + " prof=" + (professor != null ? professor.getUsername() : "null")
//                         + " attendances=" + attendances.size()
//                         + " totals: sessions=" + createdSessions + " attendances=" + createdAttendances);

//                 // petit délai pour laisser la DB respirer
//                 Thread.sleep(100L);

//             } catch (Exception ex) {
//                 System.err.println("[DataSeeder] Erreur lors de la création d'une session : " + ex.getMessage());
//                 ex.printStackTrace();
//             }
//         }

//         System.out.println(">>> DataSeeder terminé. sessions créées=" + createdSessions + ", attendances créées=" + createdAttendances);
//     }
// }
