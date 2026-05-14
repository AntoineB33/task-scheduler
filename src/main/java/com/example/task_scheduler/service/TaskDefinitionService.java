package com.example.task_scheduler.service;

import com.example.task_scheduler.entity.AppUser;
import com.example.task_scheduler.entity.Task;
import com.example.task_scheduler.repository.AppUserRepository;
import com.example.task_scheduler.repository.TaskAssignmentRepository;
import com.example.task_scheduler.repository.TaskRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskDefinitionService {

	private static final int MAX_NAME_LENGTH = 512;

	private final TaskRepository taskRepository;
	private final TaskAssignmentRepository taskAssignmentRepository;
	private final AppUserRepository appUserRepository;

	public TaskDefinitionService(
			TaskRepository taskRepository,
			TaskAssignmentRepository taskAssignmentRepository,
			AppUserRepository appUserRepository) {
		this.taskRepository = taskRepository;
		this.taskAssignmentRepository = taskAssignmentRepository;
		this.appUserRepository = appUserRepository;
	}

	@Transactional(readOnly = true)
	public List<Task> listOwnedTasks(UUID ownerId) {
		return taskRepository.findByOwner_IdOrderByNameAsc(ownerId);
	}

	@Transactional(readOnly = true)
	public Task getOwnedTask(UUID taskId, UUID ownerId) {
		return taskRepository.findByIdAndOwner_Id(taskId, ownerId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
	}

	@Transactional
	public Task createTask(UUID ownerId, String name, double minDuration) {
		String trimmed = validateName(name);
		validateMinDuration(minDuration);
		AppUser owner = appUserRepository.getReferenceById(ownerId);
		return taskRepository.save(new Task(trimmed, minDuration, owner));
	}

	@Transactional
	public Task updateTask(UUID taskId, UUID ownerId, String name, double minDuration) {
		String trimmed = validateName(name);
		validateMinDuration(minDuration);
		Task task = getOwnedTask(taskId, ownerId);
		task.setName(trimmed);
		task.setMinDuration(minDuration);
		return taskRepository.save(task);
	}

	@Transactional
	public void deleteTask(UUID taskId, UUID ownerId) {
		Task task = getOwnedTask(taskId, ownerId);
		if (task.getChildList() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Task has a nested list; remove or restructure the tree before deleting this task");
		}
		if (taskAssignmentRepository.existsByTask_Id(taskId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Task is still assigned to a list; remove those assignments before deleting");
		}
		taskRepository.delete(task);
	}

	private static String validateName(String name) {
		if (name == null || name.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
		}
		String trimmed = name.trim();
		if (trimmed.length() > MAX_NAME_LENGTH) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is too long");
		}
		return trimmed;
	}

	private static void validateMinDuration(double minDuration) {
		if (!Double.isFinite(minDuration) || minDuration <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minDuration must be a finite number > 0");
		}
	}
}
