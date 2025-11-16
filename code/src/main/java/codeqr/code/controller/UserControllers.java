package codeqr.code.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import codeqr.code.model.User;
import codeqr.code.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserControllers {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register-fcm-token")
    public ResponseEntity<String> registerFcmToken(@RequestParam String username, @RequestParam String fcmToken) {
        Optional<User> userOpt = userRepository.findByUsername(username); // recherche par username
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur non trouvé");
        }

        User user = userOpt.get();
        user.setFcmToken(fcmToken);
        userRepository.save(user);

        return ResponseEntity.ok("Token FCM enregistré pour " + username);
    }
}
