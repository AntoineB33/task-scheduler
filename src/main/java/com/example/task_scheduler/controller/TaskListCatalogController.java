package com.example.task_scheduler.controller;

import com.example.task_scheduler.entity.TaskList;
import com.example.task_scheduler.repository.TaskListRepository;
import com.example.task_scheduler.security.AuthenticatedUser;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only catalog of the signed-in user's lists (ids for schedule APIs, names for UI).
 */
@RestController
@RequestMapping("/api/task-lists")
public class TaskListCatalogController {

	private final TaskListRepository taskListRepository;

	public TaskListCatalogController(TaskListRepository taskListRepository) {
		this.taskListRepository = taskListRepository;
	}

	@GetMapping
	public List<TaskListSummary> list(@AuthenticationPrincipal AuthenticatedUser user) {
		return taskListRepository.findAllByOwner_IdOrderByNameAsc(user.getId()).stream()
				.map(TaskListSummary::fromEntity)
				.toList();
	}

	public record TaskListSummary(
			UUID id,
			String name,
			boolean implicitWeights,
			Double decreaseFactor) {
		static TaskListSummary fromEntity(TaskList list) {
			return new TaskListSummary(
					list.getId(), list.getName(), list.isImplicitWeights(), list.getDecreaseFactor());
		}
	}
}
