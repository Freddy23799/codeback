package codeqr.code.model;
import jakarta.persistence.*;
import lombok.Data;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.List;


@Data



@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"teacher_id", "academicYear_id"})})
public class TeacherYearProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academicYear_id")
    private AcademicYear academicYear;

    private boolean active = true;

    // @OneToMany(mappedBy = "teacherYearProfile")
    // private List<Course> courses;

    // @OneToMany(mappedBy = "teacherYearProfile")
    // private List<Session> sessions;

    // Getters / Setters
}