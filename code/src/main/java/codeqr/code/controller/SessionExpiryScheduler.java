package codeqr.code.controller;

import codeqr.code.repository.*;

import codeqr.code.service.*;
import codeqr.code.model.*;
import jakarta.transaction.Transactional;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class SessionExpiryScheduler {

    private final SessionRepository sessionRepository;
    private final StudentYearProfileRepository studentYearProfileRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<Session> expiredSessions = sessionRepository.findByExpiryTimeBeforeAndNotifiedFalse(now);

        for (Session session : expiredSessions) {
            // Récupérer profils attendus
            List<StudentYearProfile> profiles = studentYearProfileRepository
                .findByLevelIdAndSpecialtyIdAndAcademicYearId(
                    session.getExpectedLevel().getId(),
                    session.getExpectedSpecialty().getId(),
                    session.getAcademicYear().getId()
                );

            // Construire la liste des Users destinataires (skip si user null)
            List<User> destinataires = profiles.stream()
                .map(StudentYearProfile::getStudent)
                .filter(Objects::nonNull)
                .map(Student::getUser)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

            // Envoi notifications
            notificationService.notifyAbsences(session, destinataires);

            // Marquer la session notifiée (pour éviter les doublons)
            session.setNotified(true);
            sessionRepository.save(session);
        }
    }
}





