
package codeqr.code.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
  import java.sql.Timestamp;

import java.util.stream.Collectors;
// imports essentiels à ajouter en haut du fichier
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import codeqr.code.dto.*;
import codeqr.code.exception.NotFoundException;
import codeqr.code.model.*;
import codeqr.code.repository.*;
import codeqr.code.security.qr.QrJwtService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionService {

    private final TeacherRepository teacherRepository;
    private final MySessionRepository sessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;
    private final AcademicYearRepository academicYearRepository;
    private final CampusRepository campusRepository;
    private final RoomRepository roomRepository;
    private final LevelRepository levelRepository;
    private final StudentYearProfileRepository studentYearProfileRepository;
    private final SpecialtyRepository specialtyRepository;
    private final UserRepository userRepository;
    private final SurveillantRepository surveillantRepository;
    private final QrJwtService qrJwtService;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    // EntityManager injection (not final because injected by @PersistenceContext)
    @PersistenceContext
    private EntityManager entityManager;

    // ObjectMapper local (could be injecté si tu veux)
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final long QR_EXPIRATION_MS = 2 * 60 * 60 * 1000; // 2h
    private static final int ATTENDANCE_BATCH_SIZE = 500;

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    // ------------------------- CRUD basique -------------------------

    @Transactional(readOnly = true)
    public List<Session> findAll() {
        return sessionRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "sessionById", key = "#id")
    public Optional<Session> getSession(Long id) {
        return sessionRepository.findById(id);
    }

    public Session get(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Session not found: " + id));
    }

    public Session create(Session payload) {
        payload.setId(null);
        return sessionRepository.save(payload);
    }

    public Session update(Long id, Session payload) {
        Session s = get(id);
        if (payload.getCourse() != null) s.setCourse(payload.getCourse());
        if (payload.getAcademicYear() != null) s.setAcademicYear(payload.getAcademicYear());
        if (payload.getCampus() != null) s.setCampus(payload.getCampus());
        if (payload.getRoom() != null) s.setRoom(payload.getRoom());
        if (payload.getStartTime() != null) s.setStartTime(payload.getStartTime());
        if (payload.getEndTime() != null) s.setEndTime(payload.getEndTime());
        if (payload.getQrToken() != null) s.setQrToken(payload.getQrToken());
        if (payload.getExpectedLevel() != null) s.setExpectedLevel(payload.getExpectedLevel());
        if (payload.getExpectedSpecialty() != null) s.setExpectedSpecialty(payload.getExpectedSpecialty());
        return sessionRepository.save(s);
    }

    public void delete(Long id) {
        if (!sessionRepository.existsById(id))
            throw new NotFoundException("Session not found: " + id);
        sessionRepository.deleteById(id);
    }

    // ------------------------- Dashboard -------------------------

    @Transactional(readOnly = true)
    public DashboardDto buildDashboard(Long id) {
        Teacher teacher = teacherRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Teacher not found with hotId: " + id));

        Long userId = teacher.getUser().getId();

        long totalSessions = sessionRepository.countSessionsByUserId(userId);
        Long totalStudents = sessionRepository.countTotalStudents(userId);
        if (totalStudents == null) totalStudents = 0L;

        PresenceSummary sums = sessionRepository.sumPresenceAndAbsence(userId);
        long totalPresent = sums != null && sums.present() != null ? sums.present() : 0;
        long totalAbsent  = sums != null && sums.absent()  != null ? sums.absent()  : 0;

        double avgPresence = totalStudents > 0 
            ? ((double) totalPresent / totalStudents) * 100 
            : 0;

        List<Object[]> courseStats = sessionRepository.groupByCourse(userId);
        List<CoursePresenceDto> coursePresence = new ArrayList<>();
        String mostAttendedCourse = null;
        long maxCount = 0;

        for (Object[] row : courseStats) {
            String courseName = (String) row[0];
            long nbP = row[1] != null ? ((Number) row[1]).longValue() : 0;
            long nbA = row[2] != null ? ((Number) row[2]).longValue() : 0;

            coursePresence.add(new CoursePresenceDto(courseName, nbP, nbA));

            long totalCourse = nbP + nbA;
            if (totalCourse > maxCount) {
                maxCount = totalCourse;
                mostAttendedCourse = courseName;
            }
        }

        DashboardDto dto = new DashboardDto();
        dto.setTotalSessions(totalSessions);
        dto.setTotalStudents(totalStudents);
        dto.setAvgPresence(Math.round(avgPresence));
        dto.setMostAttendedCourse(mostAttendedCourse != null ? mostAttendedCourse : "—");
        dto.setCoursePresence(coursePresence);

        return dto;
    }

    // ------------------------- Création session avec QR (optimisée) -------------------------

 @Transactional
public Session2DTO createSessionAtomic(CreateSessionRequest req) {
    // --- Validation minimale ---
    if ((req.getProfessorId() == null && (req.getUsername() == null || req.getUsername().isBlank()))) {
        throw new IllegalArgumentException("Either professorId or username must be provided");
    }
    if (req.getCourseId() == null || req.getAcademicYearId() == null || req.getCampusId() == null
            || req.getRoomId() == null || req.getExpectedLevelId() == null || req.getExpectedSpecialtyId() == null) {
        throw new IllegalArgumentException("All entity IDs must be provided");
    }

    // --- 1 SELECT User (par id professeur ou username) ---
    User user;
    if (req.getProfessorId() != null) {
        Long professorUserId = entityManager.createQuery(
                "SELECT u.id FROM User u JOIN u.teacher t WHERE u.role = :role AND t.id = :teacherId",
                Long.class)
            .setParameter("role", Role.PROFESSEUR)
            .setParameter("teacherId", req.getProfessorId())
            .setMaxResults(1)
            .getSingleResult();
        user = entityManager.getReference(User.class, professorUserId);
    } else {
        user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + req.getUsername()));
    }

    // --- Références proxies (0 SELECT) ---
    Course courseRef = entityManager.getReference(Course.class, req.getCourseId());
    AcademicYear ayRef = entityManager.getReference(AcademicYear.class, req.getAcademicYearId());
    Campus campusRef = entityManager.getReference(Campus.class, req.getCampusId());
    Room roomRef = entityManager.getReference(Room.class, req.getRoomId());
    Level levelRef = entityManager.getReference(Level.class, req.getExpectedLevelId());
    Specialty specialtyRef = entityManager.getReference(Specialty.class, req.getExpectedSpecialtyId());
    Surveillant surveillantRef = (req.getSurveillantId() != null)
            ? entityManager.getReference(Surveillant.class, req.getSurveillantId())
            : null;

    // --- Créer la session ---
    Session session = new Session();
    session.setUser(user);
    session.setSurveillant(surveillantRef);
    session.setCourse(courseRef);
    session.setAcademicYear(ayRef);
    session.setCampus(campusRef);
    session.setRoom(roomRef);
    session.setExpectedLevel(levelRef);
    session.setExpectedSpecialty(specialtyRef);
    session.setStartTime(req.getStartTime());
    session.setEndTime(req.getEndTime());
    session.setCreated(LocalDateTime.now());
    session.setClosed(false);
    session.setExpiryTime(session.getCreated().plusHours(2));
    session.setNotified(false);

    entityManager.persist(session); // ⚡ plus rapide que saveAndFlush
    entityManager.flush(); // garantit l’id en mémoire, mais évite SELECT supplémentaire

    // --- Génération token + payload en mémoire ---
    String token = qrJwtService.generateQrToken(session.getId(), QR_EXPIRATION_MS);
    session.setQrToken(token);

    Map<String, Object> payloadMap = new HashMap<>();
    payloadMap.put("sessionId", session.getId());
    payloadMap.put("token", token);
    payloadMap.put("issuedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    payloadMap.put("expected_specialty_id", req.getExpectedSpecialtyId());
    payloadMap.put("expected_level_id", req.getExpectedLevelId());
    if (req.getProfessorId() != null) {
        payloadMap.put("professor_teacher_id", req.getProfessorId());
        payloadMap.put("professor_user_id", user.getId());
    }
    if (surveillantRef != null) {
        payloadMap.put("surveillant_id", req.getSurveillantId());
    }
    try {
        session.setQrPayload(objectMapper.writeValueAsString(payloadMap));
    } catch (Exception e) {
        throw new RuntimeException("Failed to serialize QR payload", e);
    }

    // --- INSERT massif attendances (⚡ en une seule exécution SQL) ---
    final String attendanceSql =
            "INSERT INTO attendance (student_year_profile_id, session_id, status, source, \"timestamp\", scanned_at) " +
            "SELECT syp.id, :sessionId, :status, :source, :ts, NULL " +
            "FROM student_year_profile syp " +
            "WHERE syp.level_id = :levelId AND syp.specialty_id = :specialtyId AND syp.academic_year_id = :ayId";

    String sourceValue = Attendance.Source.qr.name().toLowerCase();

    MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("sessionId", session.getId())
            .addValue("status", Attendance.Status.ABSENT.name())
            .addValue("source", sourceValue)
            .addValue("ts", Timestamp.from(Instant.now())) // ✅ plus rapide et sûr
            .addValue("levelId", req.getExpectedLevelId())
            .addValue("specialtyId", req.getExpectedSpecialtyId())
            .addValue("ayId", req.getAcademicYearId());

    namedParameterJdbcTemplate.update(attendanceSql, params);

    // --- Sauver payload/token en DB (flush unique) ---
    entityManager.merge(session);

    // --- DTO final ---
    Session2DTO dto = new Session2DTO();
    dto.setId(session.getId());
    dto.setExpectedLevelId(req.getExpectedLevelId());
    dto.setExpectedSpecialtyId(req.getExpectedSpecialtyId());
    dto.setStartTime(session.getStartTime());
    dto.setEndTime(session.getEndTime());
    dto.setQrToken(token);
    try {
        dto.setQrPayload(objectMapper.readValue(session.getQrPayload(), new TypeReference<>() {}));
    } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to deserialize QR payload", e);
    }

    return dto;
}

    // ------------------------- Gestion scans -------------------------

    @Transactional
    public AttendanceDTO recordScan(ScanRequest req) {
        Session session;
        if (req.getQrToken() != null) {
            session = sessionRepository.findByQrToken(req.getQrToken())
                    .orElseThrow(() -> new NotFoundException("Session not found for token"));
            Long sessionIdFromToken = qrJwtService.validateQrToken(req.getQrToken());
            if (!sessionIdFromToken.equals(session.getId()))
                throw new IllegalStateException("QR token does not match session");
        } else if (req.getSessionId() != null) {
            session = sessionRepository.findById(req.getSessionId())
                    .orElseThrow(() -> new NotFoundException("Session not found: " + req.getSessionId()));
        } else {
            throw new IllegalArgumentException("sessionId or qrToken required");
        }
        return recordScanWithSession(req, session);
    }

    @Transactional
    public AttendanceDTO recordScanWithSession(ScanRequest req, Session session) {
        StudentYearProfile profile = studentYearProfileRepository.findById(req.getStudentYearProfileId())
                .orElseThrow(() -> new NotFoundException("StudentYearProfile not found"));

        if (!profile.getLevel().getId().equals(session.getExpectedLevel().getId()) ||
            !profile.getSpecialty().getId().equals(session.getExpectedSpecialty().getId()) ||
            !profile.getAcademicYear().getId().equals(session.getAcademicYear().getId())) {
            throw new IllegalArgumentException("Profile does not match session expected level/specialty/academic year");
        }
        if (req.getSource() == Attendance.Source.qr && session.getQrToken() == null)
            throw new IllegalStateException("QR token invalid (session closed)");

        Attendance attendance = attendanceRepository
                .findByStudentYearProfileAndSession(profile, session)
                .orElseGet(() -> new Attendance(profile, session));

        attendance.setStatus(Attendance.Status.PRESENT);
        attendance.setSource(req.getSource());
        attendance.setScannedAt(req.getScannedAt() == null ? LocalDateTime.now() : req.getScannedAt());
        attendanceRepository.save(attendance);

        return new AttendanceDTO(attendance.getId(), session.getId(), profile.getId(),
                attendance.getStatus().name(), attendance.getSource().name(), attendance.getScannedAt());
    }

    @Transactional
    public List<AttendanceDTO> recordScansBulk(List<ScanRequest> scans) {
        List<AttendanceDTO> results = new ArrayList<>();
        for (ScanRequest scan : scans) {
            Optional<Session> sOpt = sessionRepository.findById(scan.getSessionId());
            Optional<StudentYearProfile> studentOpt = studentYearProfileRepository.findById(scan.getStudentId());
            if (sOpt.isPresent() && studentOpt.isPresent()) {
                Session session = sOpt.get();
                StudentYearProfile student = studentOpt.get();
                Attendance attendance = attendanceRepository
                        .findBySession_IdAndStudentYearProfile_Id(session.getId(), student.getId())
                        .orElseGet(() -> new Attendance(student, session));
                attendance.setStatus(scan.isPresent() ? Attendance.Status.PRESENT : Attendance.Status.ABSENT);
                attendance.setTimestamp(scan.getTimestamp() != null ? scan.getTimestamp() : Instant.now());
                attendanceRepository.save(attendance);
                results.add(new AttendanceDTO(student.getId(), session.getId(), attendance.getStatus()));
            }
        }
        return results;
    }

    // -------------------- Expected profiles for a session --------------------
    @Transactional(readOnly = true)
