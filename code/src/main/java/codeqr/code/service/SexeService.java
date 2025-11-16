package codeqr.code.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codeqr.code.repository.*;

// import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import codeqr.code.model.*;
import org.springframework.cache.annotation.Cacheable;
// --------------- Sexe Service -----------------
@Service
@Transactional
public class SexeService {
    private final SexeRepository repo;

    public SexeService(SexeRepository repo) { this.repo = repo; }
@Cacheable(cacheNames = "sexes", key = "'all'")
    public List<Sexe> findAll() { return repo.findAll(); }
    public Optional<Sexe> findById(Long id) { return repo.findById(id); }
    public Optional<Sexe> findByName(String name) { return repo.findByName(name); }
    public Sexe save(Sexe entity) { return repo.save(entity); }
    public void delete(Long id) { repo.deleteById(id); }
}
