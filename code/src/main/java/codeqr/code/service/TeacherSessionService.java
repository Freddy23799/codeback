


package codeqr.code.service;

import codeqr.code.dto.PageResponse;
import codeqr.code.dto.SessionDtos;
import codeqr.code.model.Session;
import codeqr.code.model.Teacher;
import codeqr.code.repository.SessionRepository;
import codeqr.code.repository.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherSessionService {

    private final TeacherRepository teacherRepository;


    private final SessionRepository sessionRepository;

    public PageResponse<SessionDtos> getSessionsForTeacher(Long teacherId, String search, LocalDate date, Pageable pageable) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found with id " + teacherId));

        Long userId = teacher.getUser().getId();

        Specification<Session> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                Predicate p1 = cb.like(cb.lower(root.get("course").get("title")), like);
                Predicate p2 = cb.like(cb.lower(root.get("room").get("name")), like);
                Predicate p3 = cb.like(cb.lower(root.get("campus").get("name")), like);
                predicates.add(cb.or(p1, p2, p3));
            }

            if (date != null) {
                Expression<LocalDate> sessionDate = cb.function("DATE", LocalDate.class, root.get("startTime"));
                predicates.add(cb.equal(sessionDate, date));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Session> page = sessionRepository.findAll(spec, pageable);

        List<SessionDtos> dtos = page.stream().map(SessionDtos::fromEntity).toList();

        return new PageResponse<>(
                dtos,
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }





}
