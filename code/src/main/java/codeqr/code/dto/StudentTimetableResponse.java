package codeqr.code.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class StudentTimetableResponse {

    /**
     * nombre total d'EmploiTemps correspondant au trio (année, niveau, spécialité)
     * (avant limitation aux 2 dernières semaines)
     */
    private Long totalMatched;

    /** nombre de semaines renvoyées (devrait être 0..2) */
    private Integer returnedWeekCount;

    /** nombre total d'emplois renvoyés (tous groupes confondus) */
    private Integer returnedTimetableCount;

    private ProfileDto profile;

    /**
     * Groupement par semaine. La semaine la plus récente doit être en position 0 côté service (ordre desc).
     */
    private List<WeekGroupDto> weekGroups;

    /* ----- nested DTOs ----- */

    @Data
    public static class ProfileDto {
        private Long academicYearId;
        private String academicYearLabel;
        private Long levelId;
        private String levelLabel;
        private Long specialtyId;
        private String specialtyLabel;
    }

    @Data
    public static class WeekGroupDto {
        /** id de la table semaine (peut être null pour emplois orphelins) */
        private Long weekId;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        /** true pour le groupe le plus récent (utile pour badge) */
        private Boolean mostRecent;
        /** timetables appartenant à cette semaine (ordre : plus récent -> moins récent si appliqué côté service) */
        private List<TimetableCardDto> timetables;
    }

    @Data
    public static class TimetableCardDto {
        private Long id;
        private String clientId;
        private String title;
        private String status;
        private OffsetDateTime createdAt;
        /** facultatif : id de la semaine si utile côté front */
        private Long semaineId;
        private String responsableName; // fullName du responsable (créateur via semaine.createdBy -> Responsable)
        private List<LineDto> rows;
        private String notes;
    }

    @Data
    public static class LineDto {
        private String jour;
        private String start;
        private String end;
        private Long courseId;
        private String courseName;
        private Long professorId;
        private String professorName;
        private Long roomId;
        private String roomName;
    }
}
