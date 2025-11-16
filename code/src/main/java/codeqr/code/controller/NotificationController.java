

package codeqr.code.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.google.firebase.messaging.FirebaseMessagingException;

import codeqr.code.dto.*;
import codeqr.code.dto2.NotificationDTO;
import codeqr.code.model.Notification;
import codeqr.code.model.User;
import codeqr.code.security.service.CustomUserDetails;
import codeqr.code.service.NotificationService;
import codeqr.code.repository.UserRepository;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserRepository userRepository;


  
 @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequestDto dto) {
        int count = notificationService.sendNotification(dto);
        return ResponseEntity.ok().body(
                java.util.Map.of("message", "Notification envoyée à " + count + " utilisateurs")
        );
    }
    











    // Prévisualise le nombre d'utilisateurs qui recevraient la notification
    @PostMapping("/preview-count")
    public ResponseEntity<?> previewCount(@RequestBody NotificationRequestDto dto) {
        int count = notificationService.previewCount(dto);
        return ResponseEntity.ok().body(
                java.util.Map.of("count", count)
        );
    }
    // Récupère toutes les notifications du destinataire connecté
    @GetMapping("/me")
    public List<NotificationDTO> getMyNotifications(Authentication authentication) {
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        List<Notification> notifications = notificationService.findByDestinataire(user);

        return notifications.stream()
                .map(n -> new NotificationDTO(
                        n.getId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.isRead(),
                        n.getCreatedAt(),
                        n.getDestinataire().getId(),
                        n.getDestinataire().getUsername(),
                        n.getDestinataire().getRole().name()
                ))
                .toList();
    }

    // Compte les notifications non lues
    @GetMapping("/unread-count")
    public Long getUnreadCount(Authentication authentication) {
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        return notificationService.countUnread(user);
    }

    // Marque une notification comme lue
    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    // Supprime une notification
    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
    }

    // Marque toutes les notifications comme lues
    @PostMapping("/mark-all-read")
    @Transactional
    public ResponseEntity<Map<String, Integer>> markAllRead(Authentication authentication) {
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok(Map.of("updated", 1));
    }






    @GetMapping("/unread/{username}")
    public List<NotificationDT> getUnreadNotifications(@PathVariable String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return List.of();
        return notificationService.getUnreadNotifications(user);
    }
    
}









