package codeqr.code.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ProfessorTimetableResponse {

    private Long totalMatched;             // total de lignes trouvées
    private Integer returnedWeekCount;     // 0..2
    private Integer returnedTimetableCount;
    private ProfessorDto profile;          // optionnel (nom, id...)
    private List<WeekGroupDto> weekGroups;

    @Data
    public static class ProfessorDto {
        private Long professorId;
        private String fullName;
        private String academicYearLabel;
        private String specialtyLabel;
        private String levelLabel;
    }

    @Data
    public static class WeekGroupDto {
        private Long weekId;         // id de SemaineEmploiTemps
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private Boolean mostRecent;
        // nouveau : groupes par specialite/niveau/annee pour cette semaine
        private List<SpecialityGroupDto> specGroups;
    }

    @Data
    public static class SpecialityGroupDto {
        private Long specialiteId;
        private String specialiteLabel;
        private Long niveauId;
        private String niveauLabel;
        private Long anneeAcademiqueId;
        private String anneeAcademiqueLabel;
        private List<TimetableCardDto> timetables;
    }

    @Data
    public static class TimetableCardDto {
        private Long id;
        private String clientId;
        private String title;
        private String status;
        private OffsetDateTime createdAt;
        private Long semaineId;
        private String responsableName;
        private List<LineDto> rows;
        private String notes;
        // nouveau : echo des ids/labels du groupe
        private Long specialiteId;
        private String specialiteLabel;
        private Long niveauId;
        private String niveauLabel;
        private Long anneeAcademiqueId;
        private String anneeAcademiqueLabel;
    }

    @Data
    public static class LineDto {
        private String jour;
        private String start;
        private String end;
        private Long courseId;
        private String courseName;
        private Long professorId;
        private Long roomId;
        private String roomName;
        private Integer ordreSmall;
        // nouveau : chaque ligne indique à quel specialite/niveau/annee elle appartient
        private Long specialiteId;
        private Long niveauId;
        private Long anneeAcademiqueId;
    }
}
