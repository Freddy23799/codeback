package codeqr.code.dto2;

import java.util.List;

import lombok.Data;

@Data
public class EnrollmentDTO {
    private Long id;
    private Long studentYearProfileId;

    // Infos liées au profil étudiant
    private Long academicYearId;
    private String academicYearLabel;
    private boolean profileActive;

    private Long levelId;
    private String levelName;

    private Long specialtyId;
    private String specialtyName;

    // Pour front (optionnel)
    private List<Long> sessionIds;
}
