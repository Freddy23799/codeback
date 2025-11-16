package codeqr.code.service;

import codeqr.code.dto.*;
import codeqr.code.model.Level;
import codeqr.code.repository.LevelRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
@Service
@RequiredArgsConstructor
public class LevelService {

    private final LevelRepository repository;

   @Transactional(readOnly = true)
@Cacheable(cacheNames = "levels", key = "'all'")
    public List<LevelDto> listAll() {
        return repository.findAll().stream().map(l -> new LevelDto(l.getId(), l.getName())).collect(Collectors.toList());
    }
    public Optional<Level> findById(Long id) {
        return repository.findById(id);
    }

    public Level save(Level level) {
        return repository.save(level);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
