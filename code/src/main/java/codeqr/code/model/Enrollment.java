package codeqr.code.model;
// import java.util.ArrayList;
// import java.util.List;

// import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
// import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
// import lombok.Getter;
// import lombok.Setter;


@Data




@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"studentYearProfile_id"})})
public class Enrollment {
   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lien vers le profil étudiant
    @ManyToOne(optional = false)
    @JoinColumn(name = "studentYearProfile_id")

    private StudentYearProfile studentYearProfile;

    // Toutes les séances correspondant à ce profil (temporaire pour front)
   
    public Object getTeacherYearProfile() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTeacherYearProfile'");
    }

    public void setTeacherYearProfile(Object teacherYearProfile) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setTeacherYearProfile'");
    }

    public Object getSession() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSession'");
    }

    public void setSession(Object session) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSession'");
    }

    // Getters / Setters
}