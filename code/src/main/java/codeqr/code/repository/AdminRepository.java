package codeqr.code.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import codeqr.code.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByEmail(String email); 
  Optional<Admin> findByUserUsername(String username);
    Optional<Admin> findByUserId(Long id);
       Optional<Admin> findById(Long id);





       
     @Query("SELECT a FROM Admin a " +
           "JOIN FETCH a.sexe sx " +
           "JOIN FETCH a.user u " +
           "WHERE a.user.id = :userId")
    Optional<Admin> findByUserIdWithSexe(@Param("userId") Long userId);




 
}