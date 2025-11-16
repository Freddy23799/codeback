package codeqr.code.dto;

public class PreviewEnrollmentDTO {
    public Long studentYearProfileId; // id du profil (utilisé comme enrollment)
    public String specialtyName;
    public String levelName;
    public Integer sessionCount; // nombre de sessions (via attendance) pour ce profil
    public String lastAttendanceAt; // iso string (optionnel)
}
