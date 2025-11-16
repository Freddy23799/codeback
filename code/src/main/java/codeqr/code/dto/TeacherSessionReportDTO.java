package codeqr.code.dto;

import java.time.*;
import java.util.List;

public class TeacherSessionReportDTO {

    private Long teacherId;
    private String teacherName;
    private String teacherEmail;
    private String teacherGender;
    private String teacherMatricule;

    // Remplacé : teacherMatiere a été aboli -> on expose la liste des cours
    private List<CourseDO> teacherCourses;

    private List<SessionDTO> sessions;

    // --- Getters & Setters ---
    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherMatricule() {
        return teacherMatricule;
    }

    public void setTeacherMatricule(String teacherMatricule) {
        this.teacherMatricule = teacherMatricule;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public void setTeacherEmail(String teacherEmail) {
        this.teacherEmail = teacherEmail;
    }

    public String getTeacherGender() {
        return teacherGender;
    }

    public void setTeacherGender(String teacherGender) {
        this.teacherGender = teacherGender;
    }

    public List<SessionDTO> getSessions() {
        return sessions;
    }

    public void setSessions(List<SessionDTO> sessions) {
        this.sessions = sessions;
    }

    public List<CourseDO> getTeacherCourses() {
        return teacherCourses;
    }

    public void setTeacherCourses(List<CourseDO> teacherCourses) {
        this.teacherCourses = teacherCourses;
    }

   // ==========================
   //   INNER CLASS CourseDTO
   // ==========================
   public static class CourseDTO {
       private Long id;
       private String code;
       private String title;

       public CourseDTO() {}

       public CourseDTO(Long id, String code, String title) {
           this.id = id;
           this.code = code;
           this.title = title;
       }

       public Long getId() { return id; }
       public void setId(Long id) { this.id = id; }

       public String getCode() { return code; }
       public void setCode(String code) { this.code = code; }

       public String getTitle() { return title; }
       public void setTitle(String title) { this.title = title; }
   }

   // ==========================
   //   INNER CLASS SessionDTO
   // ==========================
    public static class SessionDTO {
        private Long sessionId;
        private String courseTitle;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String campusName;
        private String roomName;
        private String specialtyName;
        private String levelName;
        private String academicYearLabel;
        private List<StudentDTO> students;

        // --- Getters & Setters ---
        public Long getSessionId() {
            return sessionId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }

        public String getCourseTitle() {
            return courseTitle;
        }

        public void setCourseTitle(String courseTitle) {
            this.courseTitle = courseTitle;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public String getCampusName() {
            return campusName;
        }

        public void setCampusName(String campusName) {
            this.campusName = campusName;
        }

        public String getRoomName() {
            return roomName;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public String getSpecialtyName() {
            return specialtyName;
        }

        public void setSpecialtyName(String specialtyName) {
            this.specialtyName = specialtyName;
        }

        public String getLevelName() {
            return levelName;
        }

        public void setLevelName(String levelName) {
            this.levelName = levelName;
        }

        public String getAcademicYearLabel() {
            return academicYearLabel;
        }

        public void setAcademicYearLabel(String academicYearLabel) {
            this.academicYearLabel = academicYearLabel;
        }

        public List<StudentDTO> getStudents() {
            return students;
        }

        public void setStudents(List<StudentDTO> students) {
            this.students = students;
        }
    }

    // ==========================
    //    INNER CLASS StudentDTO
    // ==========================
    public static class StudentDTO {
        private Long studentId;
        private String studentName;
        private String studentMatricule;
        private String status; // PRESENT, ABSENT, PENDING

        // --- Getters & Setters ---
        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public String getStudentMatricule() {
            return studentMatricule;
        }

        public void setStudentMatricule(String studentMatricule) {
            this.studentMatricule = studentMatricule;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
