
// // // package codeqr.code.config;

// // // import org.springframework.boot.context.event.ApplicationReadyEvent;
// // // import org.springframework.context.ApplicationListener;
// // // import org.springframework.core.Ordered;
// // // import org.springframework.core.annotation.Order;
// // // import org.springframework.jdbc.core.BatchPreparedStatementSetter;
// // // import org.springframework.jdbc.core.JdbcTemplate;
// // // import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
// // // import org.springframework.stereotype.Component;
// // // import org.springframework.beans.factory.annotation.Value;
// // // import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// // // import org.springframework.security.crypto.password.PasswordEncoder;

// // // import java.sql.PreparedStatement;
// // // import java.sql.SQLException;
// // // import java.time.Instant;
// // // import java.util.*;
// // // import java.util.concurrent.*;
// // // import java.util.stream.Collectors;
// // // import java.util.concurrent.atomic.AtomicBoolean;

// // // /**
// // //  * DataCiderRunnerAuto
// // //  * - s'exécute automatiquement au démarrage (ApplicationReadyEvent)
// // //  * - évite double-exécution (devtools) via context parent check
// // //  * - exécute le seeding en tâche asynchrone par défaut (configurable)
// // //  *
// // //  * Ajuste les CONSTANTS TABLE/COL si ta naming strategy diffère.
// // //  */
// // // @Component
// // // @Order(Ordered.HIGHEST_PRECEDENCE)
// // // public class DataCiderRunnerAuto implements ApplicationListener<ApplicationReadyEvent> {

// // //     private final JdbcTemplate jdbc;
// // //     private final NamedParameterJdbcTemplate namedJdbc;
// // //     private final PasswordEncoder passwordEncoder;

// // //     // Guard to avoid double run
// // //     private final AtomicBoolean started = new AtomicBoolean(false);

// // //     // ========== CONFIG ==========
// // //     // Si ta DB utilise d'autres noms, change ici.
// // //     private static final String TABLE_USER = "app_user";
// // //     private static final String TABLE_STUDENT = "student";
// // //     private static final String TABLE_STUDENT_YEAR = "student_year_profile";
// // //     private static final String TABLE_SPECIALTY = "specialty";
// // //     private static final String TABLE_LEVEL = "level";
// // //     private static final String TABLE_ACADEMIC_YEAR = "academic_year";
// // //     private static final String TABLE_SEXE = "sexe";

// // //     // Number of students per specialty × level
// // //     private static final int STUDENTS_PER_COMBINATION = 100;

// // //     // Batch sizes (tweak to match DB memory)
// // //     private static final int USER_INSERT_BATCH = 500; // insert users in chunks
// // //     private static final int STUDENT_INSERT_BATCH = 500;
// // //     private static final int YEARPROFILE_INSERT_BATCH = 500;

// // //     // Parallelism (number of threads performing combinations in parallel)
// // //     private static final int PARALLELISM = 4; // si ta DB peut supporter, augmente; sinon =1

// // //     // Academic year id to use (as requested)
// // //     private static final long ACADEMIC_YEAR_ID = 3L;
// // //     // ============================

// // //     @Value("${seeder.enabled:true}")
// // //     private boolean enabled;

// // //     @Value("${seeder.runAsync:true}")
// // //     private boolean runAsync;

// // //     public DataCiderRunnerAuto(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
// // //         this.jdbc = jdbc;
// // //         this.namedJdbc = namedJdbc;
// // //         this.passwordEncoder = new BCryptPasswordEncoder(8); // configurable strength
// // //     }

// // //     @Override
// // //     public void onApplicationEvent(ApplicationReadyEvent event) {
// // //         // Avoid double-run in child contexts (DevTools)
// // //         if (event.getApplicationContext().getParent() != null) return;

// // //         if (!enabled) {
// // //             System.out.println("Seeder disabled (seeder.enabled=false). Skipping.");
// // //             return;
// // //         }

// // //         if (!started.compareAndSet(false, true)) return;

// // //         Runnable job = () -> {
// // //             try {
// // //                 runSeeder();
// // //             } catch (Throwable t) {
// // //                 t.printStackTrace();
// // //             }
// // //         };

// // //         if (runAsync) {
// // //             ExecutorService ex = Executors.newSingleThreadExecutor(r -> {
// // //                 Thread th = new Thread(r, "data-cider-seeder");
// // //                 th.setDaemon(true);
// // //                 return th;
// // //             });
// // //             ex.submit(job);
// // //             ex.shutdown();
// // //         } else {
// // //             job.run();
// // //         }
// // //     }

// // //     // The seeding logic (adapted from previous DataCiderRunner)
// // //     private void runSeeder() throws Exception {
// // //         long start = System.currentTimeMillis();

// // //         // validations: academic year, specialties, levels
// // //         if (!existsAcademicYear(ACADEMIC_YEAR_ID)) {
// // //             System.err.println("AcademicYear id=" + ACADEMIC_YEAR_ID + " introuvable. Aborting.");
// // //             return;
// // //         }

// // //         List<IdName> specialties = loadIdNameList(TABLE_SPECIALTY);
// // //         List<IdName> levels = loadIdNameList(TABLE_LEVEL);
// // //         Long sexeId = loadAnySexeId();

// // //         if (specialties.isEmpty()) {
// // //             System.err.println("Aucune specialty trouvée. Aborting.");
// // //             return;
// // //         }
// // //         if (levels.isEmpty()) {
// // //             System.err.println("Aucun level trouvé. Aborting.");
// // //             return;
// // //         }
// // //         if (sexeId == null) {
// // //             System.err.println("Aucun sexe trouvé (table '" + TABLE_SEXE + "'). Aborting.");
// // //             return;
// // //         }

// // //         System.out.println("Found specialties: " + specialties.size() + ", levels: " + levels.size() + ", sexeId: " + sexeId);
// // //         System.out.println("Starting bulk creation: " + STUDENTS_PER_COMBINATION + " students per (specialty×level).");

// // //         // prepare thread pool
// // //         ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, PARALLELISM));
// // //         List<Callable<Stat>> tasks = new ArrayList<>();

// // //         for (IdName spec : specialties) {
// // //             for (IdName lvl : levels) {
// // //                 tasks.add(() -> createForCombination(spec.id, lvl.id, sexeId));
// // //             }
// // //         }

// // //         // run tasks with parallelism
// // //         List<Future<Stat>> results = executor.invokeAll(tasks);
// // //         executor.shutdown();
// // //         executor.awaitTermination(1, TimeUnit.HOURS);

// // //         // aggregate stats
// // //         long totalCreated = 0;
// // //         long totalTime = 0;
// // //         for (Future<Stat> f : results) {
// // //             Stat s = f.get();
// // //             totalCreated += s.count;
// // //             totalTime += s.millis;
// // //         }

// // //         long elapsed = System.currentTimeMillis() - start;
// // //         System.out.println("======== DONE ========");
// // //         System.out.println("Total students created: " + totalCreated);
// // //         System.out.println("Total time ms (sum of tasks): " + totalTime + " ms");
// // //         System.out.println("Wall-clock elapsed: " + elapsed + " ms");
// // //     }

// // //     // create for one specialty × level combination
// // //     private Stat createForCombination(Long specialtyId, Long levelId, Long sexeId) {
// // //         long t0 = System.currentTimeMillis();
// // //         String comboLabel = "spec#" + specialtyId + "-lvl#" + levelId;
// // //         System.out.println("Start " + comboLabel);

// // //         int total = STUDENTS_PER_COMBINATION;
// // //         List<String> allUsernames = new ArrayList<>(total);
// // //         List<UserRow> userRows = new ArrayList<>(total);
// // //         List<StudentRow> studentRows = new ArrayList<>(total);

// // //         // prepare data
// // //         for (int i = 1; i <= total; i++) {
// // //             String uidSuffix = UUID.randomUUID().toString().substring(0, 8);
// // //             String username = String.format("u_s%02d_l%02d_%04d_%s", specialtyId % 100, levelId % 100, i, uidSuffix);
// // //             String email = username + "@seed.local";
// // //             String matricule = String.format("M-%02d-%02d-%05d", specialtyId % 100, levelId % 100, i);
// // //             String fullName = "Student " + specialtyId + "-" + levelId + "-" + i;
// // //             String password = passwordEncoder.encode("ChangeMe123!");

// // //             userRows.add(new UserRow(username, password, "ETUDIANT", null, Instant.now()));
// // //             studentRows.add(new StudentRow(matricule, fullName, email, null, sexeId, null)); // user_id to fill later
// // //             allUsernames.add(username);
// // //         }

