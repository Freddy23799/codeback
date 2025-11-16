package codeqr.code.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.*;
@Data
public class CreateSessionRequest {
    private Long courseId;
    private Long teacherYearProfileId;
    private Long academicYearId;
    private Long campusId;
    private Long roomId;
    private String username;
      private Long professorId;   // id du professeur sélectionné
    private Long surveillantId; 
    // @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-mm-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    // @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-mm-dd'T'HH:mm:ss")
    
    private LocalDateTime endTime;
    private Long expectedLevelId;
    private Long expectedSpecialtyId;

    // getters / setters
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getTeacherYearProfileId() { return teacherYearProfileId; }
    public void setTeacherYearProfileId(Long teacherYearProfileId) { this.teacherYearProfileId = teacherYearProfileId; }
    public Long getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(Long academicYearId) { this.academicYearId = academicYearId; }
    public Long getCampusId() { return campusId; }
    public void setCampusId(Long campusId) { this.campusId = campusId; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Long getExpectedLevelId() { return expectedLevelId; }
    public void setExpectedLevelId(Long expectedLevelId) { this.expectedLevelId = expectedLevelId; }
    public Long getExpectedSpecialtyId() { return expectedSpecialtyId; }
    public void setExpectedSpecialtyId(Long expectedSpecialtyId) { this.expectedSpecialtyId = expectedSpecialtyId; }
    public void setUserId(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setUserId'");
    }
}
