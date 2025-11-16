package codeqr.code.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(
    name = "exam_document",
    indexes = {
        @Index(name = "idx_examdoc_year", columnList = "academic_year_id"),
        @Index(name = "idx_examdoc_level", columnList = "level_id"),
        @Index(name = "idx_examdoc_uploader", columnList = "uploader_id"),
        @Index(name = "idx_examdoc_category", columnList = "category")
    }
)
public class ExamDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identifiant unique
    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String uuid = UUID.randomUUID().toString();

    // Nom du fichier original
    @Column(nullable = false)
    private String originalFilename;

    // Nom du fichier stocké (après renommage)
    @Column(nullable = false)
    private String storedFilename;

    // Chemin de stockage physique
    @Column(nullable = false)
    private String storagePath;

    // Informations techniques
    private Long sizeBytes;
    private String mimeType;
    private boolean compressed = true;

    // Catégorie (CC, EXAM, SUPPORT, etc.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FileCategory category;

    // Format (PDF, DOCX, etc.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileFormat format;

    // Visibilité (PUBLIC, PRIVATE, ONLY_COURSE)
    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PUBLIC;

    // Dates et suppression logique
    private Instant uploadedAt = Instant.now();
    private boolean deleted = false;
    private Instant deletedAt;

    /* =========================
       🔹 RELATIONS PRINCIPALES
       ========================= */

    // 📘 Liste de cours associés
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "exam_document_course",
        joinColumns = @JoinColumn(name = "exam_document_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();

    // 🎓 Liste de spécialités associées
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "exam_document_specialty",
        joinColumns = @JoinColumn(name = "exam_document_id"),
        inverseJoinColumns = @JoinColumn(name = "specialty_id")
    )
    private Set<Specialty> specialties = new HashSet<>();

    // 🧩 Niveau (Licence 1, Master 2, etc.)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private Level level;

    // 📅 Année académique
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    // 👤 Professeur ou admin ayant uploadé
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    private User uploader;
}
