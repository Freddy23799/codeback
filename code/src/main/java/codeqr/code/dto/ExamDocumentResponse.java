package codeqr.code.dto;

import java.time.Instant;
import java.util.List;

import codeqr.code.model.FileCategory;
import codeqr.code.model.FileFormat;
import codeqr.code.model.Visibility;
import lombok.Data;

@Data
public class ExamDocumentResponse {
    private Long id;
    private String uuid;
    private String originalFilename;
    private String storedFilename;
    private String storagePath;
    private Long sizeBytes;
    private String mimeType;
    private FileCategory category;
    private FileFormat format;
    private Visibility visibility;
    private Instant uploadedAt;
    private String uploaderUsername;

    private CourseDto course; // pour compatibilité si un seul course (optionnel)
    private List<CourseDto> courses; // nouvelle liste de cours
    private LevelDto level;
    private SchoolYearDto schoolYear;
    private List<DisciplineDto> disciplines;

    @Data public static class CourseDto { private Long id; private String code; private String title; public CourseDto(){} public CourseDto(Long id, String code, String title){ this.id = id; this.code = code; this.title = title;} }
    @Data public static class LevelDto { private Long id; private String name; public LevelDto(){} public LevelDto(Long id, String name){ this.id = id; this.name = name;} }
    @Data public static class SchoolYearDto { private Long id; private String label; public SchoolYearDto(){} public SchoolYearDto(Long id, String label){ this.id = id; this.label = label;} }
    @Data public static class DisciplineDto { private Long id; private String name; public DisciplineDto(){} public DisciplineDto(Long id, String name){ this.id = id; this.name = name;} }
}
