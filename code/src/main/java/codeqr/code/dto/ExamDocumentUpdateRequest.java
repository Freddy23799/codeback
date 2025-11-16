package codeqr.code.dto;

import java.util.List;

import codeqr.code.model.Visibility;
import lombok.Data;

@Data
public class ExamDocumentUpdateRequest {
    private String originalFilename;
    private List<Long> specialtyIds;
    private Visibility visibility;
    private Long courseId;
    private Long levelId;
    private Long academicYearId;
}