// // //         // 1) INSERT USERS in chunks
// // //         for (int start = 0; start < userRows.size(); start += USER_INSERT_BATCH) {
// // //             int end = Math.min(userRows.size(), start + USER_INSERT_BATCH);
// // //             List<UserRow> chunk = userRows.subList(start, end);
// // //             batchInsertUsers(chunk);
// // //         }

// // //         // 2) SELECT user ids for inserted usernames
// // //         Map<String, Long> userIdByUsername = fetchIdsForUsernames(allUsernames);

// // //         // assign user_id to student rows
// // //         for (int idx = 0; idx < studentRows.size(); idx++) {
// // //             StudentRow sr = studentRows.get(idx);
// // //             String username = userRows.get(idx).username;
// // //             Long userId = userIdByUsername.get(username);
// // //             if (userId == null) {
// // //                 throw new IllegalStateException("Impossible to fetch user_id for username " + username);
// // //             }
// // //             sr.userId = userId;
// // //         }

// // //         // 3) INSERT STUDENTS in chunks
// // //         List<String> allMatricules = new ArrayList<>(studentRows.size());
// // //         for (int start = 0; start < studentRows.size(); start += STUDENT_INSERT_BATCH) {
// // //             int end = Math.min(studentRows.size(), start + STUDENT_INSERT_BATCH);
// // //             List<StudentRow> chunk = studentRows.subList(start, end);
// // //             batchInsertStudents(chunk);
// // //             allMatricules.addAll(chunk.stream().map(s -> s.matricule).collect(Collectors.toList()));
// // //         }

// // //         // 4) FETCH student ids by matricule
// // //         Map<String, Long> studentIdByMatricule = fetchIdsForMatricules(allMatricules);

// // //         // 5) PREPARE & INSERT STUDENT_YEAR_PROFILE rows
// // //         List<StudentYearProfileRow> syrList = new ArrayList<>(studentRows.size());
// // //         for (StudentRow sr : studentRows) {
// // //             Long studentId = studentIdByMatricule.get(sr.matricule);
// // //             if (studentId == null) throw new IllegalStateException("Student id not found for matricule " + sr.matricule);
// // //             syrList.add(new StudentYearProfileRow(studentId, true, ACADEMIC_YEAR_ID, levelId, specialtyId));
// // //         }

// // //         for (int start = 0; start < syrList.size(); start += YEARPROFILE_INSERT_BATCH) {
// // //             int end = Math.min(syrList.size(), start + YEARPROFILE_INSERT_BATCH);
// // //             List<StudentYearProfileRow> chunk = syrList.subList(start, end);
// // //             batchInsertStudentYearProfiles(chunk);
// // //         }

// // //         long t1 = System.currentTimeMillis();
// // //         System.out.println("End " + comboLabel + " : created=" + total + " in " + (t1 - t0) + " ms");
// // //         return new Stat(total, t1 - t0);
// // //     }

// // //     // ========== DB ops (assumptions: columns names use snake_case where appropriate) ==========

// // //     private void batchInsertUsers(List<UserRow> users) {
// // //         String sql = "INSERT INTO " + TABLE_USER + " (username, password, role, fcm_token, privacy_policy_accepted_at) VALUES (?, ?, ?, ?, ?)";
// // //         jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
// // //             @Override
// // //             public void setValues(PreparedStatement ps, int i) throws SQLException {
// // //                 UserRow u = users.get(i);
// // //                 ps.setString(1, u.username);
// // //                 ps.setString(2, u.password);
// // //                 ps.setString(3, u.role);
// // //                 if (u.fcmToken != null) ps.setString(4, u.fcmToken); else ps.setNull(4, java.sql.Types.VARCHAR);
// // //                 if (u.privacyPolicyAcceptedAt != null) ps.setTimestamp(5, java.sql.Timestamp.from(u.privacyPolicyAcceptedAt));
// // //                 else ps.setNull(5, java.sql.Types.TIMESTAMP);
// // //             }
// // //             @Override
// // //             public int getBatchSize() { return users.size(); }
// // //         });
// // //     }

// // //     private Map<String, Long> fetchIdsForUsernames(List<String> usernames) {
// // //         Map<String, Long> out = new HashMap<>();
// // //         // chunk IN clause to avoid too long queries
// // //         for (int start = 0; start < usernames.size(); start += 800) {
// // //             int end = Math.min(usernames.size(), start + 800);
// // //             List<String> chunk = usernames.subList(start, end);
// // //             String inSql = chunk.stream().map(s -> "?").collect(Collectors.joining(","));
// // //             String sql = "SELECT id, username FROM " + TABLE_USER + " WHERE username IN (" + inSql + ")";
// // //             List<Map<String,Object>> rows = jdbc.queryForList(sql, chunk.toArray());
// // //             for (Map<String,Object> r : rows) {
// // //                 out.put((String) r.get("username"), ((Number) r.get("id")).longValue());
// // //             }
// // //         }
// // //         return out;
// // //     }

// // //     private void batchInsertStudents(List<StudentRow> students) {
// // //         // Assumed column names: matricule, full_name, email, avatar_url, sexe_id, user_id
// // //         String sql = "INSERT INTO " + TABLE_STUDENT + " (matricule, full_name, email, avatar_url, sexe_id, user_id) VALUES (?, ?, ?, ?, ?, ?)";
// // //         jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
// // //             @Override
// // //             public void setValues(PreparedStatement ps, int i) throws SQLException {
// // //                 StudentRow s = students.get(i);
// // //                 ps.setString(1, s.matricule);
// // //                 ps.setString(2, s.fullName);
// // //                 ps.setString(3, s.email);
// // //                 if (s.avatarUrl != null) ps.setString(4, s.avatarUrl); else ps.setNull(4, java.sql.Types.VARCHAR);
// // //                 if (s.sexeId != null) ps.setLong(5, s.sexeId); else ps.setNull(5, java.sql.Types.BIGINT);
// // //                 if (s.userId != null) ps.setLong(6, s.userId); else ps.setNull(6, java.sql.Types.BIGINT);
// // //             }
// // //             @Override
// // //             public int getBatchSize() { return students.size(); }
// // //         });
// // //     }

// // //     private Map<String, Long> fetchIdsForMatricules(List<String> matricules) {
// // //         Map<String, Long> out = new HashMap<>();
// // //         for (int start = 0; start < matricules.size(); start += 800) {
// // //             int end = Math.min(matricules.size(), start + 800);
// // //             List<String> chunk = matricules.subList(start, end);
// // //             String inSql = chunk.stream().map(s -> "?").collect(Collectors.joining(","));
// // //             String sql = "SELECT id, matricule FROM " + TABLE_STUDENT + " WHERE matricule IN (" + inSql + ")";
// // //             List<Map<String,Object>> rows = jdbc.queryForList(sql, chunk.toArray());
// // //             for (Map<String,Object> r : rows) {
// // //                 out.put((String) r.get("matricule"), ((Number) r.get("id")).longValue());
// // //             }
// // //         }
// // //         return out;
// // //     }

// // //     private void batchInsertStudentYearProfiles(List<StudentYearProfileRow> rows) {
// // //         // assumed columns: student_id, active, academic_year_id, level_id, specialty_id
// // //         String sql = "INSERT INTO " + TABLE_STUDENT_YEAR + " (student_id, active, academic_year_id, level_id, specialty_id) VALUES (?, ?, ?, ?, ?)";
// // //         jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
// // //             @Override
// // //             public void setValues(PreparedStatement ps, int i) throws SQLException {
// // //                 StudentYearProfileRow r = rows.get(i);
// // //                 ps.setLong(1, r.studentId);
// // //                 ps.setBoolean(2, r.active);
// // //                 ps.setLong(3, r.academicYearId);
// // //                 ps.setLong(4, r.levelId);
// // //                 ps.setLong(5, r.specialtyId);
// // //             }
// // //             @Override
// // //             public int getBatchSize() { return rows.size(); }
// // //         });
// // //     }

// // //     // helpers to load small lookup lists
// // //     private List<IdName> loadIdNameList(String table) {
// // //         String sql = "SELECT id, COALESCE(name, 'n/a') AS name FROM " + table;
// // //         List<Map<String,Object>> rows = jdbc.queryForList(sql);
// // //         return rows.stream()
// // //                 .map(r -> new IdName(((Number) r.get("id")).longValue(), String.valueOf(r.get("name"))))
// // //                 .collect(Collectors.toList());
// // //     }

// // //     private boolean existsAcademicYear(long id) {
// // //         Integer cnt = jdbc.queryForObject("SELECT count(*) FROM " + TABLE_ACADEMIC_YEAR + " WHERE id = ?", Integer.class, id);
// // //         return cnt != null && cnt > 0;
// // //     }

