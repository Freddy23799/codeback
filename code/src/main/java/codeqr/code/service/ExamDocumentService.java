package codeqr.code.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import codeqr.code.dto.ExamDocumentResponse;
import codeqr.code.dto.ExamDocumentUpdateRequest;
import codeqr.code.dto.ExamDocumentUploadRequest;
import codeqr.code.model.AcademicYear;
import codeqr.code.model.Course;
import codeqr.code.model.ExamDocument;
import codeqr.code.model.FileCategory;
import codeqr.code.model.FileFormat;
import codeqr.code.model.Level;
import codeqr.code.model.Specialty;
import codeqr.code.model.User;
import codeqr.code.repository.AcademicYearRepository;
import codeqr.code.repository.CourseRepository;
import codeqr.code.repository.ExamDocumentRepository;
import codeqr.code.repository.LevelRepository;
import codeqr.code.repository.SpecialtyRepository;
import codeqr.code.repository.UserRepository;
import codeqr.code.storage.StorageService;
import jakarta.persistence.criteria.JoinType;

@Service
public class ExamDocumentService {

    private final Logger log = LoggerFactory.getLogger(ExamDocumentService.class);

    private final ExamDocumentRepository repo;
    private final SpecialtyRepository specialtyRepo;
    private final CourseRepository courseRepo;
    private final LevelRepository levelRepo;
    private final AcademicYearRepository yearRepo;
    private final UserRepository userRepo;
    private final StorageService storageService;

    private final long maxUploadBytes;
    private final Tika tika = new Tika();
    private final Random rand = new Random();

    public ExamDocumentService(ExamDocumentRepository repo,
                               SpecialtyRepository specialtyRepo,
                               CourseRepository courseRepo,
                               LevelRepository levelRepo,
                               AcademicYearRepository yearRepo,
                               UserRepository userRepo,
                               StorageService storageService,
                               @Value("${app.upload.max-bytes:10485760}") long maxUploadBytes) {
        this.repo = repo;
        this.specialtyRepo = specialtyRepo;
        this.courseRepo = courseRepo;
        this.levelRepo = levelRepo;
        this.yearRepo = yearRepo;
        this.userRepo = userRepo;
        this.storageService = storageService;
        this.maxUploadBytes = maxUploadBytes;
    }

