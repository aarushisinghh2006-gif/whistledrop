package com.whistledrop.repository;

import com.whistledrop.entity.Moderator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModeratorRepository extends JpaRepository<Moderator, Long> {

    Optional<Moderator> findByUsername(String username);

    boolean existsByUsername(String username);
}
