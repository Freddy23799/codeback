package codeqr.code.service;

import codeqr.code.dto.ProfessorTimetableResponse;
import codeqr.code.model.EmploiTemps;
import codeqr.code.model.LigneEmploiTemps;
import codeqr.code.model.Teacher;
import codeqr.code.model.Specialty;
import codeqr.code.model.Level;
import codeqr.code.model.AcademicYear;
import codeqr.code.repository.CourseRepository;
import codeqr.code.repository.EmploiTempsRepository;
import codeqr.code.repository.LigneEmploiTempsRepository;
import codeqr.code.repository.RoomRepository;
import codeqr.code.repository.ResponsableRepository;
import codeqr.code.repository.TeacherRepository;
import codeqr.code.repository.SpecialtyRepository;
import codeqr.code.repository.LevelRepository;
import codeqr.code.repository.AcademicYearRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfessorTimetableService {

    private final LigneEmploiTempsRepository ligneRepo;
    private final CourseRepository courseRepo;
    private final RoomRepository roomRepo;
    private final ResponsableRepository responsableRepo;
    private final EmploiTempsRepository emploiRepo;

    // nouveaux repos pour labels (noms d'entités adaptés)
    private final SpecialtyRepository specialiteRepo;
    private final LevelRepository niveauRepo;
    private final AcademicYearRepository anneeRepo;

    // repo professeur pour le profile (optionnel)
    private final TeacherRepository teacherRepo;

    @Transactional(readOnly = true)
      @Cacheable(value = "professeurTimetables", key = "#professeurId")
    public ProfessorTimetableResponse getTimetablesForProfessor(Long professeurId) {
        if (professeurId == null) throw new IllegalArgumentException("professeurId required");

        // 1) récupérer les 2 ids de semaines distinctes (les plus récentes)
        List<Long> weekIds = ligneRepo.findDistinctWeekIdsForProfessor(professeurId, PageRequest.of(0, 2));

        ProfessorTimetableResponse resp = new ProfessorTimetableResponse();
        resp.setTotalMatched(0L);
        resp.setReturnedWeekCount(0);
        resp.setReturnedTimetableCount(0);
        resp.setWeekGroups(Collections.emptyList());

        // populate professor profile if exists
        Optional<Teacher> tOpt = teacherRepo.findById(professeurId);
        if (tOpt.isPresent()) {
            Teacher t = tOpt.get();
            ProfessorTimetableResponse.ProfessorDto pd = new ProfessorTimetableResponse.ProfessorDto();
            pd.setProfessorId(t.getId());
            pd.setFullName(t.getFullName());
            resp.setProfile(pd);
        }

        if (weekIds == null || weekIds.isEmpty()) return resp;

        // 2) récupérer toutes les lignes du prof pour ces semaines (entity graph charge emploiTemps + semaine)
        List<LigneEmploiTemps> lines =
                ligneRepo.findByProfesseurIdAndEmploiTempsSemaineIdIn(professeurId, weekIds);

        // 3) batch fetch labels and other metadata
        Set<Long> courseIds = new HashSet<>();
        Set<Long> roomIds = new HashSet<>();
        Set<Long> responsableIds = new HashSet<>();
        Set<Long> specialiteIds = new HashSet<>();
        Set<Long> niveauIds = new HashSet<>();
        Set<Long> anneeIds = new HashSet<>();
        Map<Long, EmploiTemps> emploiById = new HashMap<>();

        for (LigneEmploiTemps l : lines) {
            if (l.getCoursId() != null) courseIds.add(l.getCoursId());
            if (l.getSalleId() != null) roomIds.add(l.getSalleId());
            if (l.getEmploiTemps() != null) {
                EmploiTemps e = l.getEmploiTemps();
                emploiById.putIfAbsent(e.getId(), e);
                if (e.getCreatedBy() != null && e.getCreatedBy().getId() != null)
                    responsableIds.add(e.getCreatedBy().getId());
                if (e.getSpecialiteId() != null) specialiteIds.add(e.getSpecialiteId());
                if (e.getNiveauId() != null) niveauIds.add(e.getNiveauId());
                if (e.getAnneeAcademiqueId() != null) anneeIds.add(e.getAnneeAcademiqueId());
            }
        }

        Map<Long, String> courseNames = courseIds.isEmpty() ? Collections.emptyMap()
                : courseRepo.findAllByIdIn(courseIds).stream()
                .collect(Collectors.toMap(
                        c -> c.getId(),
                        c -> c.getTitle() != null ? c.getTitle() : ("Cours #" + c.getId()),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        Map<Long, String> roomNames = roomIds.isEmpty() ? Collections.emptyMap()
                : roomRepo.findAllByIdIn(roomIds).stream()
                .collect(Collectors.toMap(
                        r -> r.getId(),
                        r -> r.getName() != null ? r.getName() : ("Salle #" + r.getId()),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        Map<Long, String> responsableNames = responsableIds.isEmpty() ? Collections.emptyMap()
                : responsableRepo.findAllById(responsableIds).stream()
                .collect(Collectors.toMap(
                        res -> res.getId(),
                        res -> res.getFullName(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        Map<Long, String> specialiteLabels = specialiteIds.isEmpty() ? Collections.emptyMap()
                : specialiteRepo.findAllByIdIn(specialiteIds).stream()
                .collect(Collectors.toMap(
                        Specialty::getId,
                        s -> s.getName() != null ? s.getName() : ("Specialty #" + s.getId()),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        Map<Long, String> niveauLabels = niveauIds.isEmpty() ? Collections.emptyMap()
                : niveauRepo.findAllByIdIn(niveauIds).stream()
                .collect(Collectors.toMap(
                        Level::getId,
                        l -> l.getName() != null ? l.getName() : ("Level #" + l.getId()),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        Map<Long, String> anneeLabels = anneeIds.isEmpty() ? Collections.emptyMap()
                : anneeRepo.findAllByIdIn(anneeIds).stream()
                .collect(Collectors.toMap(
                        AcademicYear::getId,
                        AcademicYear::getLabel,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        // 4) grouper lines par semaineId -> (specialite,niveau,annee) -> emploiTempsId -> lignes
        class Key3 {
            final Long specialiteId;
            final Long niveauId;
            final Long anneeId;

            Key3(Long s, Long n, Long a) {
                this.specialiteId = s;
                this.niveauId = n;
                this.anneeId = a;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Key3 k = (Key3) o;
                return Objects.equals(specialiteId, k.specialiteId) &&
                        Objects.equals(niveauId, k.niveauId) &&
                        Objects.equals(anneeId, k.anneeId);
            }

            @Override
            public int hashCode() {
                return Objects.hash(specialiteId, niveauId, anneeId);
            }
        }

        Map<Long, Map<Key3, Map<Long, List<LigneEmploiTemps>>>> structured = new LinkedHashMap<>();

        for (LigneEmploiTemps l : lines) {
            if (l.getEmploiTemps() == null || l.getEmploiTemps().getSemaine() == null) continue;
            Long weekId = l.getEmploiTemps().getSemaine().getId();
            EmploiTemps e = l.getEmploiTemps();

            Key3 k = new Key3(e.getSpecialiteId(), e.getNiveauId(), e.getAnneeAcademiqueId());
            structured
                    .computeIfAbsent(weekId, w -> new LinkedHashMap<>())
                    .computeIfAbsent(k, kk -> new LinkedHashMap<>())
                    .computeIfAbsent(e.getId(), id -> new ArrayList<>())
                    .add(l);
        }

        // 5) construire la réponse DTO
        List<ProfessorTimetableResponse.WeekGroupDto> weekGroups = new ArrayList<>();
        int totalTimetables = 0;
        boolean first = true;

        for (Long wkId : weekIds) {
            Map<Key3, Map<Long, List<LigneEmploiTemps>>> groupsForWeek =
                    structured.getOrDefault(wkId, Collections.emptyMap());
            if (groupsForWeek.isEmpty()) continue;

            ProfessorTimetableResponse.WeekGroupDto wg = new ProfessorTimetableResponse.WeekGroupDto();
            // take any emploi from this week to set periodStart/End
            EmploiTemps rep = null;
            for (Map<Long, List<LigneEmploiTemps>> m : groupsForWeek.values()) {
                if (!m.isEmpty()) {
                    Long someEmploiId = m.keySet().iterator().next();
                    rep = emploiById.get(someEmploiId);
                    break;
                }
            }
            if (rep != null && rep.getSemaine() != null) {
                wg.setPeriodStart(rep.getSemaine().getDateDebut());
                wg.setPeriodEnd(rep.getSemaine().getDateFin());
            }
            wg.setWeekId(wkId);
            wg.setMostRecent(first);
            first = false;

            List<ProfessorTimetableResponse.SpecialityGroupDto> specGroups = new ArrayList<>();
            for (Map.Entry<Key3, Map<Long, List<LigneEmploiTemps>>> entry : groupsForWeek.entrySet()) {
                Key3 k = entry.getKey();
                ProfessorTimetableResponse.SpecialityGroupDto sg = new ProfessorTimetableResponse.SpecialityGroupDto();
                sg.setSpecialiteId(k.specialiteId);
                sg.setNiveauId(k.niveauId);
                sg.setAnneeAcademiqueId(k.anneeId);
                sg.setSpecialiteLabel(k.specialiteId != null ? specialiteLabels.get(k.specialiteId) : null);
                sg.setNiveauLabel(k.niveauId != null ? niveauLabels.get(k.niveauId) : null);
                sg.setAnneeAcademiqueLabel(k.anneeId != null ? anneeLabels.get(k.anneeId) : null);

                List<ProfessorTimetableResponse.TimetableCardDto> ttDtos = new ArrayList<>();
                for (Map.Entry<Long, List<LigneEmploiTemps>> emploiEntry : entry.getValue().entrySet()) {
                    Long emploiId = emploiEntry.getKey();
                    EmploiTemps emploi = emploiById.get(emploiId);
                    List<LigneEmploiTemps> llist = emploiEntry.getValue();

                    ProfessorTimetableResponse.TimetableCardDto card = new ProfessorTimetableResponse.TimetableCardDto();
                    card.setId(emploiId);
                    card.setClientId("tt-" + emploiId);
                    card.setTitle(emploi != null ? emploi.getTitle() : null);
                    card.setStatus(emploi != null ? emploi.getStatus() : null);
                    card.setCreatedAt(emploi != null ? emploi.getCreatedAt() : null);
                    card.setSemaineId(wkId);
                    card.setSpecialiteId(k.specialiteId);
                    card.setNiveauId(k.niveauId);
                    card.setAnneeAcademiqueId(k.anneeId);
                    card.setSpecialiteLabel(sg.getSpecialiteLabel());
                    card.setNiveauLabel(sg.getNiveauLabel());
                    card.setAnneeAcademiqueLabel(sg.getAnneeAcademiqueLabel());

                    if (emploi != null && emploi.getCreatedBy() != null && emploi.getCreatedBy().getId() != null) {
                        card.setResponsableName(responsableNames.get(emploi.getCreatedBy().getId()));
                    }

                    card.setRows(llist.stream()
                            .sorted(Comparator.comparing(l -> l.getOrdreSmall() == null ? 0 : l.getOrdreSmall()))
                            .map(l -> {
                                ProfessorTimetableResponse.LineDto ld = new ProfessorTimetableResponse.LineDto();
                                ld.setJour(l.getJour() != null ? l.getJour().name() : null);
                                ld.setStart(l.getHeureDebut() != null ? l.getHeureDebut().toString() : null);
                                ld.setEnd(l.getHeureFin() != null ? l.getHeureFin().toString() : null);
                                ld.setCourseId(l.getCoursId());
                                ld.setCourseName(l.getCoursId() != null ? courseNames.get(l.getCoursId()) : null);
                                ld.setProfessorId(l.getProfesseurId());
                                ld.setRoomId(l.getSalleId());
                                ld.setRoomName(l.getSalleId() != null ? roomNames.get(l.getSalleId()) : null);
                                ld.setOrdreSmall(l.getOrdreSmall());
                                // additionally include the emploi's grouping ids
                                ld.setSpecialiteId(k.specialiteId);
                                ld.setNiveauId(k.niveauId);
                                ld.setAnneeAcademiqueId(k.anneeId);
                                return ld;
                            })
                            .collect(Collectors.toList()));

                    card.setNotes(emploi != null ? emploi.getNotes() : null);
                    totalTimetables++;
                    ttDtos.add(card);
                }

                sg.setTimetables(ttDtos);
                specGroups.add(sg);
            }

            wg.setSpecGroups(specGroups);
            weekGroups.add(wg);
        }

        resp.setTotalMatched((long) lines.size());
        resp.setReturnedWeekCount(weekGroups.size());
        resp.setReturnedTimetableCount(totalTimetables);
        resp.setWeekGroups(weekGroups);

        return resp;
    }
}
