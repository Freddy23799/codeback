package codeqr.code.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class AcademicYear {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    // Getters / Setters
}