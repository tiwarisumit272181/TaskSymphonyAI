package com.SymphonyAI.tasktracker.repository;

import com.SymphonyAI.tasktracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}