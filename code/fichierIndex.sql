-- CREATE INDEX idx_session_surveillant ON session (surveillant_id);
-- CREATE INDEX idx_session_expected_specialty ON session (expected_Specialty_id);
-- CREATE INDEX idx_session_expected_level ON session (expected_Level_id);
-- CREATE INDEX idx_session_starttime ON session (start_time);

-- CREATE INDEX idx_attendance_session ON attendance (session_id);
-- CREATE INDEX idx_attendance_status ON attendance (status);

-- -- si tu fais souvent search par teacher.name
-- CREATE INDEX idx_teacher_fullname ON teacher (full_name);



-- -- 🔥 Index pour optimiser les filtres sur student_year_profile
-- CREATE INDEX IF NOT EXISTS idx_syp_level_specialty_year 
--     ON student_year_profile (level_id, specialty_id, academic_year_id);

-- -- 🔥 Index pour accélérer les ORDER BY sur les noms d'étudiants
-- CREATE INDEX IF NOT EXISTS idx_student_fullname 
--     ON student (full_name);

-- -- 🔥 Index pour optimiser les jointures attendance -> session + profile
-- CREATE INDEX IF NOT EXISTS idx_attendance_session_profile 
--     ON attendance (session_id, student_year_profile_id);

-- -- 🔒 (Optionnel mais recommandé) Unicité : un étudiant par session max
-- CREATE UNIQUE INDEX IF NOT EXISTS uq_attendance_session_profile 
--     ON attendance (session_id, student_year_profile_id);

-- -- ⚡ (Optionnel) Si souvent tu filtres sur status = 'PRESENT'
-- CREATE INDEX IF NOT EXISTS idx_attendance_present 
--     ON attendance (session_id, student_year_profile_id) 
--     WHERE status = 'PRESENT';







-- -- activer l'extension trigram (si autorisé)
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- -- 1) Utilitaires pour les recherches texte (ILIKE) :
-- CREATE INDEX IF NOT EXISTS idx_course_title_trgm ON course USING gin (title gin_trgm_ops);
-- CREATE INDEX IF NOT EXISTS idx_room_name_trgm   ON room   USING gin (name gin_trgm_ops);
-- CREATE INDEX IF NOT EXISTS idx_campus_name_trgm ON campus USING gin (name gin_trgm_ops);
-- CREATE INDEX IF NOT EXISTS idx_teacher_fullname_trgm ON teacher USING gin (full_name gin_trgm_ops);

-- -- 2) Indexes pour jointures / filtres fréquents
-- CREATE INDEX IF NOT EXISTS idx_session_user_id       ON session (user_id);
-- CREATE INDEX IF NOT EXISTS idx_session_start_time    ON session (start_time);
-- CREATE INDEX IF NOT EXISTS idx_session_course_id     ON session (course_id);
-- CREATE INDEX IF NOT EXISTS idx_session_room_id       ON session (room_id);
-- CREATE INDEX IF NOT EXISTS idx_session_campus_id     ON session (campus_id);
-- CREATE INDEX IF NOT EXISTS idx_session_expected_level ON session (expected_level_id);
-- CREATE INDEX IF NOT EXISTS idx_session_expected_specialty ON session (expected_specialty_id);
-- CREATE INDEX IF NOT EXISTS idx_session_surveillant_id ON session (surveillant_id);

-- -- 3) Attendance (très utilisé pour count / group by / where session_id)
-- CREATE INDEX IF NOT EXISTS idx_attendance_session_id ON attendance (session_id);
-- CREATE INDEX IF NOT EXISTS idx_attendance_status     ON attendance (status);
-- -- multi-col index utile si on filtre par session + status
-- CREATE INDEX IF NOT EXISTS idx_attendance_session_status ON attendance (session_id, status);

-- -- 4) app_user / user (si tu utilises app_user)
-- CREATE INDEX IF NOT EXISTS idx_app_user_username ON app_user (username);
-- CREATE INDEX IF NOT EXISTS idx_app_user_email    ON app_user (email);

-- -- 5) Optional: si tu fais souvent ORDER BY start_time DESC avec limites
-- -- (un index partiel/desc peut aider)
-- CREATE INDEX IF NOT EXISTS idx_session_start_time_desc ON session (start_time DESC);

-- -- 6) Si tu fais fréquemment COUNT+GROUP BY sur session+campus+room etc,
-- -- vérifie les plans EXPLAIN; ces indexes ci-dessous couvrent les FK/col utilisées:
-- CREATE INDEX IF NOT EXISTS idx_room_name ON room (name);
-- CREATE INDEX IF NOT EXISTS idx_campus_name ON campus (name);
-- CREATE INDEX IF NOT EXISTS idx_course_title ON course (title);


-- -- index pour recherche/weeks par créateur + ordre
-- CREATE INDEX IF NOT EXISTS idx_semaine_created_by_date_debut_desc ON semaine_emploi_temps(created_by_id, date_debut DESC);

-- -- index pour emploi_temps par semaine
-- CREATE INDEX IF NOT EXISTS idx_emploi_semaine ON emploi_temps(semaine_id);

-- -- index pour lignes (recherche par emploi + tri)
-- CREATE INDEX IF NOT EXISTS idx_ligne_emploi_temps_emploi_jour_heure ON ligne_emploi_temps(emploi_temps_id, jour, heure_debut);

-- -- indexs FK habituels (si pas déjà PK indexes)
-- CREATE INDEX IF NOT EXISTS idx_emploi_created_by ON emploi_temps(created_by_id);

























-- -- ===============================================
-- -- 0. Supprimer le trigger et la fonction existants
-- -- ===============================================
-- DROP TRIGGER IF EXISTS trg_limit_semaine_emploi_temps ON semaine_emploi_temps;
-- DROP FUNCTION IF EXISTS limit_semaine_emploi_temps();

-- -- ===============================================
-- -- 1. Mettre les clés étrangères en ON DELETE CASCADE
-- -- ===============================================

-- -- Semaine → EmploiTemps
-- ALTER TABLE emploi_temps
-- DROP CONSTRAINT IF EXISTS fkv5978lnnkvt6r9dldescpxn6;

-- ALTER TABLE emploi_temps
-- ADD CONSTRAINT fkv5978lnnkvt6r9dldescpxn6
-- FOREIGN KEY (semaine_id) REFERENCES semaine_emploi_temps(id)
-- ON DELETE CASCADE;

-- -- EmploiTemps → LigneEmploiTemps
-- ALTER TABLE ligne_emploi_temps
-- DROP CONSTRAINT IF EXISTS fkipsql725ljtj1io215jun4imf;

-- ALTER TABLE ligne_emploi_temps
-- ADD CONSTRAINT fkipsql725ljtj1io215jun4imf
-- FOREIGN KEY (emploi_temps_id) REFERENCES emploi_temps(id)
-- ON DELETE CASCADE;

-- -- ===============================================
-- -- 2. Créer la fonction trigger
-- -- ===============================================
-- CREATE OR REPLACE FUNCTION limit_semaine_emploi_temps()
-- RETURNS TRIGGER AS $$
-- BEGIN
--     -- Supprimer les semaines les plus anciennes si un responsable dépasse 2
--     DELETE FROM semaine_emploi_temps
--     WHERE id IN (
--         SELECT id
--         FROM semaine_emploi_temps
--         WHERE created_by_id = NEW.created_by_id
--         ORDER BY created_at ASC
--         OFFSET 2  -- Conserve les 2 plus récentes
--     );

--     RETURN NEW;
-- END;
-- $$ LANGUAGE plpgsql;

-- -- ===============================================
-- -- 3. Créer le trigger qui s'exécute après chaque insertion
-- -- ===============================================
-- CREATE TRIGGER trg_limit_semaine_emploi_temps
-- AFTER INSERT ON semaine_emploi_temps
-- FOR EACH ROW
-- EXECUTE FUNCTION limit_semaine_emploi_temps();











-- -- 1. Supprimer le trigger s'il existe
-- DROP TRIGGER IF EXISTS tg_check_semaine_creation ON semaine_emploi_temps;

-- -- 2. Supprimer la fonction s'il existe
-- DROP FUNCTION IF EXISTS check_semaine_creation();

-- -- 3. Recréer la fonction
-- CREATE OR REPLACE FUNCTION check_semaine_creation()
-- RETURNS TRIGGER AS $$
-- DECLARE
--     current_monday date;
--     next_monday date;
--     week_after_next date;
--     exists_current_week BOOLEAN;
-- BEGIN
--     ------------------------------------------------------------------------
--     -- Calcul de la semaine courante (lundi)
--     ------------------------------------------------------------------------
--     current_monday := CURRENT_DATE - ((EXTRACT(DOW FROM CURRENT_DATE)::int + 6) % 7);

--     -- Calcul du lundi suivant (semaine suivante)
--     next_monday := current_monday + 7;

--     -- Calcul de la semaine après la semaine suivante
--     week_after_next := current_monday + 14;

--     ------------------------------------------------------------------------
--     -- 1ère vérification : bloquer si date_debut > semaine suivante
--     ------------------------------------------------------------------------
--     IF NEW.date_debut > next_monday THEN
--         RAISE EXCEPTION
--         'Vous ne pouvez pas créer un emploi du temps pour une semaine supérieure à la semaine suivante (%).',
--         next_monday;
--     END IF;

--     ------------------------------------------------------------------------
--     -- 2ème vérification : si l'utilisateur crée la semaine suivante,
--     -- vérifier que la semaine courante existe
--     ------------------------------------------------------------------------
--     IF NEW.date_debut = next_monday THEN
--         SELECT EXISTS (
--             SELECT 1
--             FROM semaine_emploi_temps
--             WHERE created_by_id = NEW.created_by_id
--             AND date_debut = current_monday
--         ) INTO exists_current_week;

--         IF NOT exists_current_week THEN
--             RAISE EXCEPTION
--             'Vous ne pouvez pas créer la semaine du lundi % car la semaine courante du lundi % doit d’abord exister.',
--             next_monday, current_monday;
--         END IF;
--     END IF;

--     -- Si date_debut = current_monday (semaine courante) → pas de blocage
--     RETURN NEW;
-- END;
-- $$ LANGUAGE plpgsql;

-- -- 4. Recréer le trigger
-- CREATE TRIGGER tg_check_semaine_creation
-- BEFORE INSERT OR UPDATE ON semaine_emploi_temps
-- FOR EACH ROW
-- EXECUTE FUNCTION check_semaine_creation();
