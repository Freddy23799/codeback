package codeqr.code.dto2;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentYearProfileRequest {
    @NotNull
    private Long studentId;

    @NotNull
    private Long academicYearId;

    private boolean active = true;
}
