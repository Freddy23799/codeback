package codeqr.code.controller;

import codeqr.code.model.User;
import codeqr.code.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

/**
 * Contrôleur minimal : vérifier et enregistrer l'acceptation de la confidentialité
 * en utilisant le username au lieu du userId.
 */
@RestController
@RequestMapping("/api/confidentialite")
public class ConfidentialiteController {

    private final UserRepository userRepository;

    public ConfidentialiteController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * GET /api/confidentialite/check/{username}
     * Retourne { "accepted": true } si l'utilisateur a déjà accepté (privacyPolicyAcceptedAt != null).
     */
    @GetMapping("/check/{username}")
    public ResponseEntity<PrivacyCheckResponse> checkConfidentialite(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new PrivacyCheckResponse(false));
        }

        User user = userOpt.get();
        boolean accepted = user.getPrivacyPolicyAcceptedAt() != null;

        return ResponseEntity.ok(new PrivacyCheckResponse(accepted));
    }

    /**
     * POST /api/confidentialite/accept/{username}
     * Enregistre l'acceptation de la confidentialité pour l'utilisateur.
     */
    @PostMapping("/accept/{username}")
    public ResponseEntity<AcceptResponse> acceptConfidentialite(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest().body(new AcceptResponse(false, "Utilisateur non trouvé"));
        }

        User user = userOpt.get();
        // Écrase systématiquement la valeur précédente (même si elle est null)
        user.setPrivacyPolicyAcceptedAt(Instant.now());

        userRepository.save(user); // Hibernate fera l'UPDATE

        return ResponseEntity.ok(new AcceptResponse(true, "Acceptation enregistrée"));
    }

    /* ----- DTOs internes simples pour réponses JSON ----- */

    public static class PrivacyCheckResponse {
        private boolean accepted;

        public PrivacyCheckResponse() { }

        public PrivacyCheckResponse(boolean accepted) {
            this.accepted = accepted;
        }

        public boolean isAccepted() {
            return accepted;
        }

        public void setAccepted(boolean accepted) {
            this.accepted = accepted;
        }
    }

    public static class AcceptResponse {
        private boolean success;
        private String message;

        public AcceptResponse() { }

        public AcceptResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
