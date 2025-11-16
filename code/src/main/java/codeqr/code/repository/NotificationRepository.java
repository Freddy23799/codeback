

package codeqr.code.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codeqr.code.model.Notification;
import codeqr.code.model.User;
import jakarta.transaction.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Notifications d'un destinataire, triées par date
    List<Notification> findByDestinataireOrderByCreatedAtDesc(User destinataire);

    // Compte les notifications non lues pour un destinataire
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.destinataire.id = :userId AND n.read = false")
    Long countUnreadByDestinataire(@Param("userId") Long userId);
 long countByDestinataire_IdAndReadFalse(Long destinataireUserId);
    List<Notification> findTop10ByDestinataire_IdOrderByCreatedAtDesc(Long destinataireUserId);
    // Marque toutes les notifications d'un destinataire comme lues
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.destinataire = :user")
    int markAllAsReadByUser(@Param("user") User user);
    List<Notification> findByDestinataireAndReadFalse(User destinataire);
    public List<Notification> findByDestinataire(User user);
}
