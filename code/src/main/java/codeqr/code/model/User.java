
package codeqr.code.model;
// import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import codeqr.code.model.*;
@Data
@Entity

@Table(name = "app_user")
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fcmToken;
@Column(unique=true,nullable=false,length=100)
      private String username;



     

      @Column(nullable=false)
        private String password;
@Enumerated(EnumType.STRING)
    @Column(nullable=false)
  private Role role;


  @OneToOne(mappedBy="user",cascade=CascadeType.ALL,fetch=FetchType.LAZY)
 
    private Teacher teacher;
      @OneToOne(mappedBy="user",cascade=CascadeType.ALL,fetch=FetchType.LAZY)
 
    private Surveillant surveillant;



    @OneToOne(mappedBy="user",cascade=CascadeType.ALL,fetch=FetchType.LAZY)
 
    private Responsable responsable;
       @OneToOne(mappedBy="user",cascade=CascadeType.ALL,fetch=FetchType.LAZY)


       
    private Admin admin;

  @OneToOne(mappedBy="user",cascade=CascadeType.ALL,fetch=FetchType.LAZY)
 
    private Student student;

     @OneToMany(mappedBy = "destinataire", cascade = CascadeType.ALL, orphanRemoval = true,fetch=FetchType.LAZY)
      @JsonIgnore
    private List<Notification> notifications = new ArrayList<>();

 

  @Column(name = "privacy_policy_accepted_at")
  private Instant privacyPolicyAcceptedAt;

  
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User other = (User) obj;
        return Objects.equals(id, other.id);
    }
}
