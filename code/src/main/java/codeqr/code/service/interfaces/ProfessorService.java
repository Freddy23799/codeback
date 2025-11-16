package codeqr.code.service.interfaces;

import codeqr.code.dto.ProfessorDto;
import org.springframework.data.domain.Page;

public interface ProfessorService {
    Page<ProfessorDto> search(String q, int page, int size);
}
