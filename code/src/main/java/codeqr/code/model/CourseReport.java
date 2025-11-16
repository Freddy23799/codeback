package codeqr.code.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class CourseReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name="studentYearProfile_id") // lien vers l'étudiant pour une année spécifique
    private StudentYearProfile studentYearProfile;

    @ManyToOne(optional = false)
    @JoinColumn(name="course_id") // lien vers le cours
    private Course course;
}