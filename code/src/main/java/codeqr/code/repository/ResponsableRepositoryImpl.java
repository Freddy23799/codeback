// file: codeqr/code/repository/SurveillantRepositoryImpl.java
package codeqr.code.repository;

import codeqr.code.dto.ResponsableListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ResponsableRepositoryImpl implements ResponsableRepositoryCustom {

    private final JdbcTemplate jdbc;

    @Override
    public List<ResponsableListDTO> fetchResponsablesLight(Long cursorId, int limit, String q) {
        int fetchLimit = Math.max(1, limit) + 1;

        StringBuilder base = new StringBuilder();
        base.append("SELECT s.id, s.matricule, s.full_name, s.email, ")
            .append("u.username, sx.name AS sexe_name ")
            .append("FROM responsable s ")
            .append("LEFT JOIN app_user u ON s.user_id = u.id ") // PostgreSQL compatible
            .append("LEFT JOIN sexe sx ON s.sexe_id = sx.id ")
            .append("WHERE 1=1 ");

        List<Object> baseParams = new ArrayList<>();
        if (q != null && !q.trim().isEmpty()) {
            base.append("AND (LOWER(s.full_name) LIKE ? OR LOWER(s.matricule) LIKE ? ")
                .append("OR LOWER(s.email) LIKE ? OR LOWER(u.username) LIKE ?) ");
            String qq = "%" + q.toLowerCase() + "%";
            baseParams.add(qq); baseParams.add(qq); baseParams.add(qq); baseParams.add(qq);
        }

        var execQuery = (java.util.function.Function<Long, List<ResponsableListDTO>>) (maybeCursor) -> {
            StringBuilder sb = new StringBuilder(base.toString());
            List<Object> params = new ArrayList<>(baseParams);

            if (maybeCursor != null && maybeCursor > 0) {
                sb.append("AND s.id > ? ");
                params.add(maybeCursor);
            }

            sb.append("ORDER BY s.id ASC LIMIT ? ");
            params.add(fetchLimit);

            return jdbc.query(sb.toString(), params.toArray(), new RowMapper<ResponsableListDTO>() {
                @Override
                public ResponsableListDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                    ResponsableListDTO d = new ResponsableListDTO();
                    d.setId(rs.getLong("id"));
                    d.setMatricule(rs.getString("matricule"));
                    d.setFullName(rs.getString("full_name"));
                    d.setEmail(rs.getString("email"));
                    d.setUsername(rs.getString("username"));
                    d.setSexeName(rs.getString("sexe_name"));
                    return d;
                }
            });
        };

        List<ResponsableListDTO> rows = execQuery.apply(cursorId);

        if ((rows == null || rows.isEmpty()) && q != null && !q.trim().isEmpty()
                && cursorId != null && cursorId > 0) {
            rows = execQuery.apply(null);
        }

        return rows != null ? rows : new ArrayList<>();
    }
}