// // //     private Long loadAnySexeId() {
// // //         List<Map<String,Object>> rows = jdbc.queryForList("SELECT id FROM " + TABLE_SEXE + " LIMIT 1");
// // //         if (rows.isEmpty()) return null;
// // //         return ((Number) rows.get(0).get("id")).longValue();
// // //     }

// // //     // ========== small DTOs ==========
// // //     static class IdName { final Long id; final String name; IdName(Long id, String name) { this.id = id; this.name = name; } }
// // //     static class UserRow { final String username; final String password; final String role; final String fcmToken; final Instant privacyPolicyAcceptedAt;
// // //         UserRow(String username, String password, String role, String fcmToken, Instant pAt) { this.username = username; this.password = password; this.role = role; this.fcmToken = fcmToken; this.privacyPolicyAcceptedAt = pAt; }
// // //     }
// // //     static class StudentRow { final String matricule; final String fullName; final String email; final String avatarUrl; final Long sexeId; Long userId;
// // //         StudentRow(String matricule, String fullName, String email, String avatarUrl, Long sexeId, Long userId) { this.matricule = matricule; this.fullName = fullName; this.email = email; this.avatarUrl = avatarUrl; this.sexeId = sexeId; this.userId = userId; }
// // //     }
// // //     static class StudentYearProfileRow { final Long studentId; final boolean active; final Long academicYearId; final Long levelId; final Long specialtyId;
// // //         StudentYearProfileRow(Long studentId, boolean active, Long academicYearId, Long levelId, Long specialtyId) { this.studentId = studentId; this.active = active; this.academicYearId = academicYearId; this.levelId = levelId; this.specialtyId = specialtyId; }
// // //     }
// // //     static class Stat { final int count; final long millis; Stat(int c, long m) { this.count = c; this.millis = m; } }
// // // }












































// // package codeqr.code.config;

// // import org.springframework.boot.context.event.ApplicationReadyEvent;
// // import org.springframework.context.ApplicationListener;
// // import org.springframework.core.Ordered;
// // import org.springframework.core.annotation.Order;
// // import org.springframework.stereotype.Component;
// // import org.springframework.beans.factory.annotation.Value;
// // import org.springframework.jdbc.core.BatchPreparedStatementSetter;
// // import org.springframework.jdbc.core.JdbcTemplate;
// // import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
// // import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// // import org.springframework.security.crypto.password.PasswordEncoder;

// // import java.sql.PreparedStatement;
// // import java.sql.SQLException;
// // import java.time.Instant;
// // import java.util.*;
// // import java.util.concurrent.*;
// // import java.util.concurrent.atomic.AtomicBoolean;
// // import java.util.stream.Collectors;

// // /**
// //  * BulkStaffSeeder.java
// //  *
// //  * Seeder ultra-rapide pour créer :
// //  *  - 300 professeurs
// //  *  - 300 surveillants
// //  *  - 300 administrateurs
// //  *
// //  * Principes :
// //  *  - Inserts en batch via JdbcTemplate (Postgres : ON CONFLICT DO NOTHING pour idempotence)
// //  *  - Un seul hash BCrypt calculé et réutilisé pour gagner du temps CPU
// //  *  - Chunking pour limiter la mémoire et le nombre de round-trips
// //  *
// //  * Usage :
// //  *  - Ce composant s'exécute automatiquement au démarrage si seeder.enabled=true (par défaut true)
// //  *  - Assurer qu'il existe au moins une ligne dans la table `sexe`
// //  *
// //  * Remarques :
// //  *  - Le SQL utilise ON CONFLICT (Postgres). Si tu utilises une autre DB, adapte les requêtes.
// //  *  - Vérifie que les valeurs de role (PROFESSEUR, SURVEILLANT, ADMIN) correspondent à ta contrainte/enum DB.
// //  *
// //  * Auteur: ChatGPT (généré pour ton projet)
// //  * Date  : 2025-10-04
// //  */
// // @Component
// // @Order(Ordered.HIGHEST_PRECEDENCE)
// // public class BulkStaffSeeder implements ApplicationListener<ApplicationReadyEvent> {

// //     private final JdbcTemplate jdbc;
// //     private final NamedParameterJdbcTemplate namedJdbc;
// //     private final PasswordEncoder passwordEncoder;

// //     // ======= CONFIG ========
// //     private static final int N_PROF = 300;
// //     private static final int N_SURV = 300;
// //     private static final int N_ADMIN = 300;
// //     private static final int TOTAL = N_PROF + N_SURV + N_ADMIN;

// //     private static final String TABLE_USER = "app_user";
// //     private static final String TABLE_TEACHER = "teacher";
// //     private static final String TABLE_SURVEILLANT = "surveillant";
// //     private static final String TABLE_ADMIN = "admin";
// //     private static final String TABLE_SEXE = "sexe";

// //     private static final int USER_BATCH = 600; // chunk size for user inserts
// //     private static final int STAFF_BATCH = 300; // chunk size for teacher/surveillant/admin inserts
// //     // ========================

// //     // guard to avoid double run (DevTools child context)
// //     private final AtomicBoolean started = new AtomicBoolean(false);

// //     // configurable via application.properties
// //     @Value("${seeder.enabled:true}")
// //     private boolean enabled;

// //     @Value("${seeder.runAsync:true}")
// //     private boolean runAsync;

// //     public BulkStaffSeeder(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
// //         this.jdbc = jdbc;
// //         this.namedJdbc = namedJdbc;
// //         this.passwordEncoder = new BCryptPasswordEncoder(8);
// //     }

// //     /**
// //      * ApplicationReadyEvent listener : démarre le seeding automatiquement (selon propriétés).
// //      */
// //     @Override
// //     public void onApplicationEvent(ApplicationReadyEvent event) {
// //         // avoid double-run in child contexts (DevTools)
// //         if (event.getApplicationContext().getParent() != null) return;

// //         if (!enabled) {
// //             System.out.println("[BulkStaffSeeder] disabled via seeder.enabled=false");
// //             return;
// //         }

// //         if (!started.compareAndSet(false, true)) return;

// //         Runnable job = () -> {
// //             try {
// //                 System.out.println("[BulkStaffSeeder] starting at " + Instant.now());
// //                 Map<String, Object> result = run(); // exécution idempotente
// //                 System.out.println("[BulkStaffSeeder] finished: " + result);
// //             } catch (Throwable t) {
// //                 System.err.println("[BulkStaffSeeder] failed:");
// //                 t.printStackTrace();
// //             }
// //         };

// //         if (runAsync) {
// //             ExecutorService ex = Executors.newSingleThreadExecutor(r -> {
// //                 Thread th = new Thread(r, "bulk-staff-seeder");
// //                 th.setDaemon(true);
// //                 return th;
// //             });
// //             ex.submit(job);
// //             ex.shutdown();
// //         } else {
// //             job.run();
// //         }
// //     }

// //     /**
// //      * Exécute le seed. Retourne un résumé (map).
// //      * Idempotent (ON CONFLICT DO NOTHING) pour permettre ré-exécutions sûres.
// //      */
// //     public Map<String, Object> run() {
// //         long t0 = System.currentTimeMillis();

// //         Long sexeId = loadAnySexeId();
// //         if (sexeId == null) {
// //             throw new IllegalStateException("Aucun sexe trouvé dans la table 'sexe'. Crée au moins une ligne.");
// //         }

// //         // 1) Préparer les 900 users
// //         List<UserRow> users = new ArrayList<>(TOTAL);
// //         List<String> usernames = new ArrayList<>(TOTAL);

// //         // compute one bcrypt hash and reuse (gain CPU)
// //         final String encodedPassword = passwordEncoder.encode("ChangeMe123!");

// //         // generate profs
// //         for (int i = 1; i <= N_PROF; i++) {
// //             String username = String.format("prof_%04d_%s", i, randomSuffix());
// //             String email = username + "@seed.local";
// //             String matricule = String.format("TP-%05d", i);
// //             users.add(new UserRow(username, encodedPassword, "PROFESSEUR", null, Instant.now(), matricule, "Prof " + i, email, sexeId));
// //             usernames.add(username);
// //         }
// //         // generate surveillants
// //         for (int i = 1; i <= N_SURV; i++) {
// //             String username = String.format("surv_%04d_%s", i, randomSuffix());
// //             String email = username + "@seed.local";
// //             String matricule = String.format("SV-%05d", i);
// //             users.add(new UserRow(username, encodedPassword, "SURVEILLANT", null, Instant.now(), matricule, "Surv " + i, email, sexeId));
// //             usernames.add(username);
// //         }
// //         // generate admins
// //         for (int i = 1; i <= N_ADMIN; i++) {
// //             String username = String.format("admin_%04d_%s", i, randomSuffix());
// //             String email = username + "@seed.local";
// //             String matricule = String.format("AD-%05d", i);
// //             users.add(new UserRow(username, encodedPassword, "ADMIN", null, Instant.now(), matricule, "Admin " + i, email, sexeId));
// //             usernames.add(username);
// //         }

