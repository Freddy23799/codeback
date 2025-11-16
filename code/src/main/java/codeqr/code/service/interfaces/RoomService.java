package codeqr.code.service.interfaces;

import codeqr.code.dto.RoomDtos;
import org.springframework.data.domain.Page;

public interface RoomService {
    Page<RoomDtos> search(String q, int page, int size);
}
