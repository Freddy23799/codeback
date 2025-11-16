

package codeqr.code.service.impl;

import codeqr.code.dto.RoomDtos;
import codeqr.code.model.Room;
import codeqr.code.repository.RoomRepository;
import codeqr.code.service.interfaces.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository repo;

    @Autowired
    public RoomServiceImpl(RoomRepository repo) {
        this.repo = repo;
    }


    public Page<RoomDtos> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("id").ascending());
        Page<Room> p;
        if (q == null || q.trim().isEmpty()) {
            p = repo.findAll(pageable);
        } else {
            String term = q.trim();
            p = repo.findByNameContainingIgnoreCase(term, pageable);
        }
        return p.map(r -> new RoomDtos(r.getId(), r.getName()));
    }
}