// //         // 2) Insert users in chunks (Postgres ON CONFLICT DO NOTHING to be idempotent)
// //         for (int start = 0; start < users.size(); start += USER_BATCH) {
// //             int end = Math.min(users.size(), start + USER_BATCH);
// //             List<UserRow> chunk = users.subList(start, end);
// //             batchInsertUsersOnConflict(chunk);
// //         }

// //         // 3) fetch ids by username
// //         Map<String, Long> userIdByUsername = fetchIdsForUsernames(usernames);

// //         // 4) prepare and insert teacher / surveillant / admin rows using user ids
// //         // teachers
// //         List<TeachRow> teachRows = new ArrayList<>(N_PROF);
// //         for (int i = 0; i < N_PROF; i++) {
// //             UserRow ur = users.get(i);
// //             Long userId = userIdByUsername.get(ur.username);
// //             if (userId == null) continue; // if conflict prevented insert, could be preexisting - we'll skip but still safe
// //             teachRows.add(new TeachRow(ur.matricule, ur.fullName, ur.email, ur.sexeId, userId));
// //         }
// //         batchInsertTeachers(teachRows);

// //         // surveillants
// //         List<SurvRow> survRows = new ArrayList<>(N_SURV);
// //         for (int i = N_PROF; i < N_PROF + N_SURV; i++) {
// //             UserRow ur = users.get(i);
// //             Long userId = userIdByUsername.get(ur.username);
// //             if (userId == null) continue;
// //             survRows.add(new SurvRow(ur.matricule, ur.fullName, ur.email, ur.sexeId, userId));
// //         }
// //         batchInsertSurveillants(survRows);

// //         // admins
// //         List<AdminRow> adminRows = new ArrayList<>(N_ADMIN);
// //         for (int i = N_PROF + N_SURV; i < users.size(); i++) {
// //             UserRow ur = users.get(i);
// //             Long userId = userIdByUsername.get(ur.username);
// //             if (userId == null) continue;
// //             adminRows.add(new AdminRow(ur.matricule, ur.fullName, ur.email, ur.sexeId, userId));
// //         }
// //         batchInsertAdmins(adminRows);

// //         long t1 = System.currentTimeMillis();
// //         Map<String, Object> result = new HashMap<>();
// //         result.put("createdUsers", userIdByUsername.size());
// //         result.put("teachersInserted", teachRows.size());
// //         result.put("surveillantsInserted", survRows.size());
// //         result.put("adminsInserted", adminRows.size());
// //         result.put("durationMs", (t1 - t0));
// //         return result;
// //     }

// //     // -------------------- DB ops --------------------

// //     private void batchInsertUsersOnConflict(List<UserRow> chunk) {
// //         // Postgres-specific ON CONFLICT DO NOTHING (idempotent)
// //         final String sql = "INSERT INTO " + TABLE_USER +
// //                 " (username, password, role, fcm_token, privacy_policy_accepted_at) " +
// //                 "VALUES (?, ?, ?, ?, ?) " +
// //                 "ON CONFLICT (username) DO NOTHING";
// //         jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
// //             @Override public void setValues(PreparedStatement ps, int i) throws SQLException {
// //                 UserRow u = chunk.get(i);
// //                 ps.setString(1, u.username);
// //                 ps.setString(2, u.password);
// //                 ps.setString(3, u.role);
// //                 if (u.fcmToken != null) ps.setString(4, u.fcmToken); else ps.setNull(4, java.sql.Types.VARCHAR);
// //                 if (u.privacyPolicyAcceptedAt != null) ps.setTimestamp(5, java.sql.Timestamp.from(u.privacyPolicyAcceptedAt));
// //                 else ps.setNull(5, java.sql.Types.TIMESTAMP);
// //             }
// //             @Override public int getBatchSize() { return chunk.size(); }
// //         });
// //     }

// //     private Map<String, Long> fetchIdsForUsernames(List<String> usernames) {
// //         Map<String, Long> out = new HashMap<>();
// //         for (int start=0; start<usernames.size(); start += 800) {
// //             int end = Math.min(usernames.size(), start + 800);
// //             List<String> chunk = usernames.subList(start, end);
// //             String inSql = chunk.stream().map(s -> "?").collect(Collectors.joining(","));
// //             String sql = "SELECT id, username FROM " + TABLE_USER + " WHERE username IN (" + inSql + ")";
// //             List<Map<String,Object>> rows = jdbc.queryForList(sql, chunk.toArray());
// //             for (Map<String,Object> r : rows) {
// //                 out.put((String) r.get("username"), ((Number) r.get("id")).longValue());
// //             }
// //         }
// //         return out;
// //     }

// //     private void batchInsertTeachers(List<TeachRow> rows) {
// //         if (rows == null || rows.isEmpty()) return;
// //         final String sql = "INSERT INTO " + TABLE_TEACHER + " (matricule, full_name, email, sexe_id, user_id) VALUES (?, ?, ?, ?, ?) " +
// //                 "ON CONFLICT (matricule) DO NOTHING";
// //         for (int start=0; start<rows.size(); start += STAFF_BATCH) {
// //             int end = Math.min(rows.size(), start + STAFF_BATCH);
// //             List<TeachRow> chunk = rows.subList(start, end);
// //             jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
// //                 @Override public void setValues(PreparedStatement ps, int i) throws SQLException {
// //                     TeachRow r = chunk.get(i);
// //                     ps.setString(1, r.matricule);
// //                     ps.setString(2, r.fullName);
// //                     ps.setString(3, r.email);
// //                     ps.setLong(4, r.sexeId);
// //                     ps.setLong(5, r.userId);
// //                 }
// //                 @Override public int getBatchSize() { return chunk.size(); }
// //             });
// //         }
// //     }

// //     private void batchInsertSurveillants(List<SurvRow> rows) {
// //         if (rows == null || rows.isEmpty()) return;
// //         final String sql = "INSERT INTO " + TABLE_SURVEILLANT + " (matricule, full_name, email, sexe_id, user_id) VALUES (?, ?, ?, ?, ?) " +
// //                 "ON CONFLICT (matricule) DO NOTHING";
// //         for (int start=0; start<rows.size(); start += STAFF_BATCH) {
// //             int end = Math.min(rows.size(), start + STAFF_BATCH);
// //             List<SurvRow> chunk = rows.subList(start, end);
// //             jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
// //                 @Override public void setValues(PreparedStatement ps, int i) throws SQLException {
// //                     SurvRow r = chunk.get(i);
// //                     ps.setString(1, r.matricule);
// //                     ps.setString(2, r.fullName);
// //                     ps.setString(3, r.email);
// //                     ps.setLong(4, r.sexeId);
// //                     ps.setLong(5, r.userId);
// //                 }
// //                 @Override public int getBatchSize() { return chunk.size(); }
// //             });
// //         }
// //     }

// //     private void batchInsertAdmins(List<AdminRow> rows) {
// //         if (rows == null || rows.isEmpty()) return;
// //         final String sql = "INSERT INTO " + TABLE_ADMIN + " (matricule, full_name, email, sexe_id, user_id) VALUES (?, ?, ?, ?, ?) " +
// //                 "ON CONFLICT (matricule) DO NOTHING";
// //         for (int start=0; start<rows.size(); start += STAFF_BATCH) {
// //             int end = Math.min(rows.size(), start + STAFF_BATCH);
// //             List<AdminRow> chunk = rows.subList(start, end);
// //             jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
// //                 @Override public void setValues(PreparedStatement ps, int i) throws SQLException {
// //                     AdminRow r = chunk.get(i);
// //                     ps.setString(1, r.matricule);
// //                     ps.setString(2, r.fullName);
// //                     ps.setString(3, r.email);
// //                     ps.setLong(4, r.sexeId);
// //                     ps.setLong(5, r.userId);
// //                 }
// //                 @Override public int getBatchSize() { return chunk.size(); }
// //             });
// //         }
// //     }

// //     // récupère n'importe quel sexe.id existant
// //     private Long loadAnySexeId() {
// //         List<Map<String,Object>> rows = jdbc.queryForList("SELECT id FROM " + TABLE_SEXE + " LIMIT 1");
// //         if (rows.isEmpty()) return null;
// //         return ((Number) rows.get(0).get("id")).longValue();
// //     }