    @Transactional
    public List<ExamDocumentResponse> uploadFiles(MultipartFile[] files, ExamDocumentUploadRequest metadata, String uploaderUsername) throws IOException {
        log.debug("uploadFiles called files={} metadata={}", files == null ? 0 : files.length, metadata);
        if (files == null || files.length == 0) throw new IllegalArgumentException("Aucun fichier à uploader");
        if (metadata == null) throw new IllegalArgumentException("Meta manquante");

        // Resolve single entities
        Level level = null;
        AcademicYear year = null;
        User uploader = null;

        if (metadata.getLevelId() != null) level = levelRepo.findById(metadata.getLevelId()).orElseThrow(() -> new IllegalArgumentException("Level not found"));
        if (metadata.getAcademicYearId() != null) year = yearRepo.findById(metadata.getAcademicYearId()).orElseThrow(() -> new IllegalArgumentException("Academic year not found"));
        if (uploaderUsername != null) uploader = userRepo.findByUsername(uploaderUsername).orElse(null);

        // Resolve courses: support either courseId (single) or courseIds (list)
        Set<Course> courses = new HashSet<>();
        if (metadata.getCourseIds() != null && !metadata.getCourseIds().isEmpty()) {
            courseRepo.findAllById(metadata.getCourseIds()).forEach(courses::add);
            if (courses.isEmpty()) throw new IllegalArgumentException("Course(s) introuvable(s)");
        }

        // Resolve specialties
        Set<Specialty> specialties = new HashSet<>();
        if (metadata.getSpecialtyIds() != null && !metadata.getSpecialtyIds().isEmpty()) {
            specialtyRepo.findAllById(metadata.getSpecialtyIds()).forEach(specialties::add);
        }

        List<ExamDocumentResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            log.debug("Traitement fichier: name={} size={}", file.getOriginalFilename(), file.getSize());

            if (file.getSize() > maxUploadBytes) {
                log.warn("Fichier trop grand: {} (max {} bytes)", file.getOriginalFilename(), maxUploadBytes);
                throw new IllegalArgumentException("Le fichier dépasse la taille maximale de " + (maxUploadBytes / (1024*1024)) + " Mo : " + file.getOriginalFilename());
            }

            String detectedMime = detectMimeType(file);
            FileFormat format = mapMimeToFormat(detectedMime, file.getOriginalFilename());
            if (format == null) {
                log.warn("Format non supporté pour {} (mime={})", file.getOriginalFilename(), detectedMime);
                throw new IllegalArgumentException("Format de fichier non supporté : " + file.getOriginalFilename());
            }

            String yearLabel = year != null ? sanitize(year.getLabel()) : "unknown_year";
            String coursePart = "unknown_course";
            if (!courses.isEmpty()) {
                if (courses.size() == 1) coursePart = sanitize(courses.iterator().next().getCode());
                else coursePart = "multi_course";
            }
            String levelName = level != null ? sanitize(level.getName()) : "unknown_level";

            String prefix = String.join("/", yearLabel, coursePart, levelName);

            // compute storedFilename (timestamp + random to avoid listing directories)
            String baseName = originalBaseName(file.getOriginalFilename());
            String ext = getExtension(file.getOriginalFilename()); // includes dot if present
            String storedFilename = sanitize(baseName) + "_" + Instant.now().toEpochMilli() + "_" + (rand.nextInt(9000) + 1000) + ext;

            String objectKey = prefix + "/" + storedFilename + ".gz";

            // create temporary gz file then upload
            java.nio.file.Path tmp = java.nio.file.Files.createTempFile("upload-", ".gz");
            try (InputStream in = file.getInputStream();
                 OutputStream fos = java.nio.file.Files.newOutputStream(tmp, java.nio.file.StandardOpenOption.WRITE, java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
                 GZIPOutputStream gos = new GZIPOutputStream(fos)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    gos.write(buffer, 0, len);
                }
            }

            long size = java.nio.file.Files.size(tmp);

            try (InputStream uploadStream = java.nio.file.Files.newInputStream(tmp, java.nio.file.StandardOpenOption.READ)) {
                storageService.put(objectKey, uploadStream, size, detectedMime);
            } finally {
                java.nio.file.Files.deleteIfExists(tmp);
            }

            ExamDocument doc = new ExamDocument();
            doc.setOriginalFilename(file.getOriginalFilename());
            doc.setStoredFilename(storedFilename);
            doc.setStoragePath(prefix); // prefix used as logical folder in bucket or local root
            doc.setSizeBytes(size);
            doc.setMimeType(detectedMime);
            doc.setCompressed(true);
            doc.setCategory(metadata.getCategory());
            doc.setFormat(format);
            doc.setVisibility(metadata.getVisibility());
            doc.setLevel(level);
            doc.setAcademicYear(year);
            doc.setUploader(uploader);
            if (!specialties.isEmpty()) doc.getSpecialties().addAll(specialties);
            if (!courses.isEmpty()) doc.getCourses().addAll(courses);
            doc.setUploadedAt(Instant.now());

            repo.save(doc);
            responses.add(toResponse(doc));
            log.info("Fichier stocké (key={} db id={})", objectKey, doc.getId());
        }

