// package codeqr.code.controller;

// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional;

// import codeqr.code.dto.AdminRequest;
// import codeqr.code.model.Sexe;
// import codeqr.code.repository.AdminRepository;
// import codeqr.code.repository.CourseRepository;
// import codeqr.code.repository.SexeRepository;
// import codeqr.code.repository.StudentRepository;
// import codeqr.code.repository.TeacherRepository;
// import codeqr.code.service.AdminService;
// import codeqr.code.service.StudentService;
// import codeqr.code.service.TeacherService;
// import lombok.RequiredArgsConstructor;

// @Component
// @RequiredArgsConstructor
// public class DataInitializer implements CommandLineRunner {

//     private final AdminService adminService;
//     private final StudentService studentService;
//     private final TeacherService teacherService;
//     private final SexeRepository sexeRepository;
//     private final CourseRepository courseRepository;
    
//     private final AdminRepository adminRepository;
//     private final StudentRepository studentRepository;
//     private final TeacherRepository teacherRepository;

//     @Override
//     @Transactional
//     public void run(String... args) throws Exception {
//         // Vérifier si aucun utilisateur n'existe
//         if(adminRepository.count() + studentRepository.count() + teacherRepository.count() != 0) {

//             // Récupérer un sexe existant (premier trouvé)
//             Sexe sexe = sexeRepository.findAll().stream().findFirst()
//                     .orElseThrow(() -> new RuntimeException("Aucun sexe trouvé dans la base"));

// //             // =================== ADMIN ===================
//             AdminRequest adminReq = new AdminRequest();
//             adminReq.setUsername("admin12");
//             adminReq.setPassword("admin1234");
//             adminReq.setFullName("Admiistrateur Principal");
//             adminReq.setEmail("azd@example.com");
//             adminReq.setMatricule("AMz001");
//             adminReq.setSexeId(sexe.getId());
//             adminService.createAdmin(adminReq);
//             System.out.println("Utilisateur ADMIN par défaut créé !");

// //             // =================== ETUDIANT ===================
// //             StudentRequest studentReq = new StudentRequest();
// //             studentReq.setUsername("freddy236");
// //             studentReq.setPassword("admin124");
// //             studentReq.setFullName("Freddy Etudiant");
// //             studentReq.setEmail("fredd3@example.com");
// //             studentReq.setMatricule("ET001");
// //             studentReq.setSexeId(sexe.getId());
// //             studentService.createStudent(studentReq);
// //             System.out.println("Utilisateur ETUDIANT par défaut créé !");

// //             // =================== PROFESSEUR ===================
// //             List<Course> allCourses = courseRepository.findAll();
// //             Random rnd = new Random();
// //             List<Long> selectedCourseIds = new ArrayList<>();

// //             // Choisir 2 cours aléatoires sans doublons
// //             for(int i = 0; i < 2 && i < allCourses.size(); i++) {
// //                 int idx = rnd.nextInt(allCourses.size());
// //                 Long courseId = allCourses.get(idx).getId();
// //                 if(!selectedCourseIds.contains(courseId)) {
// //                     selectedCourseIds.add(courseId);
// //                 }
// //             }

// //             TeacherRequest teacherReq = new TeacherRequest();
// //             teacherReq.setUsername("freddy2378");
// //             teacherReq.setPassword("99639881");
// //             teacherReq.setFullName("Freddy Professeur");
// //             teacherReq.setEmail("fr@example.com");
// //             teacherReq.setMatricule("PROF01");
// //             teacherReq.setSexeId(sexe.getId());
// //             teacherReq.setCourseIds(selectedCourseIds);
// //             teacherService.createTeacher(teacherReq);
// //             System.out.println("Utilisateur PROFESSEUR par défaut créé avec 2 cours assignés !");
//         }
//     }
// }
