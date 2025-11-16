package codeqr.code.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import codeqr.code.model.Admin;
import codeqr.code.model.Responsable;
import codeqr.code.model.Student;
import codeqr.code.model.Surveillant;
import codeqr.code.model.Teacher;
import codeqr.code.repository.AdminRepository;
import codeqr.code.repository.ResponsableRepository;
import codeqr.code.repository.StudentRepository;
import codeqr.code.repository.SurveillantRepository;
import codeqr.code.repository.TeacherRepository;
import codeqr.code.repository.UserRepository;

import java.util.Locale;

@Service
public class UserUpdateService {

    private static final Logger log = LoggerFactory.getLogger(UserUpdateService.class);

    private final UserRepository userRepository;
    private final ResponsableRepository responsableRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final AdminRepository adminRepository;
    private final SurveillantRepository surveillantRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserUpdateService(UserRepository userRepository,
                             ResponsableRepository responsableRepository,
                             StudentRepository studentRepository,
                             TeacherRepository teacherRepository,
                             AdminRepository adminRepository,
                             SurveillantRepository surveillantRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.responsableRepository = responsableRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.adminRepository = adminRepository;
        this.surveillantRepository = surveillantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Met à jour le username et le mot de passe pour un user à partir du profileId.
     * Maintenant utilise une requête native UPDATE (via userRepository.updateUsernameAndPasswordById)
     */
    @Transactional
    public void updateUser(Long profileId, String username, String password, String role) {
        if (profileId == null) throw new RuntimeException("ID du profile manquant");
        if (role == null || role.isBlank()) throw new RuntimeException("Rôle manquant");
        if (username == null || username.isBlank()) throw new RuntimeException("Le nom d'utilisateur ne peut pas être vide");

        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        String trimmedUsername = username.trim();

        // Protection basique : empêcher que username == rôle
        if (trimmedUsername.equalsIgnoreCase(normalizedRole)) {
            throw new RuntimeException("Le nom d'utilisateur ne peut pas être identique au rôle");
        }

        // Récupérer userId depuis profile
        Long userId = findUserIdByProfile(profileId, normalizedRole);

        // Vérifier unicité du username si changé : on utilise une méthode dérivée (existe déjà côté repo)
        if (userRepository.existsByUsernameAndIdNot(trimmedUsername, userId)) {
            throw new RuntimeException("Nom d'utilisateur déjà utilisé par un autre compte");
        }

        // Encoder le password si fourni, sinon on passe null pour ne pas l'écraser
        String encodedPassword = null;
        if (password != null && !password.isBlank()) {
            encodedPassword = passwordEncoder.encode(password);
        }

        try {
            int updated = userRepository.updateUsernameAndPasswordById(userId, trimmedUsername, encodedPassword);
            if (updated != 1) {
                // 0 means no row updated — profil utilisateur peut avoir été supprimé entre temps
                throw new RuntimeException("Aucune ligne mise à jour (userId=" + userId + ").");
            }
            log.info("Utilisateur (id={}) mis à jour via UPDATE natif (role={}, profileId={})", userId, normalizedRole, profileId);
        } catch (DataIntegrityViolationException dive) {
            log.error("Violation contrainte lors de la mise à jour native de l'utilisateur", dive);
            throw new RuntimeException("Impossible de sauvegarder l'utilisateur : contrainte violée (username peut-être déjà utilisé)");
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la mise à jour native de l'utilisateur", e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur");
        }
    }

    // findUserIdByProfile() : inchangé — copie-le depuis ta version initiale
    private Long findUserIdByProfile(Long profileId, String normalizedRole) {
        switch (normalizedRole) {
            case "ETUDIANT": {
                Student s = studentRepository.findById(profileId)
                        .orElseThrow(() -> new RuntimeException("Étudiant non trouvé (id=" + profileId + ")"));
                if (s.getUser() == null) throw new RuntimeException("Utilisateur manquant pour l'étudiant");
                return s.getUser().getId();
            }
            case "PROFESSEUR": {
                Teacher t = teacherRepository.findById(profileId)
                        .orElseThrow(() -> new RuntimeException("Professeur non trouvé (id=" + profileId + ")"));
                if (t.getUser() == null) throw new RuntimeException("Utilisateur manquant pour le professeur");
                return t.getUser().getId();
            }
            case "ADMIN": {
                Admin a = adminRepository.findById(profileId)
                        .orElseThrow(() -> new RuntimeException("Administrateur non trouvé (id=" + profileId + ")"));
                if (a.getUser() == null) throw new RuntimeException("Utilisateur manquant pour l'administrateur");
                return a.getUser().getId();
            }
            case "SURVEILLANT": {
                Surveillant sv = surveillantRepository.findById(profileId)
                        .orElseThrow(() -> new RuntimeException("Surveillant non trouvé (id=" + profileId + ")"));
                if (sv.getUser() == null) throw new RuntimeException("Utilisateur manquant pour le surveillant");
                return sv.getUser().getId();
            }
            case "RESPONSABLE": {
                Responsable r = responsableRepository.findById(profileId)
                        .orElseThrow(() -> new RuntimeException("Responsable non trouvé (id=" + profileId + ")"));
                if (r.getUser() == null) throw new RuntimeException("Utilisateur manquant pour le responsable");
                return r.getUser().getId();
            }
            default:
                throw new RuntimeException("Rôle inconnu : " + normalizedRole);
        }
    }
}
