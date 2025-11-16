package codeqr.code.model;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;


@Data





@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"student_id", "academicYear_id"})})
public class StudentYearProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
 @JsonBackReference
    private Student student;
 @Column(nullable = false)


    private boolean active ;

    @ManyToOne(optional = false)

    @JoinColumn(name = "academicYear_id")
    private AcademicYear academicYear;

    @ManyToOne(optional = false)
    @JoinColumn(name = "level_id")
 
    private Level level;


    


    @ManyToOne(optional = false)
    @JoinColumn(name = "specialty_id")
    private Specialty specialty;

    @OneToMany(mappedBy = "studentYearProfile", cascade = CascadeType.ALL,
               orphanRemoval = true)
                @JsonIgnore
    private Set<Enrollment> enrollments;

    @OneToMany(mappedBy = "studentYearProfile", cascade = CascadeType.ALL,
               orphanRemoval = true)
                @JsonIgnore
    private List<Attendance> attendances;

    // @OneToMany(mappedBy = "studentYearProfile")
    // private List<Notification> notifications;

   
    // Getters / Setters

    public void setActive(boolean active) {
    this.active = active;
}
}