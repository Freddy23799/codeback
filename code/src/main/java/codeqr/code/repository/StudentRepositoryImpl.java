package codeqr.code.repository;

import codeqr.code.dto.StudentListDTO;
import codeqr.code.dto.PreviewEnrollmentDTO;
import codeqr.code.dto.EnrollmentListDTO;
import codeqr.code.dto.SessionListDTO;
import codeqr.code.repository.StudentRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implémentation custom basée sur JdbcTemplate pour requêtes optimisées.
 * Adapté pour PostgreSQL et table app_user au lieu de user.
 */
@Repository
public class StudentRepositoryImpl implements StudentRepositoryCustom {

    @Autowired
    private JdbcTemplate jdbc;

    @Override
    public List<StudentListDTO> fetchStudentsLight(Long cursorId, int limit, String q, Long specialtyId, Long levelId) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT s.id, s.full_name, s.matricule, s.email, s.avatar_url, sx.name AS sexe_name, ")
          .append("(SELECT COUNT(*) FROM student_year_profile syp WHERE syp.student_id = s.id) AS enrollment_count ")
          .append("FROM student s ")
          .append("LEFT JOIN sexe sx ON s.sexe_id = sx.id ")
          .append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();
        if (q != null && !q.trim().isEmpty()) {
            sb.append(" AND (LOWER(s.full_name) LIKE ? OR LOWER(s.matricule) LIKE ? OR LOWER(s.email) LIKE ?) ");
            String qq = "%" + q.toLowerCase() + "%";
            params.add(qq); params.add(qq); params.add(qq);
        }
        if (specialtyId != null) {
            sb.append(" AND EXISTS(SELECT 1 FROM student_year_profile sy WHERE sy.student_id = s.id AND sy.specialty_id = ?) ");
            params.add(specialtyId);
        }
        if (levelId != null) {
            sb.append(" AND EXISTS(SELECT 1 FROM student_year_profile sy WHERE sy.student_id = s.id AND sy.level_id = ?) ");
            params.add(levelId);
        }
        if (cursorId != null) {
            sb.append(" AND s.id > ? ");
            params.add(cursorId);
        }
        sb.append(" ORDER BY s.id ASC LIMIT ? ");
        params.add(limit);

