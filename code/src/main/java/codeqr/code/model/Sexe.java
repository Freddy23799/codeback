package codeqr.code.model;

import jakarta.persistence.*;
import lombok.Data;

// import java.time.LocalDate;
// import java.time.LocalDateTime;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Data

@Entity
public class Sexe {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;



    @OneToMany(mappedBy = "sexe",fetch=FetchType.LAZY)
    @JsonIgnore
    private List<Student> students;

    @OneToMany(mappedBy = "sexe",fetch=FetchType.LAZY)
    @JsonIgnore
    private List<Admin> admins;

    // Getters / Setters
     @OneToMany(mappedBy = "sexe", fetch=FetchType.LAZY)
    @JsonIgnore
    private List<Teacher> teachers;

    // Getter
    @Override
public String toString() {
    return "Sexe{id=" + id + ", name=" + name + "}";
}

}