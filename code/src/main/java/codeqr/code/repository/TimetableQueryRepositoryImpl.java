package codeqr.code.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class TimetableQueryRepositoryImpl implements TimetableQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public TimetableQueryRepositoryImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Requête SQL pour récupérer la dernière semaine d'emploi du temps
     * pour un utilisateur donné sous forme JSON.
     * 
     * - Utilisation de CTE (WITH last_week AS ...)
     * - Conversion des dates en format 'YYYY-MM-DD'
     * - Aggregation JSON avec jsonb_build_object et jsonb_agg
     */
    private static final String SQL =
        "WITH last_week AS (\n" +
        "  SELECT id, date_debut, date_fin\n" +
        "  FROM semaine_emploi_temps\n" +
        "  WHERE created_by_id = :userId\n" +
        "  ORDER BY date_debut DESC\n" +
        "  LIMIT 1\n" +
        ")\n" +
        "SELECT (jsonb_build_object(\n" +
        "  'semaine', jsonb_build_object(\n" +
        "      'id', lw.id, \n" +
        "      'dateDebut', to_char(lw.date_debut, 'YYYY-MM-DD'), \n" +
        "      'dateFin', to_char(lw.date_fin, 'YYYY-MM-DD')\n" +
        "  ),\n" +
        "  'emplois', (\n" +
        "    SELECT coalesce(jsonb_agg(jsonb_build_object(\n" +
        "      'id', e.id,\n" +
        "      'specialiteId', e.specialite_id,\n" +
        "      'niveauId', e.niveau_id,\n" +
        "      'anneeAcademiqueId', e.annee_academique_id,\n" +
        "      'status', e.status,\n" +
        "      'rows', (\n" +
        "        SELECT coalesce(jsonb_agg(jsonb_build_object(\n" +
        "          'jour', l.jour,\n" +
        "          'start', to_char(l.heure_debut,'HH24:MI'),\n" +
        "          'end', to_char(l.heure_fin,'HH24:MI'),\n" +
        "          'courseId', l.cours_id,\n" +
        "          'courseName', c.title,\n" +
        "          'professorId', l.professeur_id,\n" +
        "          'professorName', p.full_name,\n" +
        "          'roomId', l.salle_id,\n" +
        "          'roomName', r.name\n" +
        "        ) ORDER BY l.jour, l.heure_debut), '[]'::jsonb)\n" +
        "        FROM ligne_emploi_temps l\n" +
        "        LEFT JOIN course c ON c.id = l.cours_id\n" +
        "        LEFT JOIN teacher p ON p.id = l.professeur_id\n" +
        "        LEFT JOIN room r ON r.id = l.salle_id\n" +
        "        WHERE l.emploi_temps_id = e.id\n" +
        "      )\n" +
        "    )), '[]'::jsonb)\n" +
        "    FROM emploi_temps e\n" +
        "    WHERE e.semaine_id = lw.id\n" +
        "  )\n" +
        "))::text AS result\n" +
        "FROM last_week lw;";

    /**
     * Récupère la dernière semaine d'emploi du temps d'un utilisateur
     * sous forme JSON. Retourne null si aucune donnée trouvée.
     */
    @Override
    public String fetchLastWeekJson(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        try {
            // Retourne la colonne unique contenant le JSON
            return jdbc.queryForObject(SQL, params, String.class);
        } catch (EmptyResultDataAccessException ex) {
            // Aucun résultat pour cet utilisateur
            return null;
        }
    }
}