        List<StudentListDTO> students = jdbc.query(sb.toString(), params.toArray(), new RowMapper<StudentListDTO>() {
            @Override
            public StudentListDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                StudentListDTO d = new StudentListDTO();
                d.id = rs.getLong("id");
                d.fullName = rs.getString("full_name");
                d.matricule = rs.getString("matricule");
                d.email = rs.getString("email");
                d.avatarUrl = rs.getString("avatar_url");
                d.sexeName = rs.getString("sexe_name");
                d.enrollmentCount = rs.getInt("enrollment_count");
                return d;
            }
        });

        if (students.isEmpty()) return students;

        // Fetch preview enrollments (top 2 per student) in single query using window function (PostgreSQL compatible)
        String idsCsv = students.stream().map(s -> String.valueOf(s.id)).collect(Collectors.joining(","));

        String previewSql = ""
            + "SELECT t.student_id, t.student_year_profile_id, t.specialty_name, t.level_name, t.session_count, t.last_att "
            + "FROM ( "
            + "  SELECT syp.student_id, syp.id AS student_year_profile_id, sp.name AS specialty_name, l.name AS level_name, "
            + "         COALESCE(COUNT(DISTINCT a.session_id),0) AS session_count, "
            + "         MAX(a.scanned_at) AS last_att, "
            + "         ROW_NUMBER() OVER (PARTITION BY syp.student_id ORDER BY MAX(a.scanned_at) DESC) AS rn "
            + "  FROM student_year_profile syp "
            + "  LEFT JOIN specialty sp ON syp.specialty_id = sp.id "
            + "  LEFT JOIN level l ON syp.level_id = l.id "
            + "  LEFT JOIN attendance a ON a.student_year_profile_id = syp.id "
            + "  WHERE syp.student_id IN (" + idsCsv + ") "
            + "  GROUP BY syp.id, syp.student_id, sp.name, l.name "
            + ") t WHERE t.rn <= 2";

        Map<Long, List<PreviewEnrollmentDTO>> map = new HashMap<>();
        jdbc.query(previewSql, (ResultSet rs) -> {
            Long studentId = rs.getLong("student_id");
            PreviewEnrollmentDTO p = new PreviewEnrollmentDTO();
            p.studentYearProfileId = rs.getLong("student_year_profile_id");
            p.specialtyName = rs.getString("specialty_name");
            p.levelName = rs.getString("level_name");
            p.sessionCount = rs.getInt("session_count");
            p.lastAttendanceAt = rs.getString("last_att");
            map.computeIfAbsent(studentId, k -> new ArrayList<>()).add(p);
        });

        for (StudentListDTO s : students) {
            s.previewEnrollments = map.getOrDefault(s.id, Collections.emptyList());
        }

        return students;
    }

    @Override
    public List<EnrollmentListDTO> fetchEnrollmentsByStudent(Long studentId, int offset, int limit) {
        String sql = ""
          + "SELECT syp.id as student_year_profile_id, ay.label as academicYearLabel, sp.name as specialtyName, l.name as levelName, "
          + "       COALESCE(COUNT(DISTINCT a.session_id),0) as sessionCount, MAX(a.scanned_at) as last_att "
          + "FROM student_year_profile syp "
          + "LEFT JOIN academic_year ay ON syp.academic_year_id = ay.id "
          + "LEFT JOIN specialty sp ON syp.specialty_id = sp.id "
          + "LEFT JOIN level l ON syp.level_id = l.id "
          + "LEFT JOIN attendance a ON a.student_year_profile_id = syp.id "
          + "WHERE syp.student_id = ? "
          + "GROUP BY syp.id, ay.label, sp.name, l.name "
          + "ORDER BY last_att DESC "
          + "LIMIT ? OFFSET ?";

        return jdbc.query(sql, new Object[]{studentId, limit, offset}, (rs, rowNum) -> {
            EnrollmentListDTO d = new EnrollmentListDTO();
            d.studentYearProfileId = rs.getLong("student_year_profile_id");
            d.academicYearName = rs.getString("academicYearLabel");
            d.specialtyName = rs.getString("specialtyName");
            d.levelName = rs.getString("levelName");
            d.sessionCount = rs.getInt("sessionCount");
            d.lastAttendanceAt = rs.getString("last_att");
            return d;
        });
    }

    @Override
    public List<SessionListDTO> fetchSessionsByEnrollment(Long studentYearProfileId, LocalDateTime start, LocalDateTime end, LocalDateTime lastStartTime, Long lastId, int limit) {
        StringBuilder sb = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sb.append("SELECT s.id as session_id, c.title AS courseTitle, cp.name AS campusName, r.name AS roomName, t.full_name AS teacherName, s.start_time, s.end_time, ")
          .append("COALESCE(cnt.att_count,0) AS attendanceCount, COALESCE(a.status,'PENDING') AS attendanceStatus ")
          .append("FROM session s ")
          .append("LEFT JOIN course c ON s.course_id = c.id ")
          .append("LEFT JOIN campus cp ON s.campus_id = cp.id ")
          .append("LEFT JOIN room r ON s.room_id = r.id ")
          .append("LEFT JOIN teacher t ON t.user_id = s.user_id ")
          .append("LEFT JOIN attendance a ON a.session_id = s.id AND a.student_year_profile_id = ? ")
          .append("LEFT JOIN (SELECT session_id, COUNT(*) as att_count FROM attendance WHERE student_year_profile_id = ? GROUP BY session_id) cnt ON cnt.session_id = s.id ")
          .append("WHERE s.id IN (SELECT DISTINCT session_id FROM attendance WHERE student_year_profile_id = ?) ");
        params.add(studentYearProfileId);
        params.add(studentYearProfileId);
        params.add(studentYearProfileId);

        if (start != null) { sb.append(" AND s.start_time >= ? "); params.add(start); }
        if (end != null) { sb.append(" AND s.start_time <= ? "); params.add(end); }

        if (lastStartTime != null && lastId != null) {
            sb.append(" AND (s.start_time, s.id) < (?, ?) ");
            params.add(lastStartTime); params.add(lastId);
        }

        sb.append(" ORDER BY s.start_time DESC, s.id DESC LIMIT ? ");
        params.add(limit);

        return jdbc.query(sb.toString(), params.toArray(), (rs, rn) -> {
            SessionListDTO d = new SessionListDTO();
            d.sessionId = rs.getLong("session_id");
            d.courseTitle = rs.getString("courseTitle");
            d.campusName = rs.getString("campusName");
            d.roomName = rs.getString("roomName");
            d.teacherName = rs.getString("teacherName");
            d.startTime = rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime() : null;
            d.endTime = rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null;
            d.attendanceCount = rs.getInt("attendanceCount");
            d.attendanceStatus = rs.getString("attendanceStatus");
            return d;
        });
    }
}
