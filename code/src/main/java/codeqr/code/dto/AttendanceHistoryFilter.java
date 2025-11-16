package codeqr.code.dto;

import java.time.LocalDate;

public class AttendanceHistoryFilter {
    public String period;   // "this_week", "this_month", "custom"
    public LocalDate from;
    public LocalDate to;
    public Long courseId;
    public Long campusId;
    public Long roomId;
    public String status;   // "PRESENT", "ABSENT", etc.
    public String q;        // recherche texte (cours, prof, campus, salle)
}