// //     private static String randomSuffix() {
// //         return UUID.randomUUID().toString().substring(0, 6);
// //     }

// //     // ---------------- DTOs ----------------
// //     private static class UserRow {
// //         final String username;
// //         final String password;
// //         final String role;
// //         final String fcmToken;
// //         final Instant privacyPolicyAcceptedAt;
// //         // staff fields:
// //         final String matricule;
// //         final String fullName;
// //         final String email;
// //         final Long sexeId;
// //         UserRow(String username, String password, String role, String fcmToken, Instant pAt,
// //                 String matricule, String fullName, String email, Long sexeId) {
// //             this.username = username; this.password = password; this.role = role; this.fcmToken = fcmToken;
// //             this.privacyPolicyAcceptedAt = pAt; this.matricule = matricule; this.fullName = fullName;
// //             this.email = email; this.sexeId = sexeId;
// //         }
// //     }
// //     private static class TeachRow { final String matricule, fullName, email; final Long sexeId, userId; TeachRow(String m,String f,String e,Long s,Long u){matricule=m;fullName=f;email=e;sexeId=s;userId=u;} }
// //     private static class SurvRow { final String matricule, fullName, email; final Long sexeId, userId; SurvRow(String m,String f,String e,Long s,Long u){matricule=m;fullName=f;email=e;sexeId=s;userId=u;} }
// //     private static class AdminRow { final String matricule, fullName, email; final Long sexeId, userId; AdminRow(String m,String f,String e,Long s,Long u){matricule=m;fullName=f;email=e;sexeId=s;userId=u;} }
// // }
















































// // package codeqr.code.config;

// // import org.springframework.boot.context.event.ApplicationReadyEvent;
// // import org.springframework.context.ApplicationListener;
// // import org.springframework.core.Ordered;
// // import org.springframework.core.annotation.Order;
// // import org.springframework.jdbc.core.BatchPreparedStatementSetter;
// // import org.springframework.jdbc.core.JdbcTemplate;
// // import org.springframework.stereotype.Component;
// // import org.springframework.beans.factory.annotation.Value;
// // import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// // import org.springframework.security.crypto.password.PasswordEncoder;

// // import java.sql.PreparedStatement;
// // import java.sql.SQLException;
// // import java.time.Instant;
// // import java.util.*;
// // import java.util.concurrent.*;
// // import java.util.concurrent.atomic.AtomicBoolean;

// // /**
// //  * BulkTeacherSeeder
// //  *
// //  * - Crée N professeurs (users + teacher)
// //  * - Pour chaque teacher, associe aléatoirement 1..4 cours (inserts dans teacher_courses)
// //  * - Idempotent (ON CONFLICT DO NOTHING) — ré-exécutable
// //  * - Optimisé : batch inserts, un seul hash BCrypt, chunking
// //  *
// //  * Usage: composant @Component -> s'exécute automatiquement si seeder.enabled=true
// //  *
// //  * IMPORTANT: SQL utilisé = PostgreSQL (ON CONFLICT). Si autre SGBD, adapte.
// //  */
// // @Component
// // @Order(Ordered.HIGHEST_PRECEDENCE)
// // public class BulkTeacherSeeder implements ApplicationListener<ApplicationReadyEvent> {

// //     private final JdbcTemplate jdbc;
// //     private final PasswordEncoder passwordEncoder;

// //     // ====== CONFIG ======
// //     private static final int N_PROF = 300;
// //     private static final String TABLE_USER = "app_user";
// //     private static final String TABLE_TEACHER = "teacher";
// //     private static final String TABLE_TEACHER_COURSES = "teacher_courses";
// //     private static final String TABLE_SEXE = "sexe";
// //     private static final String TABLE_COURSE = "course";

// //     private static final int USER_BATCH = 300;
// //     private static final int TEACHER_BATCH = 300;
// //     private static final int TC_BATCH = 500;
// //     // ====================

// //     private final AtomicBoolean started = new AtomicBoolean(false);

// //     @Value("${seeder.enabled:true}")
// //     private boolean enabled;

// //     @Value("${seeder.runAsync:true}")
// //     private boolean runAsync;

// //     public BulkTeacherSeeder(JdbcTemplate jdbc) {
// //         this.jdbc = jdbc;
// //         this.passwordEncoder = new BCryptPasswordEncoder(8);
// //     }

// //     @Override
// //     public void onApplicationEvent(ApplicationReadyEvent event) {
// //         // avoid double-run in devtools restart child contexts
// //         if (event.getApplicationContext().getParent() != null) return;
// //         if (!enabled) {
// //             System.out.println("[BulkTeacherSeeder] disabled via seeder.enabled=false");
// //             return;
// //         }
// //         if (!started.compareAndSet(false, true)) return;

// //         Runnable job = () -> {
// //             try {
// //                 System.out.println("[BulkTeacherSeeder] starting");
// //                 Map<String, Object> r = run();
// //                 System.out.println("[BulkTeacherSeeder] finished: " + r);
// //             } catch (Throwable t) {
// //                 System.err.println("[BulkTeacherSeeder] failed:");
// //                 t.printStackTrace();
// //             }
// //         };

// //         if (runAsync) {
// //             ExecutorService ex = Executors.newSingleThreadExecutor(r -> {
// //                 Thread th = new Thread(r, "bulk-teacher-seeder");
// //                 th.setDaemon(true);
// //                 return th;
// //             });
// //             ex.submit(job);
// //             ex.shutdown();
// //         } else {
// //             job.run();
// //         }
// //     }

// //     /**
// //      * Run seeding process.
// //      * @return summary map
// //      */
// //     public Map<String, Object> run() {
// //         long t0 = System.currentTimeMillis();

// //         // load available courses
// //         List<Long> courseIds = jdbc.queryForList("SELECT id FROM " + TABLE_COURSE, Long.class);
// //         if (courseIds.isEmpty()) {
// //             throw new IllegalStateException("Aucune course trouvée (table 'course'). Impossile d'assigner des cours.");
// //         }

// //         // pick a sexe id for teacher rows
// //         Long sexeId = loadAnySexeId();
// //         if (sexeId == null) throw new IllegalStateException("Aucun sexe trouvé (table 'sexe').");

// //         // prepare user rows
// //         List<UserRow> users = new ArrayList<>(N_PROF);
// //         List<String> usernames = new ArrayList<>(N_PROF);

// //         final String encodedPassword = passwordEncoder.encode("ChangeMe123!");

// //         for (int i = 1; i <= N_PROF; i++) {
// //             String username = String.format("prof_%04d_%s", i, randomSuffix());
// //             String email = username + "@seed.local";
// //             String matricule = String.format("TP-%05d", i);
// //             String fullName = "Prof " + i;
// //             users.add(new UserRow(username, encodedPassword, "PROFESSEUR", null, Instant.now(), matricule, fullName, email, sexeId));
// //             usernames.add(username);
// //         }

// //         // insert users in chunks (idempotent)
// //         for (int start = 0; start < users.size(); start += USER_BATCH) {
// //             int end = Math.min(users.size(), start + USER_BATCH);
// //             batchInsertUsersOnConflict(users.subList(start, end));
// //         }

// //         // fetch user ids
// //         Map<String, Long> userIdByUsername = fetchIdsForUsernames(usernames);

// //         // insert teacher rows, collect teacher ids
// //         List<TeachRow> teachRows = new ArrayList<>(N_PROF);
// //         List<Long> teacherUserIds = new ArrayList<>(N_PROF); // user ids used for teacher lookup
// //         for (UserRow ur : users) {
// //             Long uid = userIdByUsername.get(ur.username);
// //             if (uid == null) continue;
// //             teachRows.add(new TeachRow(ur.matricule, ur.fullName, ur.email, ur.sexeId, uid));
// //             teacherUserIds.add(uid);
// //         }

// //         // insert teacher rows in batch
// //         batchInsertTeachers(teachRows);

// //         // fetch teacher id by user_id
// //         Map<Long, Long> teacherIdByUserId = fetchTeacherIdByUserIds(teacherUserIds);

// //         // prepare teacher_courses rows: for each teacher, choose 1..4 random course ids
// //         List<TeacherCourseRow> tcRows = new ArrayList<>();
// //         Random rnd = new Random();
// //         for (UserRow ur : users) {
// //             Long userId = userIdByUsername.get(ur.username);
// //             if (userId == null) continue;
// //             Long teacherId = teacherIdByUserId.get(userId);
// //             if (teacherId == null) continue;
// //             // choose random number k between 1 and 4 (bounded by available courses)
// //             int maxK = Math.min(4, courseIds.size());
// //             int k = 1 + rnd.nextInt(maxK); // 1..maxK
// //             // pick k distinct course ids
// //             Set<Long> picked = new LinkedHashSet<>();
// //             while (picked.size() < k) {
// //                 Long c = courseIds.get(rnd.nextInt(courseIds.size()));
// //                 picked.add(c);
// //             }
// //             for (Long cid : picked) {
// //                 tcRows.add(new TeacherCourseRow(teacherId, cid));
// //             }
// //         }