        return responses;
    }

    private String originalBaseName(String name) {
        if (name == null) return "file";
        int dot = name.lastIndexOf('.');
        return dot == -1 ? name : name.substring(0, dot);
    }

    private String getExtension(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot == -1 ? "" : name.substring(dot);
    }

    private String sanitize(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[\\\\/:*?\"<>|]+", "_").replaceAll("\\s+", "_");
    }

    private String detectMimeType(MultipartFile file) {
        try {
            String t = tika.detect(file.getInputStream(), file.getOriginalFilename());
            if (t != null) return t;
        } catch (Exception ignored) {}
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (name.endsWith(".pdf")) return "application/pdf";
        if (name.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (name.endsWith(".doc")) return "application/msword";
        if (name.endsWith(".csv")) return "text/csv";
        if (name.endsWith(".zip")) return "application/zip";
        if (name.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".mp4")) return "video/mp4";
        return "application/octet-stream";
    }

    private FileFormat mapMimeToFormat(String mime, String originalFilename) {
        String lower = originalFilename == null ? "" : originalFilename.toLowerCase();
        if (lower.endsWith(".pdf") || "application/pdf".equals(mime)) return FileFormat.PDF;
        if (lower.endsWith(".docx") || (mime != null && mime.contains("wordprocessingml"))) return FileFormat.DOCX;
        if (lower.endsWith(".doc") || (mime != null && mime.contains("msword"))) return FileFormat.DOC;
        if (lower.endsWith(".xlsx") || (mime != null && mime.contains("spreadsheetml"))) return FileFormat.XLSX;
        if (lower.endsWith(".pptx") || (mime != null && mime.contains("presentation"))) return FileFormat.PPTX;
        if (lower.endsWith(".csv") || "text/csv".equals(mime)) return FileFormat.CSV;
        if (lower.endsWith(".zip") || "application/zip".equals(mime)) return FileFormat.ZIP;
        if ((lower.endsWith(".jpg") || lower.endsWith(".jpeg")) || (mime != null && mime.startsWith("image/"))) return FileFormat.JPG;
        if (lower.endsWith(".png")) return FileFormat.PNG;
        if (lower.endsWith(".mp4") || (mime != null && mime.startsWith("video/"))) return FileFormat.MP4;
        return null;
    }

    public ExamDocumentResponse toResponse(ExamDocument doc) {
        ExamDocumentResponse r = new ExamDocumentResponse();
        r.setId(doc.getId());
        r.setUuid(doc.getUuid());
        r.setOriginalFilename(doc.getOriginalFilename());
        r.setStoredFilename(doc.getStoredFilename());
        r.setStoragePath(doc.getStoragePath());
        r.setSizeBytes(doc.getSizeBytes());
        r.setMimeType(doc.getMimeType());
        r.setCategory(doc.getCategory());
        r.setFormat(doc.getFormat());
        r.setVisibility(doc.getVisibility());
        r.setUploadedAt(doc.getUploadedAt());
        r.setUploaderUsername(doc.getUploader() != null ? doc.getUploader().getUsername() : null);

        if (doc.getCourses() != null && !doc.getCourses().isEmpty()) {
            List<ExamDocumentResponse.CourseDto> courseDtos = doc.getCourses().stream()
                    .map(c -> new ExamDocumentResponse.CourseDto(c.getId(), c.getCode(), c.getTitle()))
                    .collect(Collectors.toList());
            r.setCourses(courseDtos);
        }

        if (doc.getLevel() != null) {
            r.setLevel(new ExamDocumentResponse.LevelDto(doc.getLevel().getId(), doc.getLevel().getName()));
        }
        if (doc.getAcademicYear() != null) {
            r.setSchoolYear(new ExamDocumentResponse.SchoolYearDto(doc.getAcademicYear().getId(), doc.getAcademicYear().getLabel()));
        }
        if (doc.getSpecialties() != null && !doc.getSpecialties().isEmpty()) {
            List<ExamDocumentResponse.DisciplineDto> disciplines = doc.getSpecialties().stream()
                    .map(s -> new ExamDocumentResponse.DisciplineDto(s.getId(), s.getName()))
                    .collect(Collectors.toList());
            r.setDisciplines(disciplines);
        }

        return r;
    }

    /**
     * Build object key used by storage backends (prefix + storedFilename.gz)
     */
    public String resolveStoredGzipKey(ExamDocument doc) {
        return doc.getStoragePath() + "/" + doc.getStoredFilename() + ".gz";
    }

    public InputStream openDecompressedStream(ExamDocument doc) throws IOException {
        String key = resolveStoredGzipKey(doc);
        InputStream gz = storageService.get(key);
        if (gz == null) throw new FileNotFoundException("Fichier stocké introuvable : " + key);
        return new java.util.zip.GZIPInputStream(gz);
    }

    @Transactional
    public void deleteDocument(Long id) throws IOException {
        ExamDocument doc = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        if (doc.isDeleted()) {
            log.debug("Document déjà supprimé id={}", id);
            return;
        }
        doc.setDeleted(true);
        doc.setDeletedAt(Instant.now());
        repo.save(doc);

        String key = resolveStoredGzipKey(doc);
        String archiveKey = "archives/deleted/" + doc.getStoredFilename() + ".gz";
        try {
            storageService.copy(key, archiveKey);
            storageService.delete(key);
            log.info("Objet archivé puis supprimé key={} archived={}", key, archiveKey);
        } catch (IOException e) {
            // si archive échoue, on essaie au moins de supprimer pour éviter fuite d'espace
            log.warn("Impossible d'archiver l'objet (key={}), tentative suppression directe", key, e);
            try {
                storageService.delete(key);
            } catch (Exception ex) {
                log.error("Impossible de supprimer l'objet après échec archive key={}", key, ex);
            }
        }
    }

    @Transactional
    public ExamDocument updateMetadata(Long id, ExamDocumentUpdateRequest request, String username) {
        ExamDocument doc = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Document introuvable"));
        if (request.getOriginalFilename() != null && !request.getOriginalFilename().isBlank()) {
            doc.setOriginalFilename(request.getOriginalFilename().trim());
        }
        if (request.getSpecialtyIds() != null) {
            Set<Specialty> specialties = new HashSet<>();
            specialtyRepo.findAllById(request.getSpecialtyIds()).forEach(specialties::add);
            doc.getSpecialties().clear();
            doc.getSpecialties().addAll(specialties);
        }
        if (request.getVisibility() != null) {
            doc.setVisibility(request.getVisibility());
        }
        log.info("Metadata updated for doc id={} by user={}", id, username);
        return repo.save(doc);
    }

    /**
     * Requête de recherche paginée.
     * Si uploaderUsername.isPresent() -> on filtre par uploader.username == uploaderUsername.get()
     */
    public Page<ExamDocumentResponse> search(
            Optional<Long> yearId,
            Optional<Long> courseId,
            Optional<Long> levelId,
            Optional<List<Long>> specialtyIds,
            Optional<FileCategory> category,
            String q,
            Optional<String> uploaderUsername,
            
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));

        org.springframework.data.jpa.domain.Specification<ExamDocument> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> preds = new ArrayList<>();
            preds.add(cb.equal(root.get("deleted"), false));

            yearId.ifPresent(y -> preds.add(cb.equal(root.get("academicYear").get("id"), y)));
            levelId.ifPresent(l -> preds.add(cb.equal(root.get("level").get("id"), l)));
            category.ifPresent(cat -> preds.add(cb.equal(root.get("category"), cat)));

            if (courseId.isPresent()) {
                jakarta.persistence.criteria.Join<ExamDocument, Course> courseJoin =
                        root.joinSet("courses", JoinType.INNER);
                preds.add(cb.equal(courseJoin.get("id"), courseId.get()));
                query.distinct(true);
            }

            if (specialtyIds.isPresent() && !specialtyIds.get().isEmpty()) {
                jakarta.persistence.criteria.Join<ExamDocument, Specialty> join =
                        root.joinSet("specialties", JoinType.INNER);
                preds.add(join.get("id").in(specialtyIds.get()));
                query.distinct(true);
            }

            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(root.get("originalFilename")), like),
                        cb.like(cb.lower(root.get("storedFilename")), like)
                ));
            }

            // === NEW: filter by uploader username when provided ===
            if (uploaderUsername.isPresent() && uploaderUsername.get() != null && !uploaderUsername.get().isBlank()) {
                // left join on uploader to handle null uploaders safely
                jakarta.persistence.criteria.Join<ExamDocument, ?> uploaderJoin = root.join("uploader", JoinType.LEFT);
                preds.add(cb.equal(uploaderJoin.get("username"), uploaderUsername.get()));
                query.distinct(true);
            }

            return cb.and(preds.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<ExamDocument> pageRes = repo.findAll(spec, pageable);
        List<ExamDocumentResponse> content = new ArrayList<>();
        for (ExamDocument d : pageRes.getContent()) {
            content.add(toResponse(d));
        }

        return new PageImpl<>(content, pageable, pageRes.getTotalElements());
    }
}
