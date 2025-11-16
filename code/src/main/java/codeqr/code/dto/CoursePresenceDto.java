// codeqr.code.dto.CoursePresenceDto.java
package codeqr.code.dto;

public class CoursePresenceDto {
    private String courseName;
    private long nbPresent;
    private long nbAbsent;

    public CoursePresenceDto(String courseName, long nbPresent, long nbAbsent) {
        this.courseName = courseName;
        this.nbPresent = nbPresent;
        this.nbAbsent = nbAbsent;
    }

    // Getters / Setters
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public long getNbPresent() { return nbPresent; }
    public void setNbPresent(long nbPresent) { this.nbPresent = nbPresent; }

    public long getNbAbsent() { return nbAbsent; }
    public void setNbAbsent(long nbAbsent) { this.nbAbsent = nbAbsent; }
}
