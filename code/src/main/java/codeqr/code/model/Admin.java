package codeqr.code.model;
// import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;


@Data



@Entity
public class Admin {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String matricule;

    @Column(unique = true)
    private String email;
 @OneToOne(fetch = FetchType.LAZY)
 @JsonIgnore
    @JoinColumn(name="user_id",nullable=false,unique=true)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sexe_id")
 @JsonIgnore
    private Sexe sexe;

    public Admin(String email, String fullName, Long id, Sexe sexe, User user) {
        this.email = email;
        this.fullName = fullName;
        this.id = id;
        this.sexe = sexe;
        this.user = user;
    }

    public Admin() {
        //TODO Auto-generated constructor stub
    }
}