
package codeqr.code.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import codeqr.code.dto.*;
import codeqr.code.repository.TeacherRepository;

@Service
@RequiredArgsConstructor
public class TeacherReadService {

    private final TeacherRepository teacherRepository;

    /**
     * cursor = lastId seen by client (pass null for first page)
     * limit = page size (client should keep small: 25/50/100)
     */
  
    public PagedResponse<TeacherListDTO> listTeachers(Long cursor, int limit, String q) {
        if (limit <= 0) limit = 50;

        // fetch limit+1 rows from repository (handled in repository method)
        List<TeacherListDTO> rows = teacherRepository.fetchTeachersLight(cursor, limit, q);

        boolean more = false;
        if (rows.size() > limit) {
            more = true;
            rows = rows.subList(0, limit); // remove extra row used to detect "more"
        }

        Long lastCursor = rows.isEmpty() ? null : rows.get(rows.size() - 1).getId();

        // Construire le PagedResponse directement via setters
        PagedResponse<TeacherListDTO> response = new PagedResponse<>();
        response.setContent(rows);
        response.setLastCursor(lastCursor);
        response.setMore(more);

        return response;
    }
}
