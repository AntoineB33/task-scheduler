package com.example.task_scheduler.repository;

import com.example.task_scheduler.entity.TaskList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskListRepository extends JpaRepository<TaskList, UUID> {

	boolean existsByOwner_Id(UUID ownerId);

	List<TaskList> findAllByOwner_IdOrderByNameAsc(UUID ownerId);

	Optional<TaskList> findByIdAndOwner_Id(UUID id, UUID ownerId);
}
