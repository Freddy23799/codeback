package codeqr.code.repository;

import codeqr.code.dto.AttendanceHistoryRowDTO;
import codeqr.code.model.Attendance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Projection interne pour la query JPA.
 * ⚠️ doit être PUBLIC pour que Hibernate puisse instancier via JPQL "new ..."
 */
public class RowProjection {
    private final Long id;
    private final String courseTitle;
    private final LocalDateTime sessionStart;
    private final String campusName;
    private final String roomName;
    private final String teacherName;
    private final Attendance.Status status;

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEEE", Locale.FRENCH);

    public RowProjection(Long id, String courseTitle, LocalDateTime sessionStart,
                         String campusName, String roomName, String teacherName, Attendance.Status status) {
        this.id = id;
        this.courseTitle = courseTitle;
        this.sessionStart = sessionStart;
        this.campusName = campusName;
        this.roomName = roomName;
        this.teacherName = teacherName;
        this.status = status;
    }

    public AttendanceHistoryRowDTO toDto() {
        String day = sessionStart != null ? DAY.format(sessionStart).toLowerCase(Locale.FRENCH) : "";
        return new AttendanceHistoryRowDTO(
                id,
                courseTitle,
                sessionStart,
                day,
                campusName,
                roomName,
                teacherName,
                status != null ? status.name() : null
        );
    }
}
