package codeqr.code.dto;

import java.time.LocalDate;

public class ConflictDto {
    private int candidateIndex;
    private String type; // "PROF" or "ROOM"
    private Long existingLigneId;
    private Long existingEmploiId;
    private Long semaineId;
    private LocalDate semaineDateDebut;
    private Long existingResponsibleId;
    private String responsibleName;
    private Long existingProfessorId;
    private String professorName;
    private Long existingRoomId;
    private String existingRoomName; // <-- nouveau champ
    private String existingStart;
    private String existingEnd;
    private String message;

    public ConflictDto() {}

    public int getCandidateIndex() { return candidateIndex; }
    public void setCandidateIndex(int candidateIndex) { this.candidateIndex = candidateIndex; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getExistingLigneId() { return existingLigneId; }
    public void setExistingLigneId(Long existingLigneId) { this.existingLigneId = existingLigneId; }

    public Long getExistingEmploiId() { return existingEmploiId; }
    public void setExistingEmploiId(Long existingEmploiId) { this.existingEmploiId = existingEmploiId; }

    public Long getSemaineId() { return semaineId; }
    public void setSemaineId(Long semaineId) { this.semaineId = semaineId; }

    public LocalDate getSemaineDateDebut() { return semaineDateDebut; }
    public void setSemaineDateDebut(LocalDate semaineDateDebut) { this.semaineDateDebut = semaineDateDebut; }

    public Long getExistingResponsibleId() { return existingResponsibleId; }
    public void setExistingResponsibleId(Long existingResponsibleId) { this.existingResponsibleId = existingResponsibleId; }

    public String getResponsibleName() { return responsibleName; }
    public void setResponsibleName(String responsibleName) { this.responsibleName = responsibleName; }

    public Long getExistingProfessorId() { return existingProfessorId; }
    public void setExistingProfessorId(Long existingProfessorId) { this.existingProfessorId = existingProfessorId; }

    public String getProfessorName() { return professorName; }
    public void setProfessorName(String professorName) { this.professorName = professorName; }

    public Long getExistingRoomId() { return existingRoomId; }
    public void setExistingRoomId(Long existingRoomId) { this.existingRoomId = existingRoomId; }

    public String getExistingRoomName() { return existingRoomName; }
    public void setExistingRoomName(String existingRoomName) { this.existingRoomName = existingRoomName; }

    public String getExistingStart() { return existingStart; }
    public void setExistingStart(String existingStart) { this.existingStart = existingStart; }

    public String getExistingEnd() { return existingEnd; }
    public void setExistingEnd(String existingEnd) { this.existingEnd = existingEnd; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
