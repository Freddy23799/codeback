package codeqr.code.controller;

import codeqr.code.model.Notification;
// import codeqr.code.model.Role;
import codeqr.code.model.User;
import codeqr.code.repository.NotificationRepository;
import codeqr.code.security.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professeur")
public class ProfesseurController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/notifications")
    @PreAuthorize("hasRole('PROFESSEUR')")
    public List<Notification> getMesNotifications(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        return notificationRepository.findByDestinataire(user);
    }
}