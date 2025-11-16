package codeqr.code.repository;

public interface AttendanceCountProjection {
    Long getSessionId();
    Long getPresentCount();
    Long getAbsentCount();
}
