package codeqr.code.service;

import codeqr.code.dto.*;
import codeqr.code.dto.TimetableConflictException;
import codeqr.code.repository.TimetableWriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TimetablePublishService {

    private final TimetableWriteRepository repo;

    public TimetablePublishService(TimetableWriteRepository repo) {
        this.repo = repo;
    }

    /**
     * Publish (create) new semaine + emplois + lignes.
     * NOTE: ici on ne supprime plus d'anciennes semaines automatique (côté DB géré).
     */
    @Transactional
    public PublishResponse publish(PublishPayload payload) {
        final Long createdBy = validateAndExtractCreatedBy(payload);
        final LocalDate dateDebut = validateAndExtractDateDebut(payload);
        final LocalDate dateFin = resolveDateFin(payload, dateDebut);

        // Acquire advisory lock to serialize publications on the same week
        repo.acquireAdvisoryLockForWeek(dateDebut);

        // Collect all candidate rows from payload
        List<RowPayload> allCandidates = gatherAllRowsFromPayload(payload);

        // Check conflicts in DB for this week (excludeSemaineId = null for new insert)
        List<ConflictDto> conflicts = repo.findConflictsForWeek(dateDebut, allCandidates, null);
        if (!conflicts.isEmpty()) {
            // Stop insertion and return conflict details via exception
            throw new TimetableConflictException(conflicts);
        }

        // 1) insert semaine
        Long semaineId = repo.insertSemaine(dateDebut, dateFin, createdBy);

        // 2) insert emplois + lignes
        List<EmploiInsertedInfo> inserted = insertEmploisAndLignesForSemaine(semaineId, payload, createdBy);

        PublishResponse resp = new PublishResponse();
        resp.setSemaineId(semaineId);
        resp.setEmplois(inserted);
        resp.setMessage("Semaine publiée avec succès");
        return resp;
    }

    /**
     * Update an existing semaine: update semaine metadata (dates) + replace emplois/lignes.
     * This keeps the same semaineId (no deletion of the semaine row itself).
     */
    @Transactional
    public PublishResponse update(Long semaineId, PublishPayload payload) {
        if (semaineId == null) throw new IllegalArgumentException("semaineId requis pour update");

        final Long createdBy = validateAndExtractCreatedBy(payload);
        final LocalDate dateDebut = validateAndExtractDateDebut(payload);
        final LocalDate dateFin = resolveDateFin(payload, dateDebut);

        // Acquire advisory lock for the week (serialize updates for that week)
        repo.acquireAdvisoryLockForWeek(dateDebut);

        // Gather candidate rows from payload
        List<RowPayload> allCandidates = gatherAllRowsFromPayload(payload);

        // Check conflicts against DB but exclude the current semaineId (we will replace its lines)
        List<ConflictDto> conflicts = repo.findConflictsForWeek(dateDebut, allCandidates, semaineId);
        if (!conflicts.isEmpty()) {
            throw new TimetableConflictException(conflicts);
        }

        // Update the semaine dates (will throw if nothing updated)
        int updatedRows = repo.updateSemaineDates(semaineId, dateDebut, dateFin, createdBy);
        if (updatedRows <= 0) {
            throw new IllegalArgumentException("Semaine introuvable : " + semaineId);
        }

        // Remove existing emplois + lignes for that semaine, then re-insert
        repo.deleteEmploisAndLignes(semaineId);

        List<EmploiInsertedInfo> inserted = insertEmploisAndLignesForSemaine(semaineId, payload, createdBy);

        PublishResponse resp = new PublishResponse();
        resp.setSemaineId(semaineId);
        resp.setEmplois(inserted);
        resp.setMessage("Semaine mise à jour avec succès");
        return resp;
    }

    /* --- helpers --- */

    private Long validateAndExtractCreatedBy(PublishPayload payload) {
        if (payload == null) throw new IllegalArgumentException("Payload absent");
        final String createdByRaw = payload.getCreatedBy();
        if (!StringUtils.hasText(createdByRaw)) throw new IllegalArgumentException("createdBy absent");
        Long createdBy;
        try { createdBy = Long.parseLong(createdByRaw); } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("createdBy must be numeric");
        }
        return createdBy;
    }

    private LocalDate validateAndExtractDateDebut(PublishPayload payload) {
        if (payload.getSemaine() == null || !StringUtils.hasText(payload.getSemaine().getDateDebut())) {
            throw new IllegalArgumentException("Semaine.dateDebut requis");
        }

        LocalDate dateDebut;
        try {
            dateDebut = LocalDate.parse(payload.getSemaine().getDateDebut());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Format dateDebut invalide (YYYY-MM-DD)");
        }

        if (dateDebut.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("dateDebut doit être un Lundi");
        }
        return dateDebut;
    }

    private LocalDate resolveDateFin(PublishPayload payload, LocalDate dateDebut) {
        LocalDate dateFin;
        if (payload.getSemaine() != null && payload.getSemaine().getDateFin() != null && !payload.getSemaine().getDateFin().isBlank()) {
            dateFin = LocalDate.parse(payload.getSemaine().getDateFin());
            if (!dateFin.equals(dateDebut.plusDays(6))) {
                throw new IllegalArgumentException("dateFin doit être dateDebut + 6 jours (Dimanche)");
            }
        } else {
            dateFin = dateDebut.plusDays(6);
        }
        return dateFin;
    }

    private List<EmploiInsertedInfo> insertEmploisAndLignesForSemaine(Long semaineId, PublishPayload payload, Long createdBy) {
        List<EmploiInsertedInfo> inserted = new ArrayList<>();

        List<EmploiPayload> emplois = payload.getEmplois() == null ? Collections.emptyList() : payload.getEmplois();
        for (EmploiPayload e : emplois) {
            Long emploiId = repo.insertEmploi(semaineId,
                    e.getSpecialiteId(),
                    e.getNiveauId(),
                    e.getAnneeAcademiqueId(),
                    e.getStatus(),
                    createdBy,
                    null,
                    null);

            List<Long> ligneIds = new ArrayList<>();
            List<RowPayload> rows = e.getRows() == null ? Collections.emptyList() : e.getRows();
            int ordre = 0;
            for (RowPayload r : rows) {
                ordre++;
                Long ligneId = repo.insertLigne(
                        emploiId,
                        r.getJour(),
                        r.getStart(),
                        r.getEnd(),
                        r.getCourseId(),
                        r.getProfessorId(),
                        r.getRoomId(),
                        ordre
                );
                ligneIds.add(ligneId);
            }

            inserted.add(new EmploiInsertedInfo(emploiId, e.getSpecialiteId(), e.getNiveauId(), ligneIds));
        }
        return inserted;
    }

    private List<RowPayload> gatherAllRowsFromPayload(PublishPayload payload) {
        List<RowPayload> all = new ArrayList<>();
        List<EmploiPayload> emplois = payload.getEmplois() == null ? Collections.emptyList() : payload.getEmplois();
        for (EmploiPayload e : emplois) {
            List<RowPayload> rows = e.getRows() == null ? Collections.emptyList() : e.getRows();
            all.addAll(rows);
        }
        return all;
    }
}
