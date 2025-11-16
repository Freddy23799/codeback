package codeqr.code.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import codeqr.code.dto.AbsenceReportDTO;
import codeqr.code.projection.AbsenceReportProjection;
import codeqr.code.repository.AbsenceReportRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AbsenceReportService {

    private final AbsenceReportRepository absenceReportRepository;

    /**
     * Retourne la liste d'AbsenceReportDTO (agrégée côté DB via native query).
     */
    public List<AbsenceReportDTO> getAbsenceReport(Long academicYearId, Long levelId, Long specialtyId,
                                                   LocalDateTime start, LocalDateTime end) {

        List<AbsenceReportProjection> rows = absenceReportRepository
                .findAbsenceReportByFilters(academicYearId, levelId, specialtyId, start, end);

        return rows.stream().map(p ->
                new AbsenceReportDTO(
                        p.getStudentId(),
                        p.getMatricule(),
                        p.getFullName(),
                        p.getSexe(),
                        p.getAbsentHours(),
                        p.getSessionsCount()
                )
        ).collect(Collectors.toList());
    }
}
