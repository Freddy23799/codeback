



package codeqr.code.controller;

import codeqr.code.service.PushNotification;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*") // Autorise les appels depuis ton frontend
public class NotificationsController {

    private final PushNotification pushNotification;

    public NotificationsController(PushNotification pushNotification) {
        this.pushNotification = pushNotification;
    }

    /**
     * Envoie une notification FCM à un utilisateur spécifique
     * @param fcmToken token FCM de l'utilisateur
     * @param title titre de la notification
     * @param body message de la notification
     * @param userId ID de l'utilisateur destinataire
     */
    @PostMapping("/send/fcm")
    public ResponseEntity<String> sendNotificationDirect(
            @RequestParam String fcmToken,
            @RequestParam String title,
            @RequestParam String body,
            @RequestParam String userId
    ) {
        try {
            // Générer un ID unique pour la notification
            String notifId = "notif-" + System.currentTimeMillis();

            // Envoyer la notification
            pushNotification.sendNotificationToUser(fcmToken, title, body, notifId, userId);

            return ResponseEntity.ok("✅ Notification envoyée avec succès (ID: " + notifId + ")");
        } catch (FirebaseMessagingException e) {
            return ResponseEntity.status(500).body("❌ Erreur FCM : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Erreur interne : " + e.getMessage());
        }
    }
}
