
// codeqr.code.dto.DashboardDto.java
package codeqr.code.dto;
import codeqr.code.dto.*;
import java.util.List;

public class DashboardDto {
    private long totalSessions;
    private long totalStudents;
    private double avgPresence;
    private String mostAttendedCourse;
    private List<CoursePresenceDto> coursePresence;

    // Getters / Setters / Constructeurs
    public long getTotalSessions() { return totalSessions; }
    public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }

    public double getAvgPresence() { return avgPresence; }
    public void setAvgPresence(double avgPresence) { this.avgPresence = avgPresence; }

    public String getMostAttendedCourse() { return mostAttendedCourse; }
    public void setMostAttendedCourse(String mostAttendedCourse) { this.mostAttendedCourse = mostAttendedCourse; }

    public List<CoursePresenceDto> getCoursePresence() { return coursePresence; }
    public void setCoursePresence(List<CoursePresenceDto> coursePresence) { this.coursePresence = coursePresence; }
}
