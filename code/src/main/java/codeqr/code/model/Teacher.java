package codeqr.code.model;
// import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Data;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.List;


@Data


@Entity
public class Teacher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String matricule;

    private String fullName;

    @Column(unique = true)
    private String email;

 @ManyToOne(optional = false)
    @JoinColumn(name = "sexe_id")
    @JsonIgnore
    private Sexe sexe;


@ManyToMany
@JoinTable(
    name = "teacher_courses",
    joinColumns = @JoinColumn(name = "teacher_id"),
    inverseJoinColumns = @JoinColumn(name = "course_id")
)
private List<Course> courses;


    @OneToMany(mappedBy = "teacher",fetch=FetchType.LAZY)
    private List<TeacherYearProfile> teacherYearProfiles;
 @OneToOne(fetch=FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name="user_id",nullable=false,unique=true)
    private User user;


    // Getters / Setters
}
