package codeqr.code.service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import codeqr.code.dto.RoomCreateDto;
import codeqr.code.dto.RoomDto;
import codeqr.code.model.Campus;
import codeqr.code.model.Room;
import codeqr.code.repository.CampusRepository;
import codeqr.code.repository.RoomRepository;

// --------------- Room Service -----------------
@Service
@Transactional
public class RoomService {
    private final RoomRepository repo;
       @Autowired
 private final CampusRepository campusRepository;
    public RoomService(RoomRepository repo) { this.repo = repo;
    this.campusRepository = null; }

   
    public Optional<Room> findById(Long id) { return repo.findById(id); }
    public List<Room> findByCampusId(Long campusId) { return repo.findByCampusId(campusId); }
    public Optional<Room> findByNameAndCampusId(String name, Long campusId) {
        return repo.findByNameAndCampusId(name, campusId);
    }
    @Transactional(readOnly = true)
       @Cacheable(cacheNames = "rooms", key = "'all'")
    public List<RoomDto> listAll() {
        // findAll annotated with @EntityGraph => campus préchargé
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public RoomDto create(RoomCreateDto in) {
        Campus c = campusRepository.findById(in.getCampusId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campus not found"));
        Room r = new Room();
        r.setName(in.getName());
        r.setCapacity(in.getCapacity());
        r.setCampus(c);
        Room saved = repo.save(r);
        return toDto(saved);
    }

    @Transactional
    public RoomDto update(Long id, RoomCreateDto in) {
        Room r = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        r.setName(in.getName());
        r.setCapacity(in.getCapacity());
        Campus c = campusRepository.findById(in.getCampusId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campus not found"));
        r.setCampus(c);
        Room saved = repo.save(r);
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        repo.deleteById(id);
    }

    private RoomDto toDto(Room r) {
        Long campusId = null;
        String campusName = null;
        if (r.getCampus() != null) {
            campusId = r.getCampus().getId();
            campusName = r.getCampus().getName();
        }
        return new RoomDto(r.getId(), r.getName(), r.getCapacity(), campusId, campusName);
    }
}