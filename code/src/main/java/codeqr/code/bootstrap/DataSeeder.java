
// package codeqr.code.bootstrap;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Random;

// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional;

// import codeqr.code.dto.CreateSessionRequest;
// import codeqr.code.model.*;
// import codeqr.code.repository.*;
// import codeqr.code.service.SessionService;

// @Component
// public class DataSeeder implements CommandLineRunner {

//     private final SessionService sessionService;
//     private final CourseRepository courseRepository;
//     private final AcademicYearRepository academicYearRepository;
//     private final CampusRepository campusRepository;
//     private final RoomRepository roomRepository;
//     private final LevelRepository levelRepository;
//     private final SpecialtyRepository specialtyRepository;
//     private final UserRepository userRepository;
// private final SessionRepository sessionRepository;
//     private final Random random = new Random();

//     public DataSeeder(SessionService sessionService,
//                       CourseRepository courseRepository,
//                       AcademicYearRepository academicYearRepository,
//                       CampusRepository campusRepository,
//                       RoomRepository roomRepository,
//                       LevelRepository levelRepository,
//                       SpecialtyRepository specialtyRepository,
//                         SessionRepository sessionRepository,
//                       UserRepository userRepository) {
//         this.sessionService = sessionService;
//         this.courseRepository = courseRepository;
//         this.academicYearRepository = academicYearRepository;
//         this.campusRepository = campusRepository;
//         this.roomRepository = roomRepository;
//         this.levelRepository = levelRepository;
//         this.specialtyRepository = specialtyRepository;
//         this.userRepository = userRepository;
//             this.sessionRepository = sessionRepository;
//     }

//     @Override
//     @Transactional
//     public void run(String... args) throws Exception {
//         List<Course> courses = courseRepository.findAll();
//         List<AcademicYear> years = academicYearRepository.findAll();
//         List<Campus> campuses = campusRepository.findAll();
//         List<Room> rooms = roomRepository.findAll();
//         List<Level> levels = levelRepository.findAll();
//         List<Specialty> specialties = specialtyRepository.findAll();
//         List<User> professors = userRepository.findByRole(Role.PROFESSEUR);

//         if (courses.isEmpty() || years.isEmpty() || campuses.isEmpty() 
//                 || rooms.isEmpty() || levels.isEmpty() || specialties.isEmpty() || professors.isEmpty()) {
//             System.out.println("⚠ Impossible de générer les sessions, données manquantes");
//             return;
//         }

//         int totalSessions = 100_000;
//         int batchSize = 500; // Taille du batch pour le flush

//         for (int i = 1; i <= totalSessions; i++) {
//             Course course = courses.get(random.nextInt(courses.size()));
//             AcademicYear year = years.get(random.nextInt(years.size()));
//             Campus campus = campuses.get(random.nextInt(campuses.size()));
//             Room room = rooms.get(random.nextInt(rooms.size()));
//             Level level = levels.get(random.nextInt(levels.size()));
//             Specialty specialty = specialties.get(random.nextInt(specialties.size()));
//             User professor = professors.get(random.nextInt(professors.size()));

//             LocalDateTime start = LocalDateTime.now()
//                     .minusDays(random.nextInt(365))
//                     .withHour(8 + random.nextInt(8))
//                     .withMinute(0);
//             LocalDateTime end = start.plusHours(2);

//             CreateSessionRequest req = new CreateSessionRequest();
//             req.setUsername(professor.getUsername());
//             req.setCourseId(course.getId());
//             req.setAcademicYearId(year.getId());
//             req.setCampusId(campus.getId());
//             req.setRoomId(room.getId());
//             req.setExpectedLevelId(level.getId());
//             req.setExpectedSpecialtyId(specialty.getId());
//             req.setStartTime(start);
//             req.setEndTime(end);

//             sessionService.createSession(req);

//             // Log tous les 10
//             if (i % 10 == 0) {
//                 System.out.println("✔ " + i + " sessions créées et attendances générées.");
//             }

//             // Flush tous les batchSize pour éviter surcharge mémoire
//             if (i % batchSize == 0) {
//                 sessionRepository.flush(); // méthode à créer dans SessionService qui fait sessionRepository.flush()
//                 System.out.println("🔄 Flush effectué après " + i + " sessions");
//             }
//         }

//         System.out.println("✅ 100 000 sessions générées avec succès !");
//     }
// }
