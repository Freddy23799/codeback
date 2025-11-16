package codeqr.code.dto;

import lombok.Data;

@Data
public class StudentYearProfileDTO {
    private Long id;
    private boolean active;

    private Long studentId;

    private Long academicYearId;
    private String academicYearLabel;

    private Long levelId;
    private String levelName;

    private Long specialtyId;
    private String specialtyName;
}
