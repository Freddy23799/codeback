// package codeqr.code.projection;
package codeqr.code.projection;

public interface AbsenceReportProjection {
    Long getStudentId();
    String getMatricule();
    String getFullName();
    String getSexe();
    Double getAbsentHours();    // heures (décimal)
    Integer getSessionsCount();
}
