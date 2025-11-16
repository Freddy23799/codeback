package codeqr.code.service;

import codeqr.code.dto.DashboardDtos;
import codeqr.code.dto.LineDto;
import codeqr.code.dto.TimetableCardDto;
import codeqr.code.model.EmploiTemps;
import codeqr.code.model.LigneEmploiTemps;
import codeqr.code.model.SemaineEmploiTemps;
import codeqr.code.repository.CourseRepository;
import codeqr.code.repository.EmploiTempsRepository;
import codeqr.code.repository.LevelRepository;
import codeqr.code.repository.RoomRepository;
import codeqr.code.repository.SemaineEmploiTempsRepository;
import codeqr.code.repository.SpecialtyRepository; // <-- changed
import codeqr.code.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
/**
 * DashboardServices (specialty-aware)
 * - Récupère la DERNIÈRE semaine (SemaineEmploiTemps) du teacher
 * - Récupère tous les EmploiTemps liés à cette semaine (toutes les cartes)
 * - Enrichit lignes (courseName, teacherName, roomTitle) via batch fetch
 * - Enrichit les cartes avec specialtyLabel et levelLabel (batch)
 */
@Service
@RequiredArgsConstructor
public class DashboardServices {

    private final SemaineEmploiTempsRepository semaineRepo;
    private final EmploiTempsRepository emploiTempsRepo;
    private final CourseRepository courseRepo;         // List<Course> findAllByIdIn(Collection<Long> ids)
    private final TeacherRepository teacherRepo;       // List<Teacher> findAllByIdIn(Collection<Long> ids)
    private final RoomRepository roomRepo;             // List<Room> findAllByIdIn(Collection<Long> ids)
    private final SpecialtyRepository specialtyRepo;   // List<Specialty> findAllByIdIn(Collection<Long> ids)
    private final LevelRepository levelRepo;           // List<Level> findAllByIdIn(Collection<Long> ids)

    @Transactional(readOnly = true)
    @Cacheable(value = "dashboards", key = "#userId")
    public DashboardDtos getDashboardForUser(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId required");

        // total all time
        long totalCount = emploiTempsRepo.countByCreatedBy_Id(userId);

        // find latest semaine
        Optional<SemaineEmploiTemps> maybeSemaine = semaineRepo.findTopByCreatedBy_IdOrderByDateDebutDesc(userId);
        if (maybeSemaine.isEmpty()) {
            DashboardDtos empty = new DashboardDtos();
            empty.setTotalCount(totalCount);
            empty.setWeeklyCount(0L);
            empty.setWeeklyTimetables(Collections.emptyList());
            return empty;
        }
        SemaineEmploiTemps semaine = maybeSemaine.get();
        Long semaineId = semaine.getId();

        // load all EmploiTemps for that semaine (should prefetch lignes)
        List<EmploiTemps> emplois = emploiTempsRepo.findBySemaine_IdOrderByIdDesc(semaineId);

        // collect ids
        Set<Long> courseIds = new HashSet<>();
        Set<Long> teacherIds = new HashSet<>();
        Set<Long> roomIds = new HashSet<>();
        Set<Long> specialtyIds = new HashSet<>();
        Set<Long> levelIds = new HashSet<>();

        for (EmploiTemps e : emplois) {
            Long spId = extractLongFromEntity(e, "getSpecialiteId", "getSpecialityId", "getSpecialtyId");
            if (spId != null) specialtyIds.add(spId);
            Long lvId = extractLongFromEntity(e, "getNiveauId", "getLevelId");
            if (lvId != null) levelIds.add(lvId);

            if (e.getLignes() == null) continue;
            for (LigneEmploiTemps l : e.getLignes()) {
                if (l.getCoursId() != null) courseIds.add(l.getCoursId());
                if (l.getProfesseurId() != null) teacherIds.add(l.getProfesseurId());
                if (l.getSalleId() != null) roomIds.add(l.getSalleId());
            }
        }

        // batch fetch linked entities
        Map<Long, String> courseNames = new HashMap<>();
        if (!courseIds.isEmpty()) {
            courseRepo.findAllByIdIn(courseIds).forEach(c -> courseNames.put(invokeId(c), getCourseLabelSafely(c)));
        }

        Map<Long, String> teacherNames = new HashMap<>();
        if (!teacherIds.isEmpty()) {
            teacherRepo.findAllByIdIn(teacherIds).forEach(t -> teacherNames.put(invokeId(t), getTeacherLabelSafely(t)));
        }

        Map<Long, String> roomTitles = new HashMap<>();
        if (!roomIds.isEmpty()) {
            roomRepo.findAllByIdIn(roomIds).forEach(r -> roomTitles.put(invokeId(r), getRoomLabelSafely(r)));
        }

        // batch fetch specialty & level labels
        Map<Long, String> specialtyLabels = new HashMap<>();
        if (!specialtyIds.isEmpty()) {
            specialtyRepo.findAllByIdIn(specialtyIds).forEach(s -> specialtyLabels.put(invokeId(s), getSpecialtyLabelSafely(s)));
        }

        Map<Long, String> levelLabels = new HashMap<>();
        if (!levelIds.isEmpty()) {
            levelRepo.findAllByIdIn(levelIds).forEach(l -> levelLabels.put(invokeId(l), getLevelLabelSafely(l)));
        }

        // map emplois -> DTO
        List<TimetableCardDto> cards = emplois.stream()
                .map(e -> mapEmploiToDto(e, courseNames, teacherNames, roomTitles, specialtyLabels, levelLabels))
                .collect(Collectors.toList());

        DashboardDtos dto = new DashboardDtos();
        dto.setTotalCount(totalCount);
        dto.setWeeklyCount((long) cards.size());
        dto.setWeeklyTimetables(cards);
        return dto;
    }

