package codeqr.code.repository;

import codeqr.code.model.Session;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class SessionSpecification {

    public static Specification<Session> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) return null;
            String like = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("course").get("title")), like),
                cb.like(cb.lower(root.get("room").get("name")), like),
                cb.like(cb.lower(root.get("campus").get("name")), like),
                cb.like(cb.lower(root.get("user").get("teacher").get("fullName")), like)
            );
        };
    }

    public static Specification<Session> hasDate(LocalDate date) {
        return (root, query, cb) -> {
            if (date == null) return null;
            // compare la partie date de startTime avec la date passée
            Expression<java.sql.Date> sessionDate = cb.function("date", java.sql.Date.class, root.get("startTime"));
            return cb.equal(sessionDate, java.sql.Date.valueOf(date));
        };
    }

    public static Specification<Session> hasTeacherId(Long teacherId) {
        return (root, query, cb) -> {
            if (teacherId == null) return null;
            return cb.equal(root.get("user").get("teacher").get("id"), teacherId);
        };
    }
}