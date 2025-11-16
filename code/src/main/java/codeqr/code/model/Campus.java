package codeqr.code.model;

import jakarta.persistence.*;
import lombok.Data;

// import java.time.LocalDate;
// import java.time.LocalDateTime;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Data

@Entity
public class Campus {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String address;

    @OneToMany(mappedBy = "campus",fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Room> rooms;

    @OneToMany(mappedBy = "campus",fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Session> sessions;

    // Getters / Setters
}