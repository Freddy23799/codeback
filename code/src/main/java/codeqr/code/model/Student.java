package codeqr.code.model;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String matricule;

    private String fullName; // tu peux séparer firstName/lastName si tu veux

    @Column(unique = true, nullable = false)
    private String email;

    private String avatarUrl;
 @ManyToOne(optional = false)
    @JoinColumn(name = "sexe_id")
    @JsonIgnore
    private Sexe sexe;




 
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL,fetch=FetchType.LAZY)
  @JsonIgnore
    private List<StudentYearProfile> studentYearProfiles;

    @OneToOne
        @JoinColumn(name = "user_id", nullable = false, unique = true)
 @JsonIgnore
    private User user; // suppose que User existe

    
  @Override
public String toString() {
    return "Student{id=" + id + ", name=" + fullName + "}";
}






    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
       Student other = (Student) obj;
        return Objects.equals(id, other.id);
    }
    }