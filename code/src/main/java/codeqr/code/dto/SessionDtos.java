
package codeqr.code.dto;

import codeqr.code.model.Session;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionDtos {
    private Long id;
    private String date;
    private String heures;
    private String courseName;
    private String levelName;
    private String specialtyName;
    private String roomName;
    private String campusName;
    private int nbPresent;
    private int nbAbsent;

    public static SessionDtos fromEntity(Session s) {
        int present = 0;
        int absent = 0;
        if (s.getAttendances() != null) {
            present = (int) s.getAttendances().stream()
                    .filter(a -> a.getStatus() == codeqr.code.model.Attendance.Status.PRESENT)
                    .count();
            absent = (int) s.getAttendances().stream()
                    .filter(a -> a.getStatus() == codeqr.code.model.Attendance.Status.ABSENT)
                    .count();
        }

        return new SessionDtos(
                s.getId(),
                s.getStartTime() != null ? s.getStartTime().toLocalDate().toString() : null,
                s.getStartTime() != null && s.getEndTime() != null
                        ? s.getStartTime().toLocalTime() + " - " + s.getEndTime().toLocalTime()
                        : "",
                s.getCourse() != null ? s.getCourse().getTitle() : null,
                s.getExpectedLevel() != null ? s.getExpectedLevel().getName() : null,
                s.getExpectedSpecialty() != null ? s.getExpectedSpecialty().getName() : null,
                s.getRoom() != null ? s.getRoom().getName() : null,
                s.getCampus() != null ? s.getCampus().getName() : null,
                present,
                absent
        );
    }
}
