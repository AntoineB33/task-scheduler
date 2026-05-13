package com.example.task_scheduler.repository;

import com.example.task_scheduler.entity.TaskList;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskListRepository extends JpaRepository<TaskList, UUID> {
}
