

package codeqr.code.service;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codeqr.code.dto.*;
import codeqr.code.model.*;
import codeqr.code.repository.AttendanceRepository;
import codeqr.code.repository.SessionRepository;
import codeqr.code.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
// --------------- Attendance Service -----------------
@Service
@Transactional
public class AttendanceService {
    private final StudentRepository studentRepository;
    private final SessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;

    public AttendanceService(StudentRepository studentRepository,
                             SessionRepository sessionRepository,
                             AttendanceRepository attendanceRepository) {
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
        this.attendanceRepository = attendanceRepository;
    }
   
    public Optional<Attendance> findById(Long id) { return attendanceRepository.findById(id); }
    public List<Attendance> findByStudentYearProfileId(Long studentYearProfileId) {
        return attendanceRepository.findByStudentYearProfile_Id(studentYearProfileId);
    }
    public Optional<Attendance> findByStudentYearProfileIdAndSessionId(Long studentYearProfileId, Long sessionId) {
        return attendanceRepository.findByStudentYearProfile_IdAndSession_Id(studentYearProfileId, sessionId);
    }
  
    









    public List<Attendance> findAll() {
        return attendanceRepository.findAll();
    }

    public Attendance save(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    public void delete(Long id) {
        attendanceRepository.deleteById(id);
    }

    public void markManual(Long sessionId, List<Long> studentIds, Attendance.Status status) {
        for (Long sid : studentIds) {
            attendanceRepository.findBySession_IdAndStudentYearProfile_Id(sessionId, sid)
                    .ifPresentOrElse(a -> {
                        a.setStatus(status);
                        attendanceRepository.save(a);
                    }, () -> {
                        // créer si absent
                        Attendance newA = new Attendance();
                        newA.setSessionId(sessionId);
                        newA.setStudentYearProfileId(sid);
                        newA.setStatus(status);
                        attendanceRepository.save(newA);
                    });
        }
    }











     public Attendance processScan(AttendanceScanRequest request) {
        // 1. Vérifier Student
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new EntityNotFoundException("Étudiant introuvable"));

        // 2. Vérifier Session
        Session session = sessionRepository.findSessionById(request.getSessionId())
                .orElseThrow(() -> new EntityNotFoundException("Session introuvable"));

        // 3. Vérifier QR Token
        if (!session.getQrToken().equals(request.getQrToken())) {
            throw new IllegalArgumentException("QR Token invalide pour cette session");
        }

        // 4. Vérifier délai de 2h
        LocalDateTime createdAt = session.getEndTime(); // Champ createdAt dans Session requis
        if (Duration.between(createdAt, request.getScannedAt()).toHours() >= 2) {
            throw new IllegalStateException("Scan refusé : délai > 2h");
        }

        // 5. Vérifier Attendance
        Attendance attendance = attendanceRepository
                .findBySessionIdAndStudentId(session.getId(), student.getId())
                .orElseThrow(() -> new EntityNotFoundException("Attendance introuvable pour cet étudiant et session"));

        // 6. Mise à jour Attendance
        attendance.setScannedAt(request.getScannedAt());
        attendance.setStatus(Attendance.Status.PRESENT);
        attendance.setSource(Attendance.Source.qr);

        return attendanceRepository.save(attendance);
    }









       public Page<Attendanceto> getAttendances(Long sessionId, String studentName, Pageable pageable) {
        Page<Attendance> page = attendanceRepository
            .findBySession_IdAndStudentYearProfileStudentFullNameContainingIgnoreCase(
                sessionId,
                studentName != null ? studentName : "",
                pageable
            );

        return page.map(a -> new Attendanceto(
            a.getId(),
            a.getStudentYearProfile().getStudent().getFullName(), // adapter selon ton modèle
            a.getStatus(),
            a.getScannedAt()
        ));
    }
}
