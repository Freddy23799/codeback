package codeqr.code.dto;

import codeqr.code.dto.ConflictDto;
import java.util.List;

public class TimetableConflictException extends RuntimeException {
    private final List<ConflictDto> conflicts;

    public TimetableConflictException(List<ConflictDto> conflicts) {
        super(buildMessage(conflicts));
        this.conflicts = conflicts;
    }

    private static String buildMessage(List<ConflictDto> conflicts) {
        StringBuilder sb = new StringBuilder("Conflits détectés : ");
        for (ConflictDto c : conflicts) {
            sb.append("\n - ").append(c.getMessage());
        }
        return sb.toString();
    }

    public List<ConflictDto> getConflicts() {
        return conflicts;
    }
}
