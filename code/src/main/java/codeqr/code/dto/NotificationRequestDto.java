package codeqr.code.dto;

import lombok.Data;

@Data
public class NotificationRequestDto {
    private String title;        // Titre de la notif
    private String message;      // Contenu du message

    private String targetType;   // "ALL_STUDENTS", "PROFESSORS", "STUDENTS_FILTERED"

    private Long specialtyId;    // pour filtrer étudiants
    private Long levelId;
    private Long academicYearId;
}