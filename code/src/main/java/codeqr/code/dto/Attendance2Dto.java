  package codeqr.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance2Dto {
    private Long id;
    private CourseDto course;
    private SessionDTO session;
    private String status; // PRESENT, ABSENT, PENDING
}

    