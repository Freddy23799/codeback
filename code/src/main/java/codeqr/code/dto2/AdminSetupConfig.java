// package codeqr.code.dto2; // adapte selon ton projet

// import org.springframework.boot.CommandLineRunner;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.crypto.password.PasswordEncoder;

// import codeqr.code.model.Admin;
// import codeqr.code.model.Role;
// import codeqr.code.model.Sexe;
// import codeqr.code.model.User;
// import codeqr.code.repository.AdminRepository;
// import codeqr.code.repository.SexeRepository;
// import codeqr.code.repository.UserRepository;

// @Configuration
// public class AdminSetupConfig {

//     @Bean
//     CommandLineRunner createDefaultAdmin(UserRepository userRepository,
//                                          SexeRepository sexeRepository,
//                                          AdminRepository adminRepository,
//                                          PasswordEncoder passwordEncoder) {
//         return args -> {
//             // Vérifie si l'admin existe déjà
//             if (userRepository.findByUsername("admi").isEmpty()) {

//                 // Création du User
//                 User u = new User();
//                 u.setUsername("admin");
//                 u.setPassword(passwordEncoder.encode("admin1234"));
//                 u.setRole(Role.ADMIN); // Enum Role.ADMIN

//                 // Récupération du sexe (par exemple avec ID = 1)
//                 Sexe sexe = sexeRepository.findById(1L)
//                         .orElseThrow(() -> new RuntimeException("Sexe non trouvé avec l'ID 1"));

//                 // Création de l'Admin associé
//                 Admin a = new Admin();
//                 a.setFullName("Administrateur Principal");
//                 a.setEmail("admins@example.com");
//                 a.setMatricule("ADMINA001");
//                 a.setSexe(sexe);

//                 // Association bidirectionnelle
//                 a.setUser(u);
//                 u.setAdmin(a);

//                 // Sauvegarde dans la base
//                 userRepository.save(u);
//                 adminRepository.save(a);

//                 System.out.println("Administrateur par défaut créé : username='admi', password='admin1234'");
//             } else {
//                 System.out.println("Administrateur déjà existant");
//             }
//         };
//     }
// }
