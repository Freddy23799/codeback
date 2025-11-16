package codeqr.code.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;

import codeqr.code.model.JourSemaine;
import codeqr.code.dto.ConflictDto;
import codeqr.code.dto.RowPayload;

/**
 * Implementation propre et lisible du repository d'écriture pour les emplois du temps.
 * - Bind correctement les types SQL (notamment la date)
 * - Utilise des logs clairs
 * - Fournit une variante non-bloquante pour l'acquisition de lock advisory
 * - Retourne maintenant le nom de la salle (existing_room_name) pour messages utilisateur
 * - Ajoute un message d'avertissement / instruction après chaque message de conflit
 */
@Repository
public class TimetableWriteRepositoryImpl implements TimetableWriteRepository {

    private static final Logger log = LoggerFactory.getLogger(TimetableWriteRepositoryImpl.class);

    private final NamedParameterJdbcTemplate jdbc;

    public TimetableWriteRepositoryImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ---------------- basic inserts / updates ---------------- */

    @Override
    public Long insertSemaine(LocalDate dateDebut, LocalDate dateFin, Long createdBy) {
        final String sql = "INSERT INTO semaine_emploi_temps (date_debut, date_fin, created_by_id, created_at) " +
                "VALUES (:dateDebut, :dateFin, :createdBy, :createdAt) RETURNING id";

        Map<String, Object> params = new HashMap<>();
        params.put("dateDebut", dateDebut);
        params.put("dateFin", dateFin);
        params.put("createdBy", createdBy);
        params.put("createdAt", OffsetDateTime.now());

        return jdbc.queryForObject(sql, params, Long.class);
    }

    @Override
    public Long insertEmploi(Long semaineId, Long specialiteId, Long niveauId, Long anneeAcademiqueId, String status,
                             Long createdBy, String title, String notes) {

        final String sql = "INSERT INTO emploi_temps (semaine_id, specialite_id, niveau_id, annee_academique_id, status, created_by_id, title, notes, created_at) " +
                "VALUES (:semaineId, :specialiteId, :niveauId, :anneeAcademiqueId, :status, :createdBy, :title, :notes, :createdAt) RETURNING id";

        Map<String, Object> p = new HashMap<>();
        p.put("semaineId", semaineId);
        p.put("specialiteId", specialiteId);
        p.put("niveauId", niveauId);
        p.put("anneeAcademiqueId", anneeAcademiqueId);
        p.put("status", status == null ? "DRAFT" : status);
        p.put("createdBy", createdBy);
        p.put("title", title);
        p.put("notes", notes);
        p.put("createdAt", OffsetDateTime.now());

        return jdbc.queryForObject(sql, p, Long.class);
    }