public List<ExpectedProfileDTO> getExpectedProfilesForSession(Long sessionId) {
    // Récupérer la session "shallow" (pour level/specialty/ay)
    Session session = sessionRepository.findSessionShallow(sessionId)
            .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));

    if (session.getExpectedLevel() == null || session.getExpectedSpecialty() == null || session.getAcademicYear() == null) {
        return List.of();
    }

    List<ExpectedProfileDTO> dtos = studentYearProfileRepository.findExpectedProfilesForSession(
            sessionId,
            session.getExpectedLevel().getId(),
            session.getExpectedSpecialty().getId(),
            session.getAcademicYear().getId()
    );

    // Optionnel : normaliser les valeurs null (ex: status null -> PENDING)
   

    return dtos;
}

   @Transactional
public void markManualAttendance(Long sessionId, List<Long> presentProfileIds) {
    if (presentProfileIds == null || presentProfileIds.isEmpty()) {
        // log.info("markManualAttendance: input empty for session {}", sessionId);
        return;
    }

    // Log brut (utile pour debug)
    // log.debug("markManualAttendance called for session {} with raw IDs: {}", sessionId, presentProfileIds);

    // Repérer indices des nulls (pour afficher précisément où se trouvent les nulls)
    List<Integer> nullIndices = IntStream.range(0, presentProfileIds.size())
            .filter(i -> presentProfileIds.get(i) == null)
            .mapToObj(Integer::valueOf)
            .collect(Collectors.toList());

    if (!nullIndices.isEmpty()) {
        // log.warn("markManualAttendance: detected {} null(s) at positions {} in provided list for session {}",
        //         nullIndices.size(), nullIndices, sessionId);
    }

    // Nettoyer la liste : enlever les null et dédoublonner (distinct) pour éviter inserts/updates redondants
    List<Long> cleanedIds = presentProfileIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

    if (cleanedIds.isEmpty()) {
        // log.info("markManualAttendance: after cleaning, no valid IDs remain for session {}", sessionId);
        return;
    }

    long t0 = System.nanoTime();

    // Récupérer la session shallow (ex: level/specialty/academic year si besoin)
    Session session = sessionRepository.findSessionShallow(sessionId)
            .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));

    // 1) récupérer en une seule requête les attendances existantes pour ces profiles
    List<Attendance> existing = attendanceRepository.findBySessionIdAndStudentYearProfileIdIn(sessionId, cleanedIds);

    // mapper profileId -> Attendance existante
    Map<Long, Attendance> existingMap = existing.stream()
            .collect(Collectors.toMap(a -> a.getStudentYearProfile().getId(), a -> a));

    // préparer listes update / insert
    List<Attendance> toUpdate = new ArrayList<>();
    List<Long> toInsertProfileIds = new ArrayList<>();

    LocalDateTime now = LocalDateTime.now();

    for (Long pid : cleanedIds) {
        Attendance a = existingMap.get(pid);
        if (a != null) {
            a.setStatus(Attendance.Status.PRESENT);
            a.setSource(Attendance.Source.manual);
            a.setScannedAt(now);
            toUpdate.add(a);
        } else {
            toInsertProfileIds.add(pid);
        }
    }

    int totalUpdated = 0;
    int totalInserted = 0;

    // Batch update existants
    if (!toUpdate.isEmpty()) {
        final String updateSql = "UPDATE attendance SET status = ?, source = ?, scanned_at = ? WHERE id = ?";
        final int batchSize = 1000;
        int total = toUpdate.size();
        for (int start = 0; start < total; start += batchSize) {
            int end = Math.min(start + batchSize, total);
            List<Attendance> chunk = toUpdate.subList(start, end);
            namedParameterJdbcTemplate.getJdbcTemplate().batchUpdate(updateSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                    Attendance a = chunk.get(i);
                    ps.setString(1, a.getStatus().name());
                    ps.setString(2, a.getSource().name().toLowerCase());
                    ps.setTimestamp(3, Timestamp.valueOf(a.getScannedAt()));
                    ps.setLong(4, a.getId());
                }
                @Override
                public int getBatchSize() {
                    return chunk.size();
                }
            });
            totalUpdated += chunk.size();
        }
    }

    // Batch insert manquants
    if (!toInsertProfileIds.isEmpty()) {
        final String insertSql = "INSERT INTO attendance (student_year_profile_id, session_id, status, source, \"timestamp\", scanned_at) VALUES (?, ?, ?, ?, ?, ?)";
        final int batchSize = 1000;
        int total = toInsertProfileIds.size();
        // Utiliser un même timestamp pour le chunk pour cohérence
        for (int start = 0; start < total; start += batchSize) {
            int end = Math.min(start + batchSize, total);
            List<Long> chunk = toInsertProfileIds.subList(start, end);
            Timestamp nowTs = Timestamp.valueOf(now); // reuse the same "now" for consistent rows
            namedParameterJdbcTemplate.getJdbcTemplate().batchUpdate(insertSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                    Long pid = chunk.get(i);
                    // pid ne doit pas être null ici (on a filtré)
                    ps.setLong(1, pid);
                    ps.setLong(2, sessionId);
                    ps.setString(3, Attendance.Status.PRESENT.name());
                    ps.setString(4, Attendance.Source.manual.name().toLowerCase());
                    ps.setTimestamp(5, nowTs); // "timestamp" column
                    ps.setTimestamp(6, nowTs); // scanned_at
                }
                @Override
                public int getBatchSize() {
                    return chunk.size();
                }
            });
            totalInserted += chunk.size();
        }
    }

    long t1 = System.nanoTime();
    long elapsedMs = (t1 - t0) / 1_000_000;

    // log.info("markManualAttendance done for session {}: rawInputCount={}, cleanedCount={}, nullCount={}, updated={}, inserted={}, elapsedMs={}",
            // sessionId, presentProfileIds.size(), cleanedIds.size(), nullIndices.size(), totalUpdated, totalInserted, elapsedMs);

    // Si tu veux, log plus verbeux en debug avec les listes
    // log.debug("markManualAttendance details for session {}: toUpdateIds={}, toInsertIds={}",
    //         sessionId,
    //         toUpdate.stream().map(a -> a.getStudentYearProfile().getId()).collect(Collectors.toList()),
    //         toInsertProfileIds);
}


    @Transactional
    public void endSession(Long sessionId) {
        Session s = get(sessionId);
        s.setQrToken(null);
        if (s.getEndTime() == null) s.setEndTime(LocalDateTime.now());
        sessionRepository.save(s);
    }

    @Transactional
    public void cancelSession(Long sessionId) {
        Session session = get(sessionId);
        sessionRepository.delete(session);
    }


























