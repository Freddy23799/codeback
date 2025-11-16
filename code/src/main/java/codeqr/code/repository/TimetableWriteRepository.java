package codeqr.code.repository;

import org.springframework.lang.Nullable;
import java.time.LocalDate;
import java.util.List;
import codeqr.code.dto.ConflictDto;
import codeqr.code.dto.RowPayload;

public interface TimetableWriteRepository {
    Long insertSemaine(LocalDate dateDebut, LocalDate dateFin, Long createdBy);
    Long insertEmploi(Long semaineId, Long specialiteId, Long niveauId, Long anneeAcademiqueId, String status, Long createdBy, @Nullable String title, @Nullable String notes);
    Long insertLigne(Long emploiId, String jour, String heureDebut, String heureFin, Long coursId, Long professeurId, Long salleId, int ordre);

    // existing cleaners
    void deleteSemaineIds(List<Long> idsToDelete);
    List<Long> findOldSemaineIdsToDelete(Long createdBy, int keepLatestN);

    // new helpers used by update:
    void deleteEmploisAndLignes(Long semaineId);
    int updateSemaineDates(Long semaineId, LocalDate dateDebut, LocalDate dateFin, Long modifiedBy);

    // ---- New methods for conflict detection & advisory lock ----
    /**
     * Recherches les conflits (chevauchement) entre la liste de lignes candidates et les lignes
     * déjà présentes pour la semaine identifiée par weekStart.
     *
     * Si excludeSemaineId != null : ignorer les lignes appartenant à cette semaine (utile pour update).
     */
    List<ConflictDto> findConflictsForWeek(LocalDate weekStart, List<RowPayload> candidates, @Nullable Long excludeSemaineId);

    /**
     * Acquire an advisory lock for the given weekStart (to serialize operations on the same week).
     * Implemented using PostgreSQL pg_advisory_xact_lock(hashtext(:key)).
     */
    void acquireAdvisoryLockForWeek(LocalDate weekStart);
}
