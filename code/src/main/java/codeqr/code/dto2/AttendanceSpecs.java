

package codeqr.code.dto2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.jpa.domain.Specification;

import codeqr.code.model.Attendance;

public class AttendanceSpecs {

    public static Specification<Attendance> forStudentProfile(Long studentYearProfileId) {
        return (root, cq, cb) -> cb.equal(root.get("studentYearProfile").get("id"), studentYearProfileId);
    }

    public static Specification<Attendance> byStatus(Attendance.Status status) {
        return (root, cq, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Attendance> between(LocalDate from, LocalDate to) {
        if (from == null && to == null) return (r,cq,cb) -> cb.conjunction();
        LocalDateTime startTime = from != null ? from.atStartOfDay() : LocalDate.MIN.atStartOfDay();
        LocalDateTime endTime = to != null ? to.atTime(LocalTime.MAX) : LocalDate.MAX.atTime(LocalTime.MAX);
        return (root, cq, cb) -> cb.between(root.get("session").get("startTime"), startTime, endTime);
    }

    public static Specification<Attendance> byCourseId(Long id) {
        return (r,cq,cb) -> id == null ? cb.conjunction() : cb.equal(r.get("session").get("course").get("id"), id);
    }

    public static Specification<Attendance> byCampusId(Long id) {
        return (r,cq,cb) -> id == null ? cb.conjunction() : cb.equal(r.get("session").get("campus").get("id"), id);
    }

    public static Specification<Attendance> byRoomId(Long id) {
        return (r,cq,cb) -> id == null ? cb.conjunction() : cb.equal(r.get("session").get("room").get("id"), id);
    }

    public static Specification<Attendance> fullText(String q) {
        if (q == null || q.isBlank()) return (r,cq,cb) -> cb.conjunction();
        String like = "%" + q.toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
            cb.like(cb.lower(root.get("session").get("course").get("title")), like),
            cb.like(cb.lower(root.get("session").get("course").get("code")), like),
            cb.like(cb.lower(root.get("session").get("room").get("name")), like),
            cb.like(cb.lower(root.get("session").get("campus").get("name")), like),
            cb.like(cb.lower(root.get("session").get("teacherYearProfile").get("teacher").get("fullName")), like)
        );
    }
}





