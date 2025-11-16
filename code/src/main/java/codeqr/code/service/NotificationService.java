
package codeqr.code.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.SendResponse;

import org.springframework.cache.annotation.Cacheable;
import codeqr.code.dto.*;
import codeqr.code.model.*;
import codeqr.code.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository repo;
     private final TeacherRepository teacherRepository;
 private final StudentRepository studentRepository;


   private final UserRepository userRepository;
   private final PushNotification pushNotification;
   
    public NotificationService(NotificationRepository repo ,PushNotification pushNotification, TeacherRepository teacherRepository , StudentRepository studentRepository,UserRepository userRepository) {
        this.repo = repo;
        this.pushNotification = pushNotification;
         this.studentRepository= studentRepository;
         this.teacherRepository =teacherRepository;
          this.userRepository =userRepository;

    }
   @Transactional
   public int sendNotification(NotificationRequestDto dto) {
    List<User> targets;

    switch (dto.getTargetType()) {
        case "ALL_STUDENTS":
            targets = userRepository.findByRole(Role.ETUDIANT);
            break;

        case "PROFESSORS":
            targets = userRepository.findByRole(Role.PROFESSEUR);
            break;

        case "STUDENTS_FILTERED":
            targets = userRepository.findStudentsByYearProfile(
                    dto.getSpecialtyId(),
                    dto.getLevelId(),
                    dto.getAcademicYearId()
            );
            break;

        default:
            throw new IllegalArgumentException("TargetType invalide : " + dto.getTargetType());
    }

    for (User user : targets) {
        // Création et sauvegarde de la notification en base
        Notification notif = new Notification();
        notif.setTitle(dto.getTitle());
        notif.setMessage(dto.getMessage());
        notif.setCreatedAt(LocalDateTime.now());
        notif.setDestinataire(user);  // Utilisateur cible
        notif.setRead(false);

        repo.save(notif); // ID généré ici

        // Envoyer la notification FCM si le user a un token
        if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
            try {
                pushNotification.sendNotificationToUser(
                    user.getFcmToken(),
                    dto.getTitle(),
                    dto.getMessage(),
                    String.valueOf(notif.getId()), // ID de la notif en base
                    user.getUsername()             // ← ajouter ici
                );
            } catch (FirebaseMessagingException e) {
                System.err.println("Erreur FCM pour " + user.getUsername() + " : " + e.getMessage());
            }
        }

        // Log debug
        System.out.println("Notification enregistrée et envoyée à " + user.getUsername());
    }

    return targets.size();
}

public int previewCount(NotificationRequestDto dto) {
    switch (dto.getTargetType()) {
        case "ALL_STUDENTS":
            return userRepository.countByRole(Role.ETUDIANT);
        case "PROFESSORS":
            return userRepository.countByRole(Role.PROFESSEUR);
        case "STUDENTS_FILTERED":
            return userRepository.countStudentsByYearProfile(
                    dto.getSpecialtyId(),
                    dto.getLevelId(),
                    dto.getAcademicYearId()
            );
        default:
            return 0;
    }
}

// public int sendNotification(NotificationRequestDto dto) {
//     if (dto == null) {
//         System.err.println("dto null");
//         return 0;
//     }

//     // 1) Résoudre les targets
//     List<User> targets;
//     switch (dto.getTargetType()) {
//         case "ALL_STUDENTS":
//             targets = userRepository.findByRole(Role.ETUDIANT);
//             break;
//         case "PROFESSORS":
//             targets = userRepository.findByRole(Role.PROFESSEUR);
//             break;
//         case "STUDENTS_FILTERED":
//             targets = userRepository.findStudentsByYearProfile(
//                     dto.getSpecialtyId(),
//                     dto.getLevelId(),
//                     dto.getAcademicYearId()
//             );
//             break;
//         default:
//             throw new IllegalArgumentException("TargetType invalide : " + dto.getTargetType());
//     }

//     if (targets == null || targets.isEmpty()) {
//         System.out.println("Aucun destinataire trouvé pour targetType=" + dto.getTargetType());
//         return 0;
//     }

//     // CONFIG
//     final int CHUNK_SIZE = 500;   // max tokens FCM
//     LocalDateTime now = LocalDateTime.now();
//     int totalSaved = 0;

//     for (int offset = 0; offset < targets.size(); offset += CHUNK_SIZE) {
//         int end = Math.min(offset + CHUNK_SIZE, targets.size());
//         List<User> chunkUsers = targets.subList(offset, end);

//         // Préparer notifications
//         List<Notification> toSave = new ArrayList<>(chunkUsers.size());
//         List<String> fcmTokens = new ArrayList<>();
//         for (User user : chunkUsers) {
//             Notification notif = new Notification();
//             notif.setTitle(dto.getTitle());
//             notif.setMessage(dto.getMessage());
//             notif.setCreatedAt(now);
//             notif.setDestinataire(user);
//             notif.setRead(false);
//             toSave.add(notif);

//             if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
//                 fcmTokens.add(user.getFcmToken());
//             }
//         }

//         // Sauvegarde chunk
//         List<Notification> savedNotifications;
//         try {
//             savedNotifications = repo.saveAll(toSave);
//             try { repo.flush(); } catch (Throwable ignored) {}
//         } catch (Exception e) {
//             System.err.println("Erreur repo.saveAll chunk " + offset + "-" + end + " : " + e.getMessage());
//             continue; // skip ce chunk
//         }

//         totalSaved += savedNotifications.size();

//         // Envoi batch FCM avec la nouvelle API
//         if (!fcmTokens.isEmpty()) {
//             try {
//                 com.google.firebase.messaging.MulticastMessage message = com.google.firebase.messaging.MulticastMessage.builder()
//                         .putData("title", dto.getTitle())
//                         .putData("message", dto.getMessage())
//                         .addAllTokens(fcmTokens)
//                         .build();

//                 // Nouvelle méthode : sendEachForMulticast
//                 com.google.firebase.messaging.BatchResponse batchResponse =
//                         com.google.firebase.messaging.FirebaseMessaging.getInstance()
//                                 .sendEachForMulticast(message);

//                 long successCount = batchResponse.getResponses().stream()
//                         .filter(com.google.firebase.messaging.SendResponse::isSuccessful)
//                         .count();

//                 System.out.println("Push batch [" + offset + "-" + (end - 1) + "] envoyé : " + successCount + "/" + fcmTokens.size());
//             } catch (Exception ex) {
//                 System.err.println("Erreur FCM batch chunk " + offset + "-" + end + " : " + ex.getMessage());
//             }
//         }

//         System.out.println("Chunk traité [" + offset + "-" + (end - 1) + "], saved=" + savedNotifications.size());
//     }

//     System.out.println("sendNotification terminé, totalSaved=" + totalSaved);
//     return totalSaved;
// }












//  public int previewCount(NotificationRequestDto dto) {
//         if (dto == null) return 0;

//         List<User> targets;
//         switch (dto.getTargetType()) {
//             case "ALL_STUDENTS":
//                 targets = userRepository.findByRole(Role.ETUDIANT);
//                 break;
//             case "PROFESSORS":
//                 targets = userRepository.findByRole(Role.PROFESSEUR);
//                 break;
//             case "STUDENTS_FILTERED":
//                 targets = userRepository.findStudentsByYearProfile(
//                         dto.getSpecialtyId(),
//                         dto.getLevelId(),
//                         dto.getAcademicYearId()
//                 );
//                 break;
//             default:
//                 throw new IllegalArgumentException("TargetType invalide : " + dto.getTargetType());
//         }

