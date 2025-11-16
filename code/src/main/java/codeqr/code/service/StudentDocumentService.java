package codeqr.code.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codeqr.code.dto.CourseDto;
import codeqr.code.dto.DisciplineDto;
import codeqr.code.dto.LevelDto;
import codeqr.code.dto.SchoolYearDto;
import codeqr.code.dto.StudentDocumentDto;
import codeqr.code.model.AcademicYear;
import codeqr.code.model.Course;
import codeqr.code.model.ExamDocument;
import codeqr.code.model.Specialty;
import codeqr.code.model.Student;
import codeqr.code.model.User;
import codeqr.code.repository.ExamDocumentRepository;
import codeqr.code.repository.UserRepository;

import jakarta.persistence.criteria.JoinType;

@Service
public class StudentDocumentService {

    private final ExamDocumentRepository docRepo;
    private final UserRepository userRepo;

    public StudentDocumentService(ExamDocumentRepository docRepo, UserRepository userRepo) {
        this.docRepo = docRepo;
        this.userRepo = userRepo;
    }

    @Transactional(readOnly = true)
    public Page<StudentDocumentDto> listForStudent(String username,
                                                   Optional<String> category,
                                                   Optional<Long> courseId,
                                                   Optional<Long> levelId,
                                                   Optional<Long> yearId,
                                                   Optional<String> q,
                                                   int page,
                                                   int size) {

        // Load user + student profile (use repo method that fetches profile eagerly)
        User user = userRepo.findByUsernameWithStudentProfile(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        Student student = user.getStudent();
        if (student == null) {
            throw new IllegalArgumentException("Aucun profil étudiant pour l'utilisateur");
        }

        // collect allowed specialties and allowed levels from student profiles
        Set<Long> allowedSpecialtyIds = new HashSet<>();
        Set<Long> allowedLevelIds = new HashSet<>();
        if (student.getStudentYearProfiles() != null) {
            for (var p : student.getStudentYearProfiles()) {
                if (p.getSpecialty() != null && p.getSpecialty().getId() != null) {
                    allowedSpecialtyIds.add(p.getSpecialty().getId());
                }
                if (p.getLevel() != null && p.getLevel().getId() != null) {
                    allowedLevelIds.add(p.getLevel().getId());
                }
            }
        }

        if (allowedSpecialtyIds.isEmpty()) {
            // l'étudiant n'a pas de spécialité -> aucun document accessible
            return new PageImpl<>(List.of(), PageRequest.of(page, size), 0);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));

        Specification<ExamDocument> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> preds = new ArrayList<>();
            preds.add(cb.equal(root.get("deleted"), false));

            // ensure document has at least one specialty in student's specialties
            jakarta.persistence.criteria.Join<ExamDocument, Specialty> spJoin = root.joinSet("specialties", JoinType.INNER);
            preds.add(spJoin.get("id").in(allowedSpecialtyIds));
            query.distinct(true);

            category.ifPresent(cat -> preds.add(cb.equal(root.get("category"), cat)));

            if (courseId.isPresent()) {
                jakarta.persistence.criteria.Join<ExamDocument, Course> cj = root.joinSet("courses", JoinType.INNER);
                preds.add(cb.equal(cj.get("id"), courseId.get()));
                query.distinct(true);
            }

            // levelId if provided (filter requested) otherwise ensure the document's level belongs to allowedLevelIds
            if (levelId.isPresent()) {
                preds.add(cb.equal(root.get("level").get("id"), levelId.get()));
            } else if (!allowedLevelIds.isEmpty()) {
                preds.add(root.get("level").get("id").in(allowedLevelIds));
            }

            yearId.ifPresent(y -> preds.add(cb.equal(root.get("academicYear").get("id"), y)));

            q.ifPresent(qs -> {
                String like = "%" + qs.toLowerCase() + "%";
                preds.add(cb.or(
                    cb.like(cb.lower(root.get("originalFilename")), like),
                    cb.like(cb.lower(root.get("storedFilename")), like)
                ));
            });

            return cb.and(preds.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<ExamDocument> docs = docRepo.findAll(spec, pageable);

        List<StudentDocumentDto> dtos = docs.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, docs.getTotalElements());
    }

    /**
     * Map ExamDocument -> StudentDocumentDto using separate DTO classes.
     * Convert enum fields to String for DTO (category, visibility).
     */
    private StudentDocumentDto toDto(ExamDocument d) {
        StudentDocumentDto out = new StudentDocumentDto();
        out.setId(d.getId());
        out.setUuid(d.getUuid());
        out.setOriginalFilename(d.getOriginalFilename());
        out.setStoredFilename(d.getStoredFilename());
        // convert enum FileCategory -> String (NAME)
        out.setCategory(d.getCategory() != null ? d.getCategory().name() : null);
        out.setSizeBytes(d.getSizeBytes());
        out.setMimeType(d.getMimeType());
        out.setUploadedAt(d.getUploadedAt());

        if (d.getLevel() != null) {
            LevelDto l = new LevelDto();
            l.setId(d.getLevel().getId());
            l.setName(d.getLevel().getName());
            out.setLevel(l);
        }

        if (d.getAcademicYear() != null) {
            AcademicYear ay = d.getAcademicYear();
            SchoolYearDto sy = new SchoolYearDto();
            sy.setId(ay.getId());
            sy.setLabel(ay.getLabel());
            out.setSchoolYear(sy);
        }

        if (d.getCourses() != null && !d.getCourses().isEmpty()) {
            List<CourseDto> cs = d.getCourses().stream().map(c -> {
                CourseDto cd = new CourseDto();
                cd.setId(c.getId());
                cd.setCode(c.getCode());
                cd.setTitle(c.getTitle());
                return cd;
            }).collect(Collectors.toList());
            out.setCourses(cs);
        }

        if (d.getSpecialties() != null && !d.getSpecialties().isEmpty()) {
            List<DisciplineDto> ds = d.getSpecialties().stream().map(s -> {
                DisciplineDto dd = new DisciplineDto();
                dd.setId(s.getId());
                dd.setName(s.getName());
                return dd;
            }).collect(Collectors.toList());
            out.setDisciplines(ds);
        }

        // convert enum Visibility -> String (NAME)
        out.setVisibility(d.getVisibility() != null ? d.getVisibility().name() : null);
        out.setUploaderUsername(d.getUploader() != null ? d.getUploader().getUsername() : null);

        return out;
    }
}
