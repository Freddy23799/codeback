// file: codeqr/code/repository/TeacherRepositoryCustom.java
package codeqr.code.repository;

import java.util.List;
import codeqr.code.dto.TeacherListDTO;

public interface TeacherRepositoryCustom {
    /**
     * Keyset pagination (cursor = last id)
     * Returns up to (limit+1) rows so caller can know if there are more.
     */
    List<TeacherListDTO> fetchTeachersLight(Long cursorId, int limit, String q);
}
