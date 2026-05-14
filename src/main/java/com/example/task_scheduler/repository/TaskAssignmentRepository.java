package com.example.task_scheduler.repository;

import com.example.task_scheduler.entity.TaskAssignment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, UUID> {

	boolean existsByTask_Id(UUID taskId);
}
