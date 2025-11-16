package codeqr.code.model;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
// import java.util.List;


@Data

@Entity
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne(optional = false)
    // @JoinColumn(name = "studentYearProfile_id")
    // private StudentYearProfile studentYearProfile;

    private String title;
    
    @Column(columnDefinition="TEXT")

    private String message;
@Column(name = "is_read")
    private boolean read = false;

    private LocalDateTime createdAt;
@ManyToOne(fetch=FetchType.LAZY)
@JoinColumn(name="destinataire_user_id",nullable=false)
private User destinataire;
    // Getters / Setters
    @ManyToOne
    private User user; 
}