    @Override
    public Long insertLigne(Long emploiId, String jour, String heureDebut, String heureFin,
                            Long coursId, Long professeurId, Long salleId, int ordre) {

        if (jour == null || jour.trim().isEmpty()) {
            throw new IllegalArgumentException("Le champ 'jour' est obligatoire.");
        }

        JourSemaine jourEnum;
        try {
            jourEnum = JourSemaine.valueOf(jour.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new IllegalArgumentException("Jour invalide : " + jour, e);
        }
        String jourNorm = jourEnum.name();

        final Time sqlHeureDebut;
        final Time sqlHeureFin;
        try {
            LocalTime ltDebut = LocalTime.parse(heureDebut.trim());
            LocalTime ltFin = LocalTime.parse(heureFin.trim());
            sqlHeureDebut = Time.valueOf(ltDebut);
            sqlHeureFin = Time.valueOf(ltFin);
        } catch (Exception dte) {
            throw new IllegalArgumentException(
                    "Format d'heure invalide (attendu HH:mm ou HH:mm:ss) - debut: " + heureDebut + ", fin: " + heureFin, dte);
        }

        final String sql = "INSERT INTO ligne_emploi_temps (emploi_temps_id, jour, heure_debut, heure_fin, cours_id, professeur_id, salle_id, ordre_small, created_at) " +
                "VALUES (:emploiId, :jour, :heureDebut, :heureFin, :coursId, :profId, :salleId, :ordre, :createdAt) RETURNING id";

        Map<String, Object> p = new HashMap<>();
        p.put("emploiId", emploiId);
        p.put("jour", jourNorm);
        p.put("heureDebut", sqlHeureDebut);
        p.put("heureFin", sqlHeureFin);
        p.put("coursId", coursId);
        p.put("profId", professeurId);
        p.put("salleId", salleId);
        p.put("ordre", ordre);
        p.put("createdAt", OffsetDateTime.now());

        return jdbc.queryForObject(sql, p, Long.class);
    }

    @Override
    public void deleteEmploisAndLignes(Long semaineId) {
        final String deleteLignesSql = "DELETE FROM ligne_emploi_temps WHERE emploi_temps_id IN (SELECT id FROM emploi_temps WHERE semaine_id = :semaineId)";
        jdbc.update(deleteLignesSql, Collections.singletonMap("semaineId", semaineId));

        final String deleteEmploisSql = "DELETE FROM emploi_temps WHERE semaine_id = :semaineId";
        jdbc.update(deleteEmploisSql, Collections.singletonMap("semaineId", semaineId));
    }

    @Override
    public int updateSemaineDates(Long semaineId, LocalDate dateDebut, LocalDate dateFin, Long modifiedBy) {
        final String sql = "UPDATE semaine_emploi_temps SET date_debut = :dateDebut, date_fin = :dateFin, updated_at = :updatedAt WHERE id = :id";
        Map<String, Object> params = new HashMap<>();
        params.put("dateDebut", dateDebut);
        params.put("dateFin", dateFin);
        params.put("updatedAt", OffsetDateTime.now());
        params.put("id", semaineId);

        return jdbc.update(sql, params);
    }

    @Override
    public void deleteSemaineIds(List<Long> idsToDelete) {
        if (idsToDelete == null || idsToDelete.isEmpty()) return;
        final String deleteSemaineSql = "DELETE FROM semaine_emploi_temps WHERE id IN (:ids)";
        jdbc.update(deleteSemaineSql, Collections.singletonMap("ids", idsToDelete));
    }

    @Override
    public List<Long> findOldSemaineIdsToDelete(Long createdBy, int keepLatestN) {
        final String sql = "SELECT id FROM semaine_emploi_temps " +
                "WHERE created_by_id = :createdBy " +
                "ORDER BY date_debut DESC " +
                "OFFSET :offset";

        Map<String, Object> params = new HashMap<>();
        params.put("createdBy", createdBy);
        params.put("offset", keepLatestN);

        return jdbc.queryForList(sql, params, Long.class);
    }

    /* ---------------- advisory lock (try, non bloquant) ---------------- */

    @Override
    public void acquireAdvisoryLockForWeek(LocalDate weekStart) {
        if (weekStart == null) throw new IllegalArgumentException("weekStart ne peut pas être null");

        // Utilise pg_try_advisory_xact_lock pour éviter blocage infini. Si l'acquisition échoue, on lève une
        // exception afin que l'appelant puisse réessayer ou renvoyer une erreur utilisateur claire.
        jdbc.getJdbcTemplate().execute((ConnectionCallback<Void>) con -> {
            final String sql = "SELECT pg_try_advisory_xact_lock(hashtext(?))";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, weekStart.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        boolean acquired = rs.getBoolean(1);
                        if (!acquired) {
                            throw new RuntimeException("Impossible d'acquérir le verrou advisory pour la semaine: " + weekStart);
                        }
                        if (log.isDebugEnabled()) log.debug("Advisory lock acquis pour la semaine {}", weekStart);
                    }
                }
            }
            return null;
        });
    }

    /* ---------------- conflict detection (PreparedStatement + JSONB) ---------------- */

    @Override
    public List<ConflictDto> findConflictsForWeek(LocalDate weekStart, List<RowPayload> candidates, Long excludeSemaineId) {
        if (candidates == null || candidates.isEmpty()) return Collections.emptyList();

        final String candidatesJson = buildCandidatesJson(candidates);
        if (log.isDebugEnabled()) {
            log.debug("findConflictsForWeek: weekStart={}, candidatesSize={}, excludeSemaineId={}", weekStart, candidates.size(), excludeSemaineId);
        }

        // Texte d'avertissement ajouté à tous les messages de conflit
        final String ADVICE = " Veuillez contacter ce/ces responsable(s) pour vérification. Ou bien essayez de changer votre emploi du temps. À défaut, vous ne pourrez pas enregistrer cette semaine — sécurité et intégrité des données.";

        return jdbc.getJdbcTemplate().execute((ConnectionCallback<List<ConflictDto>>) con -> {
            String sql =
                    "WITH candidates AS (\n" +
                    "  SELECT\n" +
                    "    (elem->>'jour') AS jour,\n" +
                    "    (elem->>'start') AS start_time,\n" +
                    "    (elem->>'end') AS end_time,\n" +
                    "    NULLIF(elem->>'prof','')::bigint AS professeur_id,\n" +
                    "    NULLIF(elem->>'room','')::bigint AS salle_id,\n" +
                    "    (elem->>'idx')::int AS candidate_idx\n" +
                    "  FROM jsonb_array_elements(?::jsonb) elem\n" +
                    ")\n" +
                    "SELECT c.candidate_idx,\n" +
                    "       c.jour AS candidate_jour,\n" +
                    "       l.id AS ligne_id,\n" +
                    "       l.emploi_temps_id,\n" +
                    "       e.semaine_id,\n" +
                    "       s.date_debut,\n" +
                    "       resp.full_name AS responsable_name,\n" +
                    "       s.created_by_id AS responsable_id,\n" +
                    "       l.professeur_id AS existing_professeur_id,\n" +
                    "       t.full_name AS professeur_name,\n" +
                    "       l.salle_id AS existing_salle_id,\n" +
                    "       rm.name AS existing_room_name,\n" +
                    "       to_char(l.heure_debut, 'HH24:MI') AS existing_start,\n" +
                    "       to_char(l.heure_fin, 'HH24:MI') AS existing_end,\n" +
                    "       c.professeur_id AS candidate_professor_id,\n" +
                    "       c.salle_id AS candidate_salle_id\n" +
                    "FROM candidates c\n" +
                    "JOIN ligne_emploi_temps l ON l.jour = c.jour\n" +
                    "JOIN emploi_temps e ON l.emploi_temps_id = e.id\n" +
                    "JOIN semaine_emploi_temps s ON e.semaine_id = s.id\n" +
                    "LEFT JOIN responsable resp ON resp.id = s.created_by_id\n" +
                    "LEFT JOIN teacher t ON t.id = l.professeur_id\n" +
                    "LEFT JOIN room rm ON rm.id = l.salle_id\n" +
                    "WHERE s.date_debut = ?\n" +
                    "  AND ( (l.heure_debut < c.end_time::time) AND (c.start_time::time < l.heure_fin) )\n" +
                    "  AND ( (c.professeur_id IS NOT NULL AND l.professeur_id = c.professeur_id) OR (c.salle_id IS NOT NULL AND l.salle_id = c.salle_id) )\n";

            if (excludeSemaineId != null) {
                sql += "  AND e.semaine_id <> ?\n";
            }

            if (log.isTraceEnabled()) log.trace("SQL detect conflicts:\n{}", sql);

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int idx = 1;
                // 1) JSONB payload
                ps.setString(idx++, candidatesJson);

                // 2) date -> binder en java.sql.Date pour éviter l'erreur de type (date = varchar)
                ps.setDate(idx++, java.sql.Date.valueOf(weekStart));

                if (excludeSemaineId != null) {
                    ps.setLong(idx++, excludeSemaineId);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    List<ConflictDto> conflicts = new ArrayList<>();
                    while (rs.next()) {
                        ConflictDto dto = new ConflictDto();
                        dto.setCandidateIndex(rs.getInt("candidate_idx"));
                        dto.setExistingLigneId(rs.getLong("ligne_id"));
                        dto.setExistingEmploiId(rs.getLong("emploi_temps_id"));
                        dto.setSemaineId(rs.getLong("semaine_id"));
                        if (rs.getDate("date_debut") != null) {
                            dto.setSemaineDateDebut(rs.getDate("date_debut").toLocalDate());
                        }
                        dto.setExistingResponsibleId(rs.getObject("responsable_id") == null ? null : rs.getLong("responsable_id"));
                        dto.setResponsibleName(rs.getString("responsable_name"));
                        Long existingProfId = rs.getObject("existing_professeur_id") == null ? null : rs.getLong("existing_professeur_id");
                        dto.setExistingProfessorId(existingProfId);
                        dto.setProfessorName(rs.getString("professeur_name"));
                        dto.setExistingRoomId(rs.getObject("existing_salle_id") == null ? null : rs.getLong("existing_salle_id"));
                        dto.setExistingRoomName(rs.getString("existing_room_name"));
                        dto.setExistingStart(rs.getString("existing_start"));
                        dto.setExistingEnd(rs.getString("existing_end"));

                        Long candProf = rs.getObject("candidate_professor_id") == null ? null : rs.getLong("candidate_professor_id");
                        Long candRoom = rs.getObject("candidate_salle_id") == null ? null : rs.getLong("candidate_salle_id");
                        String candidateJour = rs.getString("candidate_jour");

                        if (candProf != null && existingProfId != null && candProf.equals(existingProfId)) {
                            dto.setType("PROF");
                            String profName = dto.getProfessorName() == null ? ("id=" + existingProfId) : dto.getProfessorName();
                            String respName = dto.getResponsibleName() == null ? "Responsable inconnu" : dto.getResponsibleName();
                            String base = String.format("Le professeur %s est déjà programmé le %s %s-%s par %s (emploiId=%d).",
                                    profName, candidateJour, dto.getExistingStart(), dto.getExistingEnd(), respName, dto.getExistingEmploiId());
                            dto.setMessage(base + ADVICE);
                        } else {
                            dto.setType("ROOM");
                            String respName = dto.getResponsibleName() == null ? "Responsable inconnu" : dto.getResponsibleName();
                            String roomLabel = (dto.getExistingRoomName() != null && !dto.getExistingRoomName().isBlank())
                                    ? dto.getExistingRoomName()
                                    : (dto.getExistingRoomId() == null ? "inconnue" : "id=" + dto.getExistingRoomId());
                            String base = String.format("La salle %s est déjà occupée le %s %s-%s (emploiId=%d) par %s.",
                                    roomLabel, candidateJour, dto.getExistingStart(), dto.getExistingEnd(), dto.getExistingEmploiId(), respName);
                            dto.setMessage(base + ADVICE);
                        }

                        conflicts.add(dto);
                    }
                    return conflicts;
                }
            } catch (SQLException ex) {
                log.error("Erreur SQL lors de la détection des conflits", ex);
                throw new RuntimeException("Erreur SQL lors de la détection des conflits: " + ex.getMessage(), ex);
            }
        });
    }

    /* ---------------- helper: build JSON payload ---------------- */

    private String buildCandidatesJson(List<RowPayload> candidates) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < candidates.size(); i++) {
            RowPayload r = candidates.get(i);
            if (i > 0) sb.append(',');
            sb.append('{');

            sb.append("\"jour\":").append(jsonString(r.getJour() == null ? "" : r.getJour().trim().toUpperCase())).append(',');
            sb.append("\"start\":").append(jsonString(r.getStart() == null ? "" : r.getStart())).append(',');
            sb.append("\"end\":").append(jsonString(r.getEnd() == null ? "" : r.getEnd())).append(',');

            if (r.getProfessorId() == null) sb.append("\"prof\":null,");
            else sb.append("\"prof\":").append(r.getProfessorId()).append(',');

            if (r.getRoomId() == null) sb.append("\"room\":null,");
            else sb.append("\"room\":").append(r.getRoomId()).append(',');

            sb.append("\"idx\":").append(i);

            sb.append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    private String jsonString(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
