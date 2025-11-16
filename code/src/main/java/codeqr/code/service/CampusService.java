package codeqr.code.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import codeqr.code.repository.CampusRepository;

// import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import codeqr.code.dto.CampusDto;
import codeqr.code.model.Campus;

// --------------- Campus Service -----------------
@Service
@Transactional
public class CampusService {
    private final CampusRepository repo;

    public CampusService(CampusRepository repo) { this.repo = repo; }

  
    @Transactional(readOnly = true)
 @Cacheable(cacheNames = "campuses", key = "'all'")
    public List<CampusDto> listAll() {
        return repo.findAll().stream().map(c -> new CampusDto(c.getId(), c.getName(), c.getAddress())).collect(Collectors.toList());
    }
    public Optional<Campus> findById(Long id) { return repo.findById(id); }
    public Optional<Campus> findByName(String name) { return repo.findByName(name); }
    public Campus save(Campus entity) { return repo.save(entity); }
    public void delete(Long id) { repo.deleteById(id); }
}
