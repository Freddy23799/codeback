package codeqr.code.dto;

import java.util.List;

public class TeacherLightDTO {
    public Long teacherId;
    public String teacherName;
    public String teacherEmail;
    public String teacherGender;
    public Integer sessionsCount;
    public List<SessionLightDTO> sessions; // optionnel, peut être null
}
