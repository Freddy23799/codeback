package codeqr.code.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.RequiredArgsConstructor;
import codeqr.code.dto.TeacherListDTO;

@Repository
@RequiredArgsConstructor
public class TeacherRepositoryImpl implements TeacherRepositoryCustom {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper(); // Jackson pour parser JSON

    @Override
    public List<TeacherListDTO> fetchTeachersLight(Long cursorId, int limit, String q) {
        int fetchLimit = Math.max(1, limit) + 1;

        // Base SQL avec agrégation JSON pour les cours
        StringBuilder base = new StringBuilder();
        base.append("SELECT t.id, t.full_name, t.matricule, t.email, ")
            .append("u.username, ")
            .append("t.sexe_id AS sexeId, sx.name AS sexe_name, ")
            .append("(SELECT COUNT(*) FROM teacher_year_profile typ WHERE typ.teacher_id = t.id) AS profile_count, ")
            .append("COALESCE((SELECT json_agg(json_build_object('id', c.id, 'code', c.code, 'title', c.title)) ")
            .append("          FROM teacher_courses tc ")
            .append("          JOIN course c ON c.id = tc.course_id ")
            .append("          WHERE tc.teacher_id = t.id), '[]') AS courses ")
            .append("FROM teacher t ")
            .append("LEFT JOIN sexe sx ON t.sexe_id = sx.id ")
            .append("LEFT JOIN app_user u ON t.user_id = u.id ")
            .append("WHERE 1=1 ");

        List<Object> baseParams = new ArrayList<>();
        if (q != null && !q.trim().isEmpty()) {
            base.append(" AND (LOWER(t.full_name) LIKE ? ")
                .append("OR LOWER(t.matricule) LIKE ? ")
                .append("OR LOWER(t.email) LIKE ? ")
                .append("OR LOWER(u.username) LIKE ?) ");
            String qq = "%" + q.toLowerCase() + "%";
            baseParams.add(qq);
            baseParams.add(qq);
            baseParams.add(qq);
            baseParams.add(qq);
        }

        // Helper pour exécuter la requête
        var execQuery = (java.util.function.Function<Long, List<TeacherListDTO>>) (maybeCursor) -> {
            StringBuilder sb = new StringBuilder(base.toString());
            List<Object> params = new ArrayList<>(baseParams);

            if (maybeCursor != null && maybeCursor > 0) {
                sb.append(" AND t.id > ? ");
                params.add(maybeCursor);
            }

            sb.append(" ORDER BY t.id ASC LIMIT ? ");
            params.add(fetchLimit);

            return jdbc.query(sb.toString(), params.toArray(), new RowMapper<TeacherListDTO>() {
                @Override
                public TeacherListDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                    TeacherListDTO d = new TeacherListDTO();
                    d.setId(rs.getLong("id"));
                    d.setFullName(rs.getString("full_name"));
                    d.setMatricule(rs.getString("matricule"));
                    d.setEmail(rs.getString("email"));
                    d.setSexeId(rs.getLong("sexeId"));
                    d.setSexeName(rs.getString("sexe_name"));
                    d.setProfileCount(rs.getInt("profile_count"));
                    d.setUsername(rs.getString("username"));

                    // ---- CORRECTION ICI pour les cours ----
                    Object obj = rs.getObject("courses");
                    if (obj != null) {
                        try {
                            String json = obj.toString();
                            List<Map<String, Object>> coursesList = mapper.readValue(
                                json, new TypeReference<List<Map<String, Object>>>() {}
                            );
                            d.setCourses(coursesList);
                        } catch (Exception e) {
                            d.setCourses(new ArrayList<>());
                            e.printStackTrace();
                        }
                    } else {
                        d.setCourses(new ArrayList<>());
                    }

                    return d;
                }
            });
        };

        // 1) Normal query avec cursor
        List<TeacherListDTO> rows = execQuery.apply(cursorId);

        // 2) Si recherche avec q et rien trouvé avec cursor → on ignore le cursor
        if ((rows == null || rows.isEmpty()) && q != null && !q.trim().isEmpty()
                && cursorId != null && cursorId > 0) {
            rows = execQuery.apply(null);
        }

        return rows != null ? rows : new ArrayList<>();
    }
}