// //         // batch insert teacher_courses (ON CONFLICT DO NOTHING)
// //         for (int start = 0; start < tcRows.size(); start += TC_BATCH) {
// //             int end = Math.min(tcRows.size(), start + TC_BATCH);
// //             batchInsertTeacherCourses(tcRows.subList(start, end));
// //         }

// //         long t1 = System.currentTimeMillis();
// //         Map<String, Object> res = new HashMap<>();
// //         res.put("usersPrepared", users.size());
// //         res.put("usersInsertedOrExists", userIdByUsername.size());
// //         res.put("teachersInsertedOrExists", teacherIdByUserId.size());
// //         res.put("teacherCoursesInserted", tcRows.size());
// //         res.put("durationMs", (t1 - t0));
// //         return res;
// //     }

// //     // -------- DB ops --------

// //     private void batchInsertUsersOnConflict(List<UserRow> chunk) {
// //         final String sql = "INSERT INTO " + TABLE_USER + " (username, password, role, fcm_token, privacy_policy_accepted_at) " +
// //                 "VALUES (?, ?, ?, ?, ?) ON CONFLICT (username) DO NOTHING";
// //         jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
// //             @Override public void setValues(PreparedStatement ps, int i) throws SQLException {
// //                 UserRow u = chunk.get(i);
// //                 ps.setString(1, u.username);
// //                 ps.setString(2, u.password);
// //                 ps.setString(3, u.role);
// //                 if (u.fcmToken != null) ps.setString(4, u.fcmToken); else ps.setNull(4, java.sql.Types.VARCHAR);
// //                 if (u.privacyPolicyAcceptedAt != null) ps.setTimestamp(5, java.sql.Timestamp.from(u.privacyPolicyAcceptedAt));
// //                 else ps.setNull(5, java.sql.Types.TIMESTAMP);
// //             }
// //             @Override public int getBatchSize() { return chunk.size(); }
// //         });
// //     }

// //     private Map<String, Long> fetchIdsForUsernames(List<String> usernames) {
// //         Map<String, Long> out = new HashMap<>();
// //         for (int start=0; start<usernames.size(); start += 800) {
// //             int end = Math.min(usernames.size(), start + 800);
// //             List<String> chunk = usernames.subList(start, end);
// //             String inSql = String.join(",", Collections.nCopies(chunk.size(), "?"));
// //             String sql = "SELECT id, username FROM " + TABLE_USER + " WHERE username IN (" + inSql + ")";
// //             List<Map<String,Object>> rows = jdbc.queryForList(sql, chunk.toArray());
// //             for (Map<String,Object> r : rows) {
// //                 out.put((String) r.get("username"), ((Number) r.get("id")).longValue());
// //             }
// //         }
// //         return out;
// //     }

// //     private void batchInsertTeachers(List<TeachRow> rows) {
// //         if (rows == null || rows.isEmpty()) return;
// //         final String sql = "INSERT INTO " + TABLE_TEACHER + " (matricule, full_name, email, sexe_id, user_id) " +
// //                 "VALUES (?, ?, ?, ?, ?) ON CONFLICT (matricule) DO NOTHING";
// //         for (int start=0; start<rows.size(); start += TEACHER_BATCH) {
// //             int end = Math.min(rows.size(), start + TEACHER_BATCH);
// //             List<TeachRow> chunk = rows.subList(start, end);
// //             jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
// //                 @Override public void setValues(PreparedStatement ps, int i) throws SQLException {
// //                     TeachRow r = chunk.get(i);
// //                     ps.setString(1, r.matricule);
// //                     ps.setString(2, r.fullName);
// //                     ps.setString(3, r.email);
// //                     ps.setLong(4, r.sexeId);
// //                     ps.setLong(5, r.userId);
// //                 }
// //                 @Override public int getBatchSize() { return chunk.size(); }
// //             });
// //         }
// //     }

// //     private Map<Long, Long> fetchTeacherIdByUserIds(List<Long> userIds) {
// //         Map<Long, Long> out = new HashMap<>();
// //         if (userIds == null || userIds.isEmpty()) return out;
// //         for (int start=0; start<userIds.size(); start += 800) {
// //             int end = Math.min(userIds.size(), start + 800);
// //             List<Long> chunk = userIds.subList(start, end);
// //             String inSql = String.join(",", Collections.nCopies(chunk.size(), "?"));
// //             String sql = "SELECT id, user_id FROM " + TABLE_TEACHER + " WHERE user_id IN (" + inSql + ")";
// //             List<Map<String,Object>> rows = jdbc.queryForList(sql, chunk.toArray());
// //             for (Map<String,Object> r : rows) {
// //                 out.put(((Number) r.get("user_id")).longValue(), ((Number) r.get("id")).longValue());
// //             }
// //         }
// //         return out;
// //     }

// //     private void batchInsertTeacherCourses(List<TeacherCourseRow> rows) {
// //         if (rows == null || rows.isEmpty()) return;
// //         final String sql = "INSERT INTO " + TABLE_TEACHER_COURSES + " (teacher_id, course_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
// //         for (int start=0; start<rows.size(); start += TC_BATCH) {
// //             int end = Math.min(rows.size(), start + TC_BATCH);
// //             List<TeacherCourseRow> chunk = rows.subList(start, end);
// //             jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
// //                 @Override public void setValues(PreparedStatement ps, int i) throws SQLException {
// //                     TeacherCourseRow r = chunk.get(i);
// //                     ps.setLong(1, r.teacherId);
// //                     ps.setLong(2, r.courseId);
// //                 }
// //                 @Override public int getBatchSize() { return chunk.size(); }
// //             });
// //         }
// //     }

// //     private Long loadAnySexeId() {
// //         List<Map<String,Object>> rows = jdbc.queryForList("SELECT id FROM " + TABLE_SEXE + " LIMIT 1");
// //         if (rows.isEmpty()) return null;
// //         return ((Number) rows.get(0).get("id")).longValue();
// //     }

// //     private static String randomSuffix() {
// //         return UUID.randomUUID().toString().substring(0, 6);
// //     }

// //     // DTOs
// //     private static class UserRow {
// //         final String username, password, role, fcmToken;
// //         final Instant privacyPolicyAcceptedAt;
// //         final String matricule, fullName, email;
// //         final Long sexeId;
// //         UserRow(String username, String password, String role, String fcmToken, Instant pAt,
// //                 String matricule, String fullName, String email, Long sexeId) {
// //             this.username = username; this.password = password; this.role = role; this.fcmToken = fcmToken;
// //             this.privacyPolicyAcceptedAt = pAt; this.matricule = matricule; this.fullName = fullName;
// //             this.email = email; this.sexeId = sexeId;
// //         }
// //     }
// //     private static class TeachRow { final String matricule, fullName, email; final Long sexeId, userId; TeachRow(String m,String f,String e,Long s,Long u){matricule=m;fullName=f;email=e;sexeId=s;userId=u;} }
// //     private static class TeacherCourseRow { final Long teacherId, courseId; TeacherCourseRow(Long t, Long c){teacherId=t;courseId=c;} }
// // }















































// package codeqr.code.config;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import codeqr.code.model.*;
// import codeqr.code.repository.*;
// import codeqr.code.security.qr.QrJwtService;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.core.annotation.Order;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.stereotype.Component;
// import org.springframework.boot.ApplicationArguments;
// import org.springframework.boot.ApplicationRunner;
// import org.springframework.transaction.annotation.Transactional;

// import javax.sql.DataSource;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.Statement;
// import java.sql.Timestamp;
// import java.time.Instant;
// import java.time.LocalDateTime;
// import java.util.*;
// import java.util.stream.Collectors;

// @Component
// @Order(1)
// public class DataSeeder implements ApplicationRunner {

//     private final TeacherRepository teacherRepository;
//     private final SurveillantRepository surveillantRepository;
//     private final CourseRepository courseRepository;
//     private final CampusRepository campusRepository;
//     private final RoomRepository roomRepository;
//     private final LevelRepository levelRepository;
//     private final SpecialtyRepository specialtyRepository;
//     private final AcademicYearRepository academicYearRepository;
//     private final StudentYearProfileRepository studentYearProfileRepository;
//     private final SessionRepository sessionRepository;
//     private final JdbcTemplate jdbcTemplate;
//     private final DataSource dataSource;
//     private final QrJwtService qrJwtService;
//     private final ObjectMapper objectMapper;

