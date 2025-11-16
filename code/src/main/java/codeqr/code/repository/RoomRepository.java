package codeqr.code.repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;

// import gestion.control.model.Specialty;

import java.util.*;

import java.util.Optional;

import org.springframework.data.domain.*;

import codeqr.code.model.Room;

public  interface RoomRepository  extends JpaRepository<Room, Long> {
List<Room>findByCampusId(Long campusId);
// List<Room>findByCampus_Name(Long campusName);
    Optional<Room> findByNameAndCampusId(String name, Long campusId);
List<Room> findAllByIdIn(Collection<Long> ids);
    Page<Room> findByNameContainingIgnoreCase(String name, Pageable pageable);
     @Override
    @EntityGraph(attributePaths={"campus"})

    List<Room>findAll();
}