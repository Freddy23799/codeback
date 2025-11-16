package codeqr.code.repository;

import codeqr.code.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class AdminJdbcRepository {

    @Autowired
    private JdbcTemplate jdbc;

    /**
     * Retourne la liste "light" des professeurs (optionnellement filtrée par q).
     * Limit raisonnable (ex: 1000) pour le virtual-scroll (frontend fait le reste).
     */
    public List<TeacherLightDTO> fetchTeachersLight(String q, int limit) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT t.id AS teacher_id, t.full_name AS teacher_name, t.email AS teacher_email, ")
          .append("(SELECT name FROM sexe sx WHERE sx.id = t.sexe_id) AS teacher_gender, ")
          .append("(SELECT COUNT(*) FROM teacher_year_profile typ JOIN session s ON s.teacher_year_profile_id = typ.id WHERE typ.teacher_id = t.id) AS sessions_count ")
          .append("FROM teacher t ");

        List<Object> params = new ArrayList<>();
        if (q != null && !q.trim().isEmpty()) {
            sb.append("WHERE LOWER(t.full_name) LIKE ? ");
            params.add("%" + q.toLowerCase().trim() + "%");
        }
        sb.append("ORDER BY t.full_name ASC LIMIT ? ");
        params.add(limit);

        return jdbc.query(sb.toString(), params.toArray(), (rs, rn) -> {
            TeacherLightDTO dto = new TeacherLightDTO();
            dto.teacherId = rs.getLong("teacher_id");
            dto.teacherName = rs.getString("teacher_name");
            dto.teacherEmail = rs.getString("teacher_email");
            dto.teacherGender = rs.getString("teacher_gender");
            dto.sessionsCount = rs.getInt("sessions_count");
            return dto;
        });
    }

    /**
     * Récupère les sessions d'un professeur - cléset pagination (lastStart,lastId) pour scalabilité.
     * Filtres start/end (plage), academicYearId, specialtyId, levelId sont appliqués si fournis.
     */
    public List<SessionLightDT> fetchSessionsByTeacher(Long teacherId,
                                                        LocalDateTime start,
                                                        LocalDateTime end,
                                                        LocalDateTime lastStart,
                                                        Long lastId,
                                                        int limit,
                                                        Long academicYearId,
                                                        Long specialtyId,
                                                        Long levelId) {
        StringBuilder sb = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sb.append("SELECT s.id as session_id, c.title AS courseTitle, cp.name AS campusName, r.name AS roomName, ")
          .append("COALESCE(tch.full_name, '') AS teacherName, s.start_time, s.end_time, ")
          .append("sp.name AS specialtyName, l.name AS levelName, ")
          .append("COALESCE(cnt.att_count,0) AS attendanceCount ")
          .append("FROM session s ")
          .append("LEFT JOIN course c ON s.course_id = c.id ")
          .append("LEFT JOIN campus cp ON s.campus_id = cp.id ")
          .append("LEFT JOIN room r ON s.room_id = r.id ")
          .append("LEFT JOIN teacher_year_profile typ ON s.teacher_year_profile_id = typ.id ")
          .append("LEFT JOIN teacher tch ON typ.teacher_id = tch.id ")
          .append("LEFT JOIN app_user u ON s.user_id = u.id ") // ✅ corrigé
          .append("LEFT JOIN specialty sp ON s.expected_specialty_id = sp.id ")
          .append("LEFT JOIN level l ON s.expected_level_id = l.id ")
          .append("LEFT JOIN (SELECT session_id, COUNT(*) as att_count FROM attendance GROUP BY session_id) cnt ON cnt.session_id = s.id ")
          .append("WHERE (typ.teacher_id = ? OR u.id = (SELECT user_id FROM teacher WHERE id = ?)) ");
        params.add(teacherId); params.add(teacherId);

        if (academicYearId != null) { sb.append(" AND s.academic_year_id = ? "); params.add(academicYearId); }
        if (specialtyId != null) { sb.append(" AND s.expected_specialty_id = ? "); params.add(specialtyId); }
        if (levelId != null) { sb.append(" AND s.expected_level_id = ? "); params.add(levelId); }

        if (start != null) { sb.append(" AND s.start_time >= ? "); params.add(start); }
        if (end != null)   { sb.append(" AND s.start_time <= ? "); params.add(end); }

        // keyset pagination (start_time desc, id desc)
        if (lastStart != null && lastId != null) {
            sb.append(" AND (s.start_time < ? OR (s.start_time = ? AND s.id < ?)) ");
            params.add(lastStart); params.add(lastStart); params.add(lastId);
        }

        sb.append(" ORDER BY s.start_time DESC, s.id DESC LIMIT ? ");
        params.add(limit);

        return jdbc.query(sb.toString(), params.toArray(), (rs, rn) -> {
            SessionLightDT d = new SessionLightDT();
            d.sessionId = rs.getLong("session_id");
            d.courseTitle = rs.getString("courseTitle");
            d.campusName = rs.getString("campusName");
            d.roomName = rs.getString("roomName");
            d.teacherName = rs.getString("teacherName");
            d.startTime = rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime() : null;
            d.endTime = rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null;
            d.specialtyName = rs.getString("specialtyName");
            d.levelName = rs.getString("levelName");
            d.attendanceCount = rs.getInt("attendanceCount");
            return d;
        });
    }

    /**
     * Page (offset) des étudiants pour une session donnée, avec statut d'attendance.
     * Retourne contenu + total pour pagination côté frontend.
     */
    public PagedResult<StudentInSessionDTO> fetchStudentsBySession(Long sessionId, int page, int size, String q) {
        PagedResult<StudentInSessionDTO> res = new PagedResult<>();
        List<Object> params = new ArrayList<>();
        StringBuilder baseWhere = new StringBuilder();
        baseWhere.append(" FROM attendance a ")
                 .append("JOIN student_year_profile syp ON a.student_year_profile_id = syp.id ")
                 .append("JOIN student st ON syp.student_id = st.id ")
                 .append("LEFT JOIN sexe sx ON st.sexe_id = sx.id ")
                 .append(" WHERE a.session_id = ? ");
        params.add(sessionId);

        if (q != null && !q.trim().isEmpty()) {
            baseWhere.append(" AND (LOWER(st.full_name) LIKE ? OR LOWER(st.matricule) LIKE ?) ");
            String qq = "%" + q.toLowerCase().trim() + "%";
            params.add(qq); params.add(qq);
        }

        // count
        String countSql = "SELECT COUNT(*) " + baseWhere.toString();
        long total = jdbc.queryForObject(countSql, params.toArray(), Long.class);

        // data with limit/offset
        int offset = (Math.max(1, page) - 1) * size;
        String dataSql = "SELECT st.id as studentId, st.full_name as studentName, st.matricule as studentMatricule, sx.name as sexe, a.status as status "
                + baseWhere.toString()
                + " ORDER BY st.full_name ASC LIMIT ? OFFSET ?";

        // copy params + limit/offset
        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(size);
        dataParams.add(offset);

        List<StudentInSessionDTO> rows = jdbc.query(dataSql, dataParams.toArray(), new RowMapper<StudentInSessionDTO>() {
            @Override
            public StudentInSessionDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                StudentInSessionDTO d = new StudentInSessionDTO();
                d.studentId = rs.getLong("studentId");
                d.studentName = rs.getString("studentName");
                d.studentMatricule = rs.getString("studentMatricule");
                d.sexe = rs.getString("sexe");
                d.attendanceStatus = rs.getString("status");
                return d;
            }
        });

        res.content = rows;
        res.totalElements = total;
        res.page = page;
        res.size = size;
        return res;
    }
}