//     @Value("${app.seeder.enabled:true}")
//     private boolean enabled;

//     @Value("${app.seeder.chunk-size:1000}")
//     private int chunkSize;

//     private static final long FIXED_ACADEMIC_YEAR_ID = 3L;
//     private static final String SEED_TAG = "data_seeder_v1";
//     private static final long QR_EXPIRATION_MS = 1000L * 60 * 60 * 24;

//     public DataSeeder(TeacherRepository teacherRepository,
//                       SurveillantRepository surveillantRepository,
//                       CourseRepository courseRepository,
//                       CampusRepository campusRepository,
//                       RoomRepository roomRepository,
//                       LevelRepository levelRepository,
//                       SpecialtyRepository specialtyRepository,
//                       AcademicYearRepository academicYearRepository,
//                       StudentYearProfileRepository studentYearProfileRepository,
//                       SessionRepository sessionRepository,
//                       JdbcTemplate jdbcTemplate,
//                       DataSource dataSource,
//                       QrJwtService qrJwtService,
//                       ObjectMapper objectMapper) {
//         this.teacherRepository = teacherRepository;
//         this.surveillantRepository = surveillantRepository;
//         this.courseRepository = courseRepository;
//         this.campusRepository = campusRepository;
//         this.roomRepository = roomRepository;
//         this.levelRepository = levelRepository;
//         this.specialtyRepository = specialtyRepository;
//         this.academicYearRepository = academicYearRepository;
//         this.studentYearProfileRepository = studentYearProfileRepository;
//         this.sessionRepository = sessionRepository;
//         this.jdbcTemplate = jdbcTemplate;
//         this.dataSource = dataSource;
//         this.qrJwtService = qrJwtService;
//         this.objectMapper = objectMapper;
//     }

//     @Override
//     @Transactional
//     public void run(ApplicationArguments args) throws Exception {
//         if (!enabled) {
//             System.out.println("DataSeeder: seeder disabled via property, exiting.");
//             return;
//         }

//         Integer already = jdbcTemplate.queryForObject(
//                 "SELECT count(*) FROM session WHERE qr_payload LIKE ?", Integer.class, "%" + SEED_TAG + "%");
//         if (already != null && already > 0) {
//             System.out.println("DataSeeder: sessions with seed tag already exist (skipping seeding)");
//             return;
//         }

//         System.out.println("DataSeeder: starting seeding process...");

//         // Charge les listes complètes
//         List<Teacher> teachers = teacherRepository.findAll();
//         List<Surveillant> surveillants = surveillantRepository.findAll();
//         List<Level> levels = levelRepository.findAll();
//         List<Specialty> specialties = specialtyRepository.findAll();
//         List<Campus> campuses = campusRepository.findAll();
//         List<Room> rooms = roomRepository.findAll();

//         // On veut maintenant utiliser 1/30ème des profs et 1/30ème des surveillants
//         Random rnd = new Random(12345); // seed pour reproductibilité

//         int teacherSampleSize = Math.max(1, teachers.size() / 30);
//         Collections.shuffle(teachers, rnd);
//         teachers = new ArrayList<>(teachers.subList(0, teacherSampleSize)); // copie pour sécurité

//         int surveillantSampleSize = Math.max(1, surveillants.size() / 30);
//         Collections.shuffle(surveillants, rnd);
//         surveillants = new ArrayList<>(surveillants.subList(0, surveillantSampleSize));

//         System.out.println("DataSeeder: sampled teachers size = " + teachers.size() + " (1/30)");
//         System.out.println("DataSeeder: sampled surveillants size = " + surveillants.size() + " (1/30)");

//         Map<Long, List<Room>> roomsByCampus = rooms.stream()
//                 .filter(r -> r.getCampus() != null && r.getCampus().getId() != null)
//                 .collect(Collectors.groupingBy(r -> r.getCampus().getId()));

//         academicYearRepository.findById(FIXED_ACADEMIC_YEAR_ID)
//                 .orElseThrow(() -> new IllegalStateException("AcademicYear id=3 not found"));

//         List<SessionRow> sessionRows = new ArrayList<>();

//         System.out.println("DataSeeder: generating session rows for teachers...");
//         // === Methode prof (sur l'échantillon seulement) ===
//         for (Teacher t : teachers) {
//             List<Course> teacherCourses = t.getCourses();
//             if (teacherCourses == null || teacherCourses.isEmpty()) continue;
//             if (t.getUser() == null || t.getUser().getId() == null) continue;
//             for (Course c : teacherCourses) {
//                 for (int lot = 0; lot < 5; lot++) {
//                     Level lvl = pickRandom(levels, rnd);
//                     Specialty sp = pickRandom(specialties, rnd);
//                     for (int s = 0; s < 5; s++) {
//                         Campus campus = pickRandom(campuses, rnd);
//                         List<Room> rs = roomsByCampus.get(campus.getId());
//                         Room room = pickRandom(rs, rnd);
//                         LocalDateTime start = randomDateWithinLastMonths(6, rnd);
//                         LocalDateTime end = start.plusHours(4);
//                         SessionRow row = new SessionRow();
//                         row.courseId = c.getId();
//                         row.academicYearId = FIXED_ACADEMIC_YEAR_ID;
//                         row.campusId = campus.getId();
//                         row.userId = t.getUser().getId();
//                         row.roomId = room.getId();
//                         row.startTime = start;
//                         row.endTime = end;
//                         row.created = LocalDateTime.now();
//                         row.closed = false;
//                         row.expiryTime = row.created.plusHours(2);
//                         row.notified = false;
//                         row.expectedLevelId = lvl.getId();
//                         row.expectedSpecialtyId = sp.getId();
//                         row.teacherYearProfileId = null;
//                         row.surveillantId = null;
//                         row.professorTeacherId = t.getId();
//                         row.professorUserId = t.getUser().getId();
//                         sessionRows.add(row);
//                     }
//                 }
//             }
//         }

//         System.out.println("DataSeeder: generating session rows for surveillants (échantillon)...");
//         // === Methode surveillant (sur l'échantillon seulement) ===
//         List<Teacher> teachersPool = new ArrayList<>(teachers); // on se limite au pool échantillonné
//         for (Surveillant sv : surveillants) {
//             Collections.shuffle(teachersPool, rnd);
//             // conserve logique précédente (limit 15) mais si pool plus petit on s'adapte
//             List<Teacher> picked = teachersPool.stream().limit(Math.min(15, teachersPool.size())).collect(Collectors.toList());
//             for (Teacher t : picked) {
//                 List<Course> teacherCourses = t.getCourses();
//                 if (teacherCourses == null || teacherCourses.isEmpty()) continue;
//                 if (t.getUser() == null || t.getUser().getId() == null) continue;
//                 for (Course c : teacherCourses) {
//                     for (int lot = 0; lot < 5; lot++) {
//                         Level lvl = pickRandom(levels, rnd);
//                         Specialty sp = pickRandom(specialties, rnd);
//                         for (int s = 0; s < 4; s++) {
//                             Campus campus = pickRandom(campuses, rnd);
//                             List<Room> rs = roomsByCampus.get(campus.getId());
//                             Room room = pickRandom(rs, rnd);
//                             LocalDateTime start = randomDateWithinLastMonths(6, rnd);
//                             LocalDateTime end = start.plusHours(4);
//                             SessionRow row = new SessionRow();
//                             row.courseId = c.getId();
//                             row.academicYearId = FIXED_ACADEMIC_YEAR_ID;
//                             row.campusId = campus.getId();
//                             row.userId = t.getUser().getId();
//                             row.roomId = room.getId();
//                             row.startTime = start;
//                             row.endTime = end;
//                             row.created = LocalDateTime.now();
//                             row.closed = false;
//                             row.expiryTime = row.created.plusHours(2);
//                             row.notified = false;
//                             row.expectedLevelId = lvl.getId();
//                             row.expectedSpecialtyId = sp.getId();
//                             row.teacherYearProfileId = null;
//                             row.surveillantId = sv.getId();
//                             row.professorTeacherId = t.getId();
//                             row.professorUserId = t.getUser().getId();
//                             sessionRows.add(row);
//                         }
//                     }
//                 }
//             }
//         }

//         System.out.println("DataSeeder: prepared " + sessionRows.size() + " session rows to insert");

