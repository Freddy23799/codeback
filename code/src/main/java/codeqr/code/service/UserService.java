package codeqr.code.service;

import codeqr.code.model.Role;
import codeqr.code.model.Teacher;
import codeqr.code.model.TeacherYearProfile;
import codeqr.code.model.User;
import codeqr.code.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Créer un utilisateur
    public User createUser(String username, String rawPassword, Role role) {
        if(userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username déjà utilisé !");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        return userRepository.save(user);
    }
  public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    // Récupérer un utilisateur par ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Récupérer un utilisateur par username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Lister tous les utilisateurs
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Mettre à jour un utilisateur (username, password, rôle)
    public User updateUser(Long id, String username, String rawPassword, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé !"));

        if(username != null && !username.isBlank()) user.setUsername(username);
        if(rawPassword != null && !rawPassword.isBlank()) user.setPassword(passwordEncoder.encode(rawPassword));
        if(role != null) user.setRole(role);

        return userRepository.save(user);
    }

    // Supprimer un utilisateur
    public void deleteUser(Long id) {
        if(!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur non trouvé !");
        }
        userRepository.deleteById(id);
    }

    // Vérifier l'existence d'un username
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // Compter le nombre total d'utilisateurs
    public long countUsers() {
        return userRepository.count();
    }



      public TeacherYearProfile getCurrentTeacherYearProfile() {
        // 1) récupérer l'identifiant de l'utilisateur connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Utilisateur non connecté");
        }
        String username = authentication.getName().trim().toLowerCase();

        // 2) chercher le Teacher par username
        Teacher teacher = teacherRepository.findByUserUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("Aucun professeur trouvé pour l'utilisateur : " + username));

        // 3) récupérer le TeacherYearProfile
        return teacher.getTeacherYearProfiles()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun TeacherYearProfile trouvé pour le professeur : " + username));
    }









    
}
