package codeqr.code.dto;

import java.util.List;

import codeqr.code.model.FileCategory;
import codeqr.code.model.Visibility;

public class ExamDocumentUploadRequest {
    private List<Long> courseIds;        // liste de cours (peut être vide)
    private List<Long> specialtyIds;     // liste de spécialités (peut être vide)
    private Long levelId;
    private Long academicYearId;
    private FileCategory category = FileCategory.EXAM;
    private Visibility visibility = Visibility.PUBLIC;

    // --- Getters & Setters ---
    public List<Long> getCourseIds() { return courseIds; }
    public void setCourseIds(List<Long> courseIds) { this.courseIds = courseIds; }

    public List<Long> getSpecialtyIds() { return specialtyIds; }
    public void setSpecialtyIds(List<Long> specialtyIds) { this.specialtyIds = specialtyIds; }

    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }

    public Long getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(Long academicYearId) { this.academicYearId = academicYearId; }

    public FileCategory getCategory() { return category; }
    public void setCategory(FileCategory category) { this.category = category; }

    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
}