//         return targets == null ? 0 : targets.size();
//     }






























    public List<Notification> findByDestinataire(User destinataire) {
        return repo.findByDestinataireOrderByCreatedAtDesc(destinataire);
    }
  public List<NotificationDT> getUnreadNotifications(User destinataire) {
    // Récupère les notifications non lues
    List<Notification> notifications = repo.findByDestinataireAndReadFalse(destinataire);

    // Transforme chaque Notification en NotificationDTO
    return notifications.stream()
            .map(n -> new NotificationDT(
                    n.getId(),
                    n.getMessage(),
                    n.isRead(),
                    n.getCreatedAt()
            ))
            .collect(Collectors.toList());
}
    public Long countUnread(User destinataire) {
        return repo.countUnreadByDestinataire(destinataire.getId());
    }

    public void markAsRead(Long notificationId) {
        Notification notif = repo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notif.setRead(true);
        repo.save(notif);
    }

    public void markAllAsRead(User user) {
        repo.markAllAsReadByUser(user);
    }

    public void deleteNotification(Long notificationId) {
        repo.deleteById(notificationId);
    }

    public Notification save(Notification notification) {
        return repo.save(notification);
    }





    public void notifyAbsences(Session session, List<User> destinataires) {
        String profDisplay = resolveUserDisplayName(session.getUser());

        for (User destinataire : destinataires) {
            String recipDisplay = resolveUserDisplayName(destinataire);

            Notification notif = new Notification();
            notif.setDestinataire(destinataire);
            notif.setTitle("Absence automatique : session expirée");

            String dateStr = session.getStartTime() != null
                ? session.getStartTime().toLocalDate().toString()
                : (session.getCreated() != null ? session.getCreated().toLocalDate().toString() : "—");

            String courseName = session.getCourse() != null ? session.getCourse().getTitle() : "cours";
            String campus = session.getCampus() != null ? session.getCampus().getName() : "campus";
            String room = session.getRoom() != null ? session.getRoom().getName() : "salle";

            String message = String.format(
                "Bonjour %s,\n\n"
              + "Vous aviez le %s avec %s le %s au %s (salle %s).\n"
              + "La session est maintenant expirée (plus de 2 heures). Vous êtes marqué(e) ABSENT(e) si vous navez pas justifier votre presence et "
              + "vous ne pouvez plus modifier cette présence.\n\nCordialement ADMINISTRATION.",
                recipDisplay, courseName, profDisplay, dateStr, campus, room
            );

            notif.setMessage(message);
            notif.setCreatedAt(LocalDateTime.now());
            // champ 'user' : on met l'auteur (ici le prof / owner de la session)
            notif.setUser(session.getUser());
notif.setRead(false);
             repo.save(notif);
        }
    }

    /**
     * Résout un nom d'affichage lisible pour un User.
     *  - si User est lié à un Student => retourne student.fullName
     *  - sinon si User est lié à un Teacher => retourne teacher.fullName
     *  - sinon fallback sur username
     */
    private String resolveUserDisplayName(User user) {
        if (user == null) return "Utilisateur";

        // 1) Student lié au User 
        Optional<Student> sOpt = studentRepository.findByUser(user);
        if (sOpt.isPresent() && sOpt.get().getFullName() != null && !sOpt.get().getFullName().isBlank()) {
            return sOpt.get().getFullName();
        }

        // 2) Teacher lié au User 
        Optional<Teacher> tOpt = teacherRepository.findByUser(user);
        if (tOpt.isPresent()) {
            // adapter selon la propriété 'fullName' du Teacher
            if (tOpt.get().getFullName() != null && !tOpt.get().getFullName().isBlank()) {
                return tOpt.get().getFullName();
            }
            // ou si Teacher a un champ firstname/lastname
          
        }

        // 3) fallback : username ou email
        // if (user.getUsername() != null && !user.getUsername().isBlank()) return user.getUsername();
        // if (user.getEmail() != null && !user.getEmail().isBlank()) return user.getEmail();

        return "Utilisateur";
    }








}
