package codeqr.code.repository;

import codeqr.code.dto.SessionDtoss;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MySessionRepositoryCustom {
    Page<SessionDtoss> findSessionsByUsername(String username, String search, Pageable pageable);
}
