package codeqr.code.model;
// import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.*;




@Data
@Entity
public class Session {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



 


    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

  



   private LocalDateTime expiryTime;

 private Boolean notified=false;
    @ManyToOne(optional = false)
    @JoinColumn(name = "academicYear_id")
    private AcademicYear academicYear;

    @ManyToOne(optional = false)
    @JoinColumn(name = "campus_id")
    private Campus campus;


    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(unique = true,columnDefinition = "TEXT")
    private String qrToken;

 @Lob
    @Column(columnDefinition = "TEXT") // permet de stocker du JSON
    private String qrPayload;

    // Méthodes utilitaires pour gérer le payload
    public void setQrPayloadFromMap(Map<String, Object> payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.qrPayload = mapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la conversion du payload en JSON", e);
        }
    }

    public Map<String, Object> getQrPayloadAsMap() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.qrPayload, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la lecture du payload JSON", e);
        }
    }

   private  Boolean  Closed= true;



private LocalDateTime  Created;



    @ManyToOne(optional = false)
    @JoinColumn(name = "expectedLevel_id")
    private Level expectedLevel;


    @ManyToOne(optional = true)
    @JoinColumn(name = "teacher_year_profile_id", nullable=true)
    private TeacherYearProfile teacherYearProfile;

  @ManyToOne(optional = true)
    @JoinColumn( nullable=true)
    private Surveillant surveillant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "expectedSpecialty_id")
    private Specialty expectedSpecialty;

    @OneToMany(mappedBy = "session",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.LAZY)
    @JsonIgnore
    private List<Attendance> attendances;

    // Getters / Setters
}