package codeqr.code.model;
// import java.util.ArrayList;
import java.util.List;

// import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.List;


@Data


@Entity
public class Surveillant {
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




    
 @OneToOne(fetch=FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name="user_id",nullable=false,unique=true)
    private User user;


    // Getters / Setters
}
