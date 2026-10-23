package com.SymphonyAI.tasktracker.repository;

import com.SymphonyAI.tasktracker.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByName(String name);
}