@Transactional(readOnly = true)
    @Cacheable(
        value = "sessionsByUser",
        key = "T(String).format('%s:%d:%d:%s', #username, #pageable.pageNumber, #pageable.pageSize, (#search == null ? '' : #search))"
    )
    public SessionPageResponse getSessionsByUsername(String username, String search, Pageable pageable) {
        Page<SessionDtoss> page = sessionRepository.findSessionsByUsername(username, search, pageable);

        return new SessionPageResponse(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }

@Cacheable(
    value = "sessionsBySurveillant",
    key = "#surveillantId + '-' + (#teacherName==null?'':#teacherName) + '-' + (#specialtyId==null?'':#specialtyId) + '-' + (#levelId==null?'':#levelId) + '-' + #page + '-' + #size"
)
public SessionsPageDTO getSessionsForSurveillant(Long surveillantId,
                                                 String teacherName,
                                                 Long specialtyId,
                                                 Long levelId,
                                                 int page,
                                                 int size) {
    Pageable pageable = PageRequest.of(page, size);

    if (teacherName != null && teacherName.trim().isEmpty()) {
        teacherName = null;
    }

    Page<Object[]> rawPage = sessionRepository.findSessionsForSurveillantNative(
        surveillantId, teacherName, specialtyId, levelId, pageable
    );

    Page<SessionSummaryDTO> mappedPage = rawPage.map(row -> {
        Long sessionId = ((Number) row[0]).longValue();
        LocalDateTime startTime = row[1] != null ? ((Timestamp) row[1]).toLocalDateTime() : null;
        String courseTitle = (String) row[2];
        String levelName = (String) row[3];
        String specialtyName = (String) row[4];
        String roomName = (String) row[5];
        String campusName = (String) row[6];
        String teacherFullName = (String) row[7];
        Long presentCount = row[8] != null ? ((Number) row[8]).longValue() : 0L;
        Long totalCount = row[9] != null ? ((Number) row[9]).longValue() : 0L;
        Boolean closed = (Boolean) row[10];

        return new SessionSummaryDTO(
            sessionId,
            startTime,
            courseTitle,
            levelName,
            specialtyName,
            roomName,
            campusName,
            teacherFullName,
            presentCount,
            totalCount,
            closed
        );
    });

    return new SessionsPageDTO(
        mappedPage.getContent(),
        mappedPage.getNumber(),
        mappedPage.getSize(),
        mappedPage.getTotalElements(),
        mappedPage.getTotalPages()
    );
}


}