//         // Insertion par chunks
//         List<SessionMeta> allSessionMetas = new ArrayList<>();
//         int total = sessionRows.size();
//         for (int i = 0; i < total; i += chunkSize) {
//             int end = Math.min(total, i + chunkSize);
//             List<SessionRow> chunk = sessionRows.subList(i, end);
//             System.out.println("DataSeeder: inserting chunk " + (i / chunkSize + 1) + " ...");
//             List<SessionMeta> metas = insertSessionChunkAndReturnMeta(chunk);
//             allSessionMetas.addAll(metas);
//             System.out.println("DataSeeder: inserted chunk " + (i / chunkSize + 1) + " size=" + chunk.size());
//         }

//         // QR tokens
//         System.out.println("DataSeeder: starting batchUpdateTokensAndPayloads...");
//         batchUpdateTokensAndPayloads(allSessionMetas);
//         System.out.println("DataSeeder: finished batchUpdateTokensAndPayloads");

//         // Attendances
//         System.out.println("DataSeeder: starting creation of attendances...");
//         Map<LotKey, List<Long>> sessionsByLot = new HashMap<>();
//         for (SessionMeta m : allSessionMetas) {
//             LotKey key = new LotKey(m.expectedLevelId, m.expectedSpecialtyId, m.academicYearId);
//             sessionsByLot.computeIfAbsent(key, k -> new ArrayList<>()).add(m.sessionId);
//         }

//         Instant now = Instant.now();
//         for (Map.Entry<LotKey, List<Long>> entry : sessionsByLot.entrySet()) {
//             LotKey key = entry.getKey();
//             List<Long> sessionIds = entry.getValue();
//             List<Long> profileIds = jdbcTemplate.queryForList(
//                     "SELECT id FROM student_year_profile WHERE level_id = ? AND specialty_id = ? AND academic_year_id = ?",
//                     Long.class,
//                     key.levelId, key.specialtyId, key.academicYearId
//             );
//             if (profileIds.isEmpty()) continue;
//             for (Long sid : sessionIds) {
//                 jdbcTemplate.batchUpdate(
//                         "INSERT INTO attendance (student_year_profile_id, session_id, status, source, timestamp) VALUES (?, ?, 'ABSENT', 'qr', ?)",
//                         profileIds,
//                         profileIds.size(),
//                         (ps, profileId) -> {
//                             ps.setLong(1, profileId);
//                             ps.setLong(2, sid);
//                             ps.setTimestamp(3, Timestamp.from(now));
//                         }
//                 );
//             }
//         }
//         System.out.println("DataSeeder: created attendances for all sessions (by lot)");
//         System.out.println("DataSeeder: seeding finished. total sessions=" + allSessionMetas.size());
//     }

//     private List<SessionMeta> insertSessionChunkAndReturnMeta(List<SessionRow> chunk) throws Exception {
//         String sql = "INSERT INTO session (course_id, academic_year_id, campus_id, user_id, room_id, start_time, end_time, created, closed, expiry_time, notified, expected_level_id, expected_specialty_id, teacher_year_profile_id, surveillant_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//         List<SessionMeta> metas = new ArrayList<>();
//         try (Connection conn = dataSource.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//             conn.setAutoCommit(false);
//             for (SessionRow r : chunk) {
//                 ps.setLong(1, r.courseId);
//                 ps.setLong(2, r.academicYearId);
//                 ps.setLong(3, r.campusId);
//                 ps.setLong(4, r.userId);
//                 ps.setLong(5, r.roomId);
//                 ps.setTimestamp(6, Timestamp.valueOf(r.startTime));
//                 ps.setTimestamp(7, Timestamp.valueOf(r.endTime));
//                 ps.setTimestamp(8, Timestamp.valueOf(r.created));
//                 ps.setBoolean(9, r.closed);
//                 ps.setTimestamp(10, Timestamp.valueOf(r.expiryTime));
//                 ps.setBoolean(11, r.notified);
//                 ps.setLong(12, r.expectedLevelId);
//                 ps.setLong(13, r.expectedSpecialtyId);
//                 if (r.teacherYearProfileId != null) {
//                     ps.setLong(14, r.teacherYearProfileId);
//                 } else {
//                     ps.setNull(14, java.sql.Types.BIGINT);
//                 }
//                 if (r.surveillantId != null) {
//                     ps.setLong(15, r.surveillantId);
//                 } else {
//                     ps.setNull(15, java.sql.Types.BIGINT);
//                 }
//                 ps.addBatch();
//             }
//             ps.executeBatch();
//             try (ResultSet keys = ps.getGeneratedKeys()) {
//                 int idx = 0;
//                 while (keys.next()) {
//                     long generatedId = keys.getLong(1);
//                     SessionRow r = chunk.get(idx);
//                     SessionMeta meta = new SessionMeta();
//                     meta.sessionId = generatedId;
//                     meta.expectedLevelId = r.expectedLevelId;
//                     meta.expectedSpecialtyId = r.expectedSpecialtyId;
//                     meta.academicYearId = r.academicYearId;
//                     meta.professorTeacherId = r.professorTeacherId;
//                     meta.professorUserId = r.professorUserId;
//                     meta.surveillantId = r.surveillantId;
//                     metas.add(meta);
//                     idx++;
//                 }
//             }
//             conn.commit();
//         }
//         return metas;
//     }

//     private void batchUpdateTokensAndPayloads(List<SessionMeta> metas) {
//         String sql = "UPDATE session SET qr_token = ?, qr_payload = ? WHERE id = ?";
//         try (Connection conn = dataSource.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {
//             conn.setAutoCommit(false);
//             int count = 0;
//             for (SessionMeta m : metas) {
//                 count++;
//                 if (count % 100 == 0) System.out.println("DataSeeder: batchUpdateTokensAndPayloads processed " + count + "/" + metas.size());
//                 String token = qrJwtService.generateQrToken(m.sessionId, QR_EXPIRATION_MS);
//                 Map<String, Object> payload = new HashMap<>();
//                 payload.put("sessionId", m.sessionId);
//                 payload.put("token", token);
//                 payload.put("issuedAt", LocalDateTime.now().toString());
//                 payload.put("expected_specialty_id", m.expectedSpecialtyId);
//                 payload.put("expected_level_id", m.expectedLevelId);
//                 payload.put("professor_teacher_id", m.professorTeacherId);
//                 payload.put("professor_user_id", m.professorUserId);
//                 payload.put("surveillant_id", m.surveillantId);
//                 payload.put("seed_tag", SEED_TAG);
//                 String payloadJson = objectMapper.writeValueAsString(payload);
//                 ps.setString(1, token);
//                 ps.setString(2, payloadJson);
//                 ps.setLong(3, m.sessionId);
//                 ps.addBatch();
//             }
//             ps.executeBatch();
//             conn.commit();
//         } catch (Exception ex) {
//             ex.printStackTrace();
//             throw new RuntimeException("Failed to batch update tokens/payloads", ex);
//         }
//     }

//     private static <T> T pickRandom(List<T> list, Random rnd) {
//         if (list == null || list.isEmpty()) return null;
//         return list.get(rnd.nextInt(list.size()));
//     }

//     private static LocalDateTime randomDateWithinLastMonths(int months, Random rnd) {
//         LocalDateTime now = LocalDateTime.now();
//         LocalDateTime start = now.minusMonths(months);
//         long startSec = start.toEpochSecond(java.time.ZoneOffset.UTC);
//         long endSec = now.toEpochSecond(java.time.ZoneOffset.UTC);
//         long randomSec = startSec + (long) (rnd.nextDouble() * (endSec - startSec));
//         return LocalDateTime.ofEpochSecond(randomSec, 0, java.time.ZoneOffset.UTC);
//     }

//     @Data
//     private static class SessionRow {
//         Long courseId;
//         Long academicYearId;
//         Long campusId;
//         Long userId;
//         Long roomId;
//         LocalDateTime startTime;
//         LocalDateTime endTime;
//         LocalDateTime created;
//         Boolean closed;
//         LocalDateTime expiryTime;
//         Boolean notified;
//         Long expectedLevelId;
//         Long expectedSpecialtyId;
//         Long teacherYearProfileId;
//         Long surveillantId;
//         Long professorTeacherId;
//         Long professorUserId;
//     }

//     @Data
//     private static class SessionMeta {
//         Long sessionId;
//         Long expectedLevelId;
//         Long expectedSpecialtyId;
//         Long academicYearId;
//         Long professorTeacherId;
//         Long professorUserId;
//         Long surveillantId;
//     }

//     @Data
//     @AllArgsConstructor
//     @NoArgsConstructor
//     private static class LotKey {
//         Long levelId;
//         Long specialtyId;
//         Long academicYearId;
//     }
// }
 