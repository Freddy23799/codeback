





package codeqr.code.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codeqr.code.dto.DashboardSummaryDTO;
import codeqr.code.dto.NotificationDTO;
import codeqr.code.dto.StatsDTO;
import codeqr.code.model.Attendance;
import codeqr.code.model.StudentYearProfile;
import codeqr.code.model.Student;
import codeqr.code.model.Notification;
import codeqr.code.repository.AttendanceRepository;
import codeqr.code.repository.StudentRepository;
import codeqr.code.repository.StudentYearProfileRepository;
import codeqr.code.repository.NotificationRepository;
import codeqr.code.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final StudentRepository studentRepository;
    private final StudentYearProfileRepository studentYearProfileRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationRepository notificationRepository;
@Cacheable(cacheNames = "studentDashboard", key = "#studentId")
    public DashboardSummaryDTO getDashboardForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

        List<StudentYearProfile> profiles = studentYearProfileRepository.findByStudentId(studentId);
        List<Long> profileIds = profiles.stream().map(StudentYearProfile::getId).collect(Collectors.toList());

        long totalCourses = 0L;
        long attended = 0L;
        long absent = 0L;

        if (!profileIds.isEmpty()) {
            totalCourses = attendanceRepository.countByStudentYearProfile_IdIn(profileIds);
            attended     = attendanceRepository.countByStudentYearProfile_IdInAndStatus(profileIds, Attendance.Status.PRESENT);

            // IMPORTANT : PENDING est considéré comme ABSENT => on compte ABSENT + PENDING
            List<Attendance.Status> absentStatuses = List.of(Attendance.Status.ABSENT, Attendance.Status.PENDING);
            absent = attendanceRepository.countByStudentYearProfile_IdInAndStatusIn(profileIds, absentStatuses);
        }

        StatsDTO stats = StatsDTO.builder()
                .totalCourses(totalCourses)
                .attended(attended)
                .absent(absent)
                .unreadNotifications(0L)
                .build();

        Long userId = student.getUser() != null ? student.getUser().getId() : null;
        List<NotificationDTO> notifDtos = List.of();
        long unreadCount = 0L;
        if (userId != null) {
            unreadCount = notificationRepository.countByDestinataire_IdAndReadFalse(userId);
            var notifications = notificationRepository.findTop10ByDestinataire_IdOrderByCreatedAtDesc(userId);
            notifDtos = notifications.stream().map(this::toDto).collect(Collectors.toList());
        }

        stats.setUnreadNotifications(unreadCount);

        return DashboardSummaryDTO.builder()
                .stats(stats)
                .notifications(notifDtos)
                .build();
    }

    private NotificationDTO toDto(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
