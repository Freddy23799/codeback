package codeqr.code.dto;

public class AbsenceReportDTO {
    private Long studentId;
    private String matricule;
    private String fullName;
    private String sexe;
    private Double absentHours;
    private Integer sessionsCount;

    public AbsenceReportDTO() {}

    public AbsenceReportDTO(Long studentId, String matricule, String fullName, String sexe, Double absentHours, Integer sessionsCount) {
        this.studentId = studentId;
        this.matricule = matricule;
        this.fullName = fullName;
        this.sexe = sexe;
        this.absentHours = absentHours;
        this.sessionsCount = sessionsCount;
    }

    // getters / setters
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }
    public Double getAbsentHours() { return absentHours; }
    public void setAbsentHours(Double absentHours) { this.absentHours = absentHours; }
    public Integer getSessionsCount() { return sessionsCount; }
    public void setSessionsCount(Integer sessionsCount) { this.sessionsCount = sessionsCount; }
}
