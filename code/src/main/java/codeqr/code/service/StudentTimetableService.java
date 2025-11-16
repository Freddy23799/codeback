package codeqr.code.service;

import codeqr.code.dto.StudentTimetableResponse;
import codeqr.code.model.*;
import codeqr.code.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class StudentTimetableService {

    private final StudentRepository studentRepo;
    private final StudentYearProfileRepository profileRepo;
    private final EmploiTempsRepository emploiRepo;
    private final CourseRepository courseRepo;
    private final TeacherRepository teacherRepo;
    private final RoomRepository roomRepo;
    private final ResponsableRepository responsableRepo;
    private final SpecialtyRepository specialtyRepo;
    private final LevelRepository levelRepo;
    private final AcademicYearRepository academicYearRepo;

    @Cacheable(value = "studentTimetables", key = "#studentId")
    @Transactional(readOnly = true)
    public StudentTimetableResponse getTimetablesForStudent(Long studentId) {
        if (studentId == null) throw new IllegalArgumentException("studentId required");

        // 1) vérifier que le Student existe
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // 2) récupérer le StudentYearProfile le plus récent pour ce student
        Optional<StudentYearProfile> maybeProfile =
                profileRepo.findTopByStudent_IdOrderByIdDesc(student.getId());

        if (maybeProfile.isEmpty()) {
            StudentTimetableResponse out = new StudentTimetableResponse();
            out.setTotalMatched(0L);
            out.setReturnedWeekCount(0);
            out.setReturnedTimetableCount(0);
            out.setProfile(null);
            out.setWeekGroups(Collections.emptyList());
            return out;
        }
        StudentYearProfile profile = maybeProfile.get();

        Long academicYearId = profile.getAcademicYear() != null ? profile.getAcademicYear().getId() : null;
        Long levelId = profile.getLevel() != null ? profile.getLevel().getId() : null;
        Long specialtyId = profile.getSpecialty() != null ? profile.getSpecialty().getId() : null;

        // profile DTO
        StudentTimetableResponse.ProfileDto profileDto = new StudentTimetableResponse.ProfileDto();
        profileDto.setAcademicYearId(academicYearId);
        profileDto.setLevelId(levelId);
        profileDto.setSpecialtyId(specialtyId);

        academicYearRepo.findById(academicYearId).ifPresent(ay -> profileDto.setAcademicYearLabel(safeGetAcademicYearLabel(ay)));
        levelRepo.findById(levelId).ifPresent(l -> profileDto.setLevelLabel(
                l.getName() != null ? l.getName() : "Niveau " + l.getId()));
        specialtyRepo.findById(specialtyId).ifPresent(s -> profileDto.setSpecialtyLabel(
                s.getName() != null ? s.getName() : "Specialty " + s.getId()));

        // 3) récupérer les emplois correspondant au profil (ordre createdAt desc)
        List<EmploiTemps> emplois = emploiRepo.findByAnneeAcademiqueIdAndNiveauIdAndSpecialiteIdOrderByCreatedAtDesc(
                academicYearId, levelId, specialtyId);

        long totalMatched = emplois == null ? 0L : emplois.size();
        if (emplois == null || emplois.isEmpty()) {
            StudentTimetableResponse respEmpty = new StudentTimetableResponse();
            respEmpty.setTotalMatched(totalMatched);
            respEmpty.setReturnedWeekCount(0);
            respEmpty.setReturnedTimetableCount(0);
            respEmpty.setProfile(profileDto);
            respEmpty.setWeekGroups(Collections.emptyList());
            return respEmpty;
        }

        // 4) group by semaine (key = semaine.id if present, else negative emploi.id)
        Map<Long, LocalDate> weekKeyToDate = new HashMap<>(); // key -> representative date used for sorting
        Map<Long, List<EmploiTemps>> weekToEmplois = new HashMap<>();

        for (EmploiTemps e : emplois) {
            Long key;
            LocalDate keyDate;

            if (e.getSemaine() != null && e.getSemaine().getId() != null) {
                key = e.getSemaine().getId();
                if (e.getSemaine().getDateDebut() != null) {
                    keyDate = e.getSemaine().getDateDebut();
                } else if (e.getCreatedAt() != null) {
                    keyDate = e.getCreatedAt().toLocalDate();
                } else {
                    keyDate = LocalDate.MIN;
                }
            } else {
                // unique negative key for orphan emploi (so it won't collide with any semaine.id)
                key = - (e.getId() != null ? e.getId() : UUID.randomUUID().hashCode());
                keyDate = e.getCreatedAt() != null ? e.getCreatedAt().toLocalDate() : LocalDate.MIN;
            }

            // keep the most recent date for that key (max)
            weekKeyToDate.merge(key, keyDate, (oldD, newD) -> (oldD.isAfter(newD) ? oldD : newD));

            weekToEmplois.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
        }

        // 5) sort week keys by date desc and limit to 2 (most recent first)
        List<Long> sortedWeekKeys = weekKeyToDate.entrySet().stream()
                .sorted(Map.Entry.<Long, LocalDate>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(2)
                .collect(Collectors.toList());

        // flatten emplois for these keys (preserve repository order within each group)
        List<EmploiTemps> filteredEmplois = new ArrayList<>();
        for (Long wk : sortedWeekKeys) {
            List<EmploiTemps> group = weekToEmplois.getOrDefault(wk, Collections.emptyList());
            filteredEmplois.addAll(group);
        }

        // 6) collect referenced ids to batch fetch labels (only for filtered emplois)
        Set<Long> courseIds = new HashSet<>();
        Set<Long> teacherIds = new HashSet<>();
        Set<Long> roomIds = new HashSet<>();
        Set<Long> responsableIds = new HashSet<>();

        for (EmploiTemps e : filteredEmplois) {
            if (e.getLignes() != null) {
                for (LigneEmploiTemps l : e.getLignes()) {
                    if (l.getCoursId() != null) courseIds.add(l.getCoursId());
                    if (l.getProfesseurId() != null) teacherIds.add(l.getProfesseurId());
                    if (l.getSalleId() != null) roomIds.add(l.getSalleId());
                }
            }
            if (e.getSemaine() != null && e.getSemaine().getCreatedBy() != null) {
                responsableIds.add(e.getSemaine().getCreatedBy().getId());
            } else if (e.getCreatedBy() != null) {
                responsableIds.add(e.getCreatedBy().getId());
            }
        }

        // 7) batch fetch (using findAllById which exists on JpaRepository)
        Map<Long, String> courseNames = courseIds.isEmpty() ? Collections.emptyMap() :
                StreamSupport.stream(courseRepo.findAllById(courseIds).spliterator(), false)
                        .collect(Collectors.toMap(Course::getId, this::safeGetCourseLabel));

        Map<Long, String> teacherNames = teacherIds.isEmpty() ? Collections.emptyMap() :
                StreamSupport.stream(teacherRepo.findAllById(teacherIds).spliterator(), false)
                        .collect(Collectors.toMap(Teacher::getId, this::safeGetTeacherLabel));

        Map<Long, String> roomNames = roomIds.isEmpty() ? Collections.emptyMap() :
                StreamSupport.stream(roomRepo.findAllById(roomIds).spliterator(), false)
                        .collect(Collectors.toMap(Room::getId, this::safeGetRoomLabel));

        Map<Long, String> responsableNames = responsableIds.isEmpty() ? Collections.emptyMap() :
                StreamSupport.stream(responsableRepo.findAllById(responsableIds).spliterator(), false)
                        .collect(Collectors.toMap(Responsable::getId, Responsable::getFullName));

        // 8) build weekGroups DTOs
        List<StudentTimetableResponse.WeekGroupDto> weekGroups = new ArrayList<>();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        int idx = 0;
        int totalTimetables = 0;

        for (Long wkKey : sortedWeekKeys) {
            List<EmploiTemps> groupEmplois = weekToEmplois.getOrDefault(wkKey, Collections.emptyList());
            if (groupEmplois.isEmpty()) continue;

            StudentTimetableResponse.WeekGroupDto wg = new StudentTimetableResponse.WeekGroupDto();
            wg.setWeekId(wkKey > 0 ? wkKey : null);

            // representative emploi to extract period dates if possible
            EmploiTemps rep = groupEmplois.get(0);
            if (rep.getSemaine() != null) {
                wg.setPeriodStart(rep.getSemaine().getDateDebut());
                wg.setPeriodEnd(rep.getSemaine().getDateFin());
            } else {
                wg.setPeriodStart(null);
                wg.setPeriodEnd(null);
            }

            wg.setMostRecent(idx == 0);

            List<StudentTimetableResponse.TimetableCardDto> timetables = new ArrayList<>();
            for (EmploiTemps e : groupEmplois) {
                StudentTimetableResponse.TimetableCardDto card = new StudentTimetableResponse.TimetableCardDto();
                card.setId(e.getId());
                card.setClientId("tt-" + (e.getId() != null ? e.getId() : UUID.randomUUID().toString()));
                card.setTitle(e.getTitle());
                card.setStatus(e.getStatus());
                card.setCreatedAt(e.getCreatedAt());
                card.setSemaineId(e.getSemaine() != null ? e.getSemaine().getId() : null);

                Long responsableId = e.getSemaine() != null && e.getSemaine().getCreatedBy() != null
                        ? e.getSemaine().getCreatedBy().getId()
                        : e.getCreatedBy() != null ? e.getCreatedBy().getId() : null;
                card.setResponsableName(responsableId != null ? responsableNames.get(responsableId) : null);

                // lines
                List<StudentTimetableResponse.LineDto> rows = new ArrayList<>();
                if (e.getLignes() != null) {
                    for (LigneEmploiTemps l : e.getLignes()) {
                        StudentTimetableResponse.LineDto ld = new StudentTimetableResponse.LineDto();
                        ld.setJour(l.getJour() != null ? l.getJour().name() : null);
                        ld.setStart(l.getHeureDebut() != null ? l.getHeureDebut().format(timeFmt) : null);
                        ld.setEnd(l.getHeureFin() != null ? l.getHeureFin().format(timeFmt) : null);

                        ld.setCourseId(l.getCoursId());
                        ld.setCourseName(l.getCoursId() != null ? courseNames.get(l.getCoursId()) : null);

                        ld.setProfessorId(l.getProfesseurId());
                        ld.setProfessorName(l.getProfesseurId() != null ? teacherNames.get(l.getProfesseurId()) : null);

                        ld.setRoomId(l.getSalleId());
                        ld.setRoomName(l.getSalleId() != null ? roomNames.get(l.getSalleId()) : null);

                        rows.add(ld);
                    }
                }
                card.setRows(rows);
                card.setNotes(e.getNotes());
                timetables.add(card);
            }

            totalTimetables += timetables.size();
            wg.setTimetables(timetables);
            weekGroups.add(wg);
            idx++;
        }

        // 9) assemble response
        StudentTimetableResponse resp = new StudentTimetableResponse();
        resp.setTotalMatched(totalMatched);
        resp.setReturnedWeekCount(weekGroups.size());
        resp.setReturnedTimetableCount(totalTimetables);
        resp.setProfile(profileDto);
        resp.setWeekGroups(weekGroups);
        return resp;
    }

    // helpers
    private String safeGetCourseLabel(Course c) {
        if (c == null) return null;
        try {
            if (c.getTitle() != null) return c.getTitle();
        } catch (Exception ignored) {}
       
        return "Cours #" + (c.getId() != null ? c.getId() : "??");
    }

    private String safeGetTeacherLabel(Teacher t) {
        if (t == null) return null;
        try {
            if (t.getFullName() != null) return t.getFullName();
        } catch (Exception ignored) {}
      
        return "Prof #" + (t.getId() != null ? t.getId() : "??");
    }

    private String safeGetRoomLabel(Room r) {
        if (r == null) return null;
        try {
            if (r.getName() != null) return r.getName();
        } catch (Exception ignored) {}
       
        return "Salle #" + (r.getId() != null ? r.getId() : "??");
    }

    private String safeGetAcademicYearLabel(AcademicYear ay) {
        if (ay == null) return null;
        try {
            if (ay.getLabel() != null) return ay.getLabel();
        } catch (Exception ignored) {}
       
        return ay.toString();
    }
}