    /* ---------------- mapping ---------------- */
    private TimetableCardDto mapEmploiToDto(EmploiTemps e,
                                            Map<Long,String> courseNames,
                                            Map<Long,String> teacherNames,
                                            Map<Long,String> roomTitles,
                                            Map<Long,String> specialtyLabels,
                                            Map<Long,String> levelLabels) {
        TimetableCardDto dto = new TimetableCardDto();
        dto.setId(e.getId());
        dto.setClientId("tt-" + (e.getId() != null ? e.getId() : System.currentTimeMillis()));

        // extract specialtyId & levelId robustly (try multiple getters)
        Long specialtyId = extractLongFromEntity(e, "getSpecialiteId", "getSpecialityId", "getSpecialtyId");
        Long levelId = extractLongFromEntity(e, "getNiveauId", "getLevelId");

        // set numeric ids (keep compatibility: DTO must have these fields)
        dto.setSpecialtyId(specialtyId);
        dto.setLevelId(levelId);
        dto.setAcademicYear(e.getAnneeAcademiqueId() != null ? String.valueOf(e.getAnneeAcademiqueId()) : null);

        // set labels (display names)
        dto.setSpecialtyLabel(specialtyId != null ? specialtyLabels.getOrDefault(specialtyId, null) : null);
        dto.setLevelLabel(levelId != null ? levelLabels.getOrDefault(levelId, null) : null);

        if (e.getSemaine() != null) {
            dto.setPeriodStart(e.getSemaine().getDateDebut());
            dto.setPeriodEnd(e.getSemaine().getDateFin());
        }
        dto.setStatus(e.getStatus());
        dto.setCreatedByName(e.getCreatedBy() != null ? safeUserLabel(e.getCreatedBy().getId()) : null);
        dto.setCreatedAt(e.getCreatedAt());

        List<LineDto> rows = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        if (e.getLignes() != null) {
            for (LigneEmploiTemps l : e.getLignes()) {
                LineDto ld = new LineDto();
                ld.setJour(l.getJour() != null ? l.getJour().name() : null);
                try {
                    ld.setStart(l.getHeureDebut() != null ? l.getHeureDebut().format(fmt) : null);
                    ld.setEnd(l.getHeureFin() != null ? l.getHeureFin().format(fmt) : null);
                } catch (DateTimeException ignored) { ld.setStart(null); ld.setEnd(null); }

                Long cId = l.getCoursId();
                Long tId = l.getProfesseurId();
                Long rId = l.getSalleId();

                ld.setCourseId(cId);
                ld.setCourseName(cId != null ? courseNames.getOrDefault(cId, null) : null);

                // teacher / professor naming kept in DTO for compatibility
                ld.setProfessorId(tId);
                ld.setProfessorName(tId != null ? teacherNames.getOrDefault(tId, null) : null);

                ld.setRoomId(rId);
                ld.setRoomName(rId != null ? roomTitles.getOrDefault(rId, null) : null);

                rows.add(ld);
            }
        }
        dto.setRows(rows);
        return dto;
    }

    /* ----------------- helpers (reflection-safe) ----------------- */

    /**
     * Try several getter names on an entity to return a Long id, or null.
     * e.g. extractLongFromEntity(e, "getSpecialiteId","getSpecialityId","getSpecialtyId")
     */
    private Long extractLongFromEntity(Object entity, String... getterNames) {
        if (entity == null) return null;
        for (String g : getterNames) {
            try {
                Method m = entity.getClass().getMethod(g);
                Object val = m.invoke(entity);
                if (val instanceof Number) return ((Number) val).longValue();
                if (val instanceof String) {
                    try { return Long.valueOf((String) val); } catch (NumberFormatException ignored) {}
                }
            } catch (NoSuchMethodException ignored) {
                // try next
            } catch (Exception ex) {
                // unexpected -> continue trying others
            }
        }
        return null;
    }

    /**
     * invokeId: try to read getId() on an entity and return Long, used to key map population.
     * If reflection fails returns null (but repo objects normally have getId()).
     */
    private Long invokeId(Object entity) {
        if (entity == null) return null;
        try {
            Method m = entity.getClass().getMethod("getId");
            Object val = m.invoke(entity);
            if (val instanceof Number) return ((Number) val).longValue();
            if (val instanceof String) {
                try { return Long.valueOf((String) val); } catch (NumberFormatException ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getCourseLabelSafely(Object course) {
        if (course == null) return null;
        try {
            try { return (String) course.getClass().getMethod("getTitle").invoke(course); } catch (NoSuchMethodException ignored) {}
            try { return (String) course.getClass().getMethod("getName").invoke(course); } catch (NoSuchMethodException ignored) {}
            Object id = course.getClass().getMethod("getId").invoke(course);
            return "Course #" + (id != null ? id.toString() : "");
        } catch (Exception ex) {
            return null;
        }
    }

    private String getTeacherLabelSafely(Object teacher) {
        if (teacher == null) return null;
        try {
            try { return (String) teacher.getClass().getMethod("getFullName").invoke(teacher); } catch (NoSuchMethodException ignored) {}
            try { return (String) teacher.getClass().getMethod("getName").invoke(teacher); } catch (NoSuchMethodException ignored) {}
            Object id = teacher.getClass().getMethod("getId").invoke(teacher);
            return "Teacher #" + (id != null ? id.toString() : "");
        } catch (Exception ex) {
            return null;
        }
    }

    private String getRoomLabelSafely(Object room) {
        if (room == null) return null;
        try {
            try { return (String) room.getClass().getMethod("getTitle").invoke(room); } catch (NoSuchMethodException ignored) {}
            try { return (String) room.getClass().getMethod("getName").invoke(room); } catch (NoSuchMethodException ignored) {}
            Object id = room.getClass().getMethod("getId").invoke(room);
            return "Salle #" + (id != null ? id.toString() : "");
        } catch (Exception ex) {
            return null;
        }
    }

    private String getSpecialtyLabelSafely(Object specialty) {
        if (specialty == null) return null;
        try {
            try { return (String) specialty.getClass().getMethod("getName").invoke(specialty); } catch (NoSuchMethodException ignored) {}
            try { return (String) specialty.getClass().getMethod("getLabel").invoke(specialty); } catch (NoSuchMethodException ignored) {}
            try { return (String) specialty.getClass().getMethod("getTitle").invoke(specialty); } catch (NoSuchMethodException ignored) {}
            Object id = specialty.getClass().getMethod("getId").invoke(specialty);
            return "Specialty #" + (id != null ? id.toString() : "");
        } catch (Exception ex) {
            return null;
        }
    }

    private String getLevelLabelSafely(Object level) {
        if (level == null) return null;
        try {
            try { return (String) level.getClass().getMethod("getName").invoke(level); } catch (NoSuchMethodException ignored) {}
            try { return (String) level.getClass().getMethod("getLabel").invoke(level); } catch (NoSuchMethodException ignored) {}
            try { return (String) level.getClass().getMethod("getTitle").invoke(level); } catch (NoSuchMethodException ignored) {}
            Object id = level.getClass().getMethod("getId").invoke(level);
            return "Level #" + (id != null ? id.toString() : "");
        } catch (Exception ex) {
            return null;
        }
    }

    private String safeUserLabel(Long userId) {
        return userId != null ? String.valueOf(userId) : null;
    }
}
