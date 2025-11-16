package codeqr.code.model;
import jakarta.persistence.*;
import lombok.Data;

// import java.time.LocalDate;
// import java.time.LocalDateTime;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Data


@Entity
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer capacity;

    @ManyToOne
    @JoinColumn(name = "campus_id", nullable = false)
    private Campus campus;

    @OneToMany(mappedBy = "room",fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Session> sessions;

    // Getters / Setters
}