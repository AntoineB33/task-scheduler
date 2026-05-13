package com.example.task_scheduler.repository;

import com.example.task_scheduler.entity.Task;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {
}
