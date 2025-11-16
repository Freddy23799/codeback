

package codeqr.code.service.interfaces;

import codeqr.code.dto.CourseDto;
import org.springframework.data.domain.Page;

public interface CourseService {
    Page<CourseDto> search(String q, int page, int size);
}
