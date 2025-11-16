package codeqr.code.dto;

import java.time.Instant;
import java.util.List;
import lombok.*;



@Data
public class StudentDocumentDto {

    private Long id;
    private String uuid;
    private String originalFilename;
    private String storedFilename;
    private String category;
    private Long sizeBytes;
    private String mimeType;
    private Instant uploadedAt;
    private LevelDto level;
    private SchoolYearDto schoolYear;
    private List<CourseDto> courses;
    private List<DisciplineDto> disciplines;
    private String visibility;
    private String uploaderUsername;

}
