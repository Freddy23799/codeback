package codeqr.code.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import codeqr.code.model.*;
@Repository
public interface  SexeRepository extends JpaRepository<Sexe, Long> {


    Optional<Sexe> findByName(String name);
}