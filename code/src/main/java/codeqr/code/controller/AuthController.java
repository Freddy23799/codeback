










package codeqr.code.controller;

import codeqr.code.dto.LoginRequest;
// import codeqr.code.dto.LoginResponse;
import codeqr.code.model.*;
import codeqr.code.repository.*;
import codeqr.code.security.jwt.JwtService;
import codeqr.code.security.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final AdminRepository adminRepository;
   private final SurveillantRepository surveillantRepository;
   private final ResponsableRepository responsableRepository;
   @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        try {
            // Authentification
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User user = userDetails.getUser();

            // Génération du JWT
            String token = jwtService.generateToken(userDetails);

            // Préparer les infos selon le rôle
            Long id = null;
            String fullName = null;

            switch (user.getRole()) {
                case ETUDIANT -> {
                    Student student = studentRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil étudiant introuvable"));
                    id = student.getId();
                    fullName = student.getFullName();
                }
                case PROFESSEUR -> {
                    Teacher teacher = teacherRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil enseignant introuvable"));
                    id = teacher.getId();
                    fullName = teacher.getFullName();
                }
                case ADMIN -> {
                    Admin admin = adminRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil admin introuvable"));
                    id = admin.getId();
                    fullName = admin.getFullName();
                }
                case SURVEILLANT -> {
                    Surveillant surveillant = surveillantRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil surveillant introuvable"));
                    id = surveillant.getId();
                    fullName = surveillant.getFullName();
                }
                case RESPONSABLE -> {
                    Responsable responsable = responsableRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil surveillant introuvable"));
                    id = responsable.getId();
                    fullName = responsable.getFullName();
                }
            }

            // ✅ Vérification du champ privacyPolicyAcceptedAt
            boolean accepted = user.getPrivacyPolicyAcceptedAt() != null;

            // Retour de la réponse
            return new LoginResponse(token, id, fullName, user.getRole().name(), user.getUsername(), accepted);

        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }
    }

    // ----- DTO LoginResponse -----
    public static class LoginResponse {
        private String token;
        private Long id;
        private String fullName;
        private String role;
        private String username;
        private boolean accepted;

        public LoginResponse(String token, Long id, String fullName, String role, String username, boolean accepted) {
            this.token = token;
            this.id = id;
            this.fullName = fullName;
            this.role = role;
            this.username = username;
            this.accepted = accepted;
        }

        // Getters & Setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public boolean isAccepted() { return accepted; }
        public void setAccepted(boolean accepted) { this.accepted = accepted; }
    } 
    // Optionnel : endpoint pour vérifier le token
    @GetMapping("/me")
    public LoginResponse me(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        Long id = null;
        String fullName = null;

        switch (user.getRole()) {
            case ETUDIANT -> {
                id = studentRepository.findByUserId(user.getId()).map(Student::getId).orElse(null);
                fullName = studentRepository.findByUserId(user.getId()).map(Student::getFullName).orElse(null);
            }
            case PROFESSEUR -> {
                id = teacherRepository.findByUserId(user.getId()).map(Teacher::getId).orElse(null);
                fullName = teacherRepository.findByUserId(user.getId()).map(Teacher::getFullName).orElse(null);
            }
            case ADMIN -> {
                id = adminRepository.findByUserId(user.getId()).map(Admin::getId).orElse(null);
                fullName = adminRepository.findByUserId(user.getId()).map(Admin::getFullName).orElse(null);
            }
        }
 boolean accepted = user.getPrivacyPolicyAcceptedAt() != null;
        return new LoginResponse(null, id, fullName, user.getRole().name(), user.getUsername(), accepted);
    }



}