package codeqr.code.model;
import jakarta.persistence.*;
import lombok.Data;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
// import com.fasterxml.jackson.annotation.JsonManagedReference;

@Data
@Entity
public class Level {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "level",fetch = FetchType.LAZY)
@JsonIgnore
    private List<StudentYearProfile> studentYearProfiles;

}