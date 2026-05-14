package com.example.task_scheduler.controller;

import com.example.task_scheduler.entity.Task;
import com.example.task_scheduler.security.AuthenticatedUser;
import com.example.task_scheduler.service.TaskDefinitionService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

	private final TaskDefinitionService taskDefinitionService;

	public TaskController(TaskDefinitionService taskDefinitionService) {
		this.taskDefinitionService = taskDefinitionService;
	}

	@GetMapping
	public List<TaskResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
		return taskDefinitionService.listOwnedTasks(user.getId()).stream().map(TaskResponse::fromEntity).toList();
	}

	@GetMapping("/{taskId}")
	public TaskResponse get(@PathVariable UUID taskId, @AuthenticationPrincipal AuthenticatedUser user) {
		return TaskResponse.fromEntity(taskDefinitionService.getOwnedTask(taskId, user.getId()));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TaskResponse create(@RequestBody UpsertTaskRequest body, @AuthenticationPrincipal AuthenticatedUser user) {
		Task created = taskDefinitionService.createTask(user.getId(), body.name(), body.minDuration());
		return TaskResponse.fromEntity(created);
	}

	@PutMapping("/{taskId}")
	public TaskResponse replace(
			@PathVariable UUID taskId,
			@RequestBody UpsertTaskRequest body,
			@AuthenticationPrincipal AuthenticatedUser user) {
		Task updated = taskDefinitionService.updateTask(taskId, user.getId(), body.name(), body.minDuration());
		return TaskResponse.fromEntity(updated);
	}

	@DeleteMapping("/{taskId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID taskId, @AuthenticationPrincipal AuthenticatedUser user) {
		taskDefinitionService.deleteTask(taskId, user.getId());
	}

	public record UpsertTaskRequest(String name, double minDuration) {
	}

	public record TaskResponse(UUID id, String name, double minDuration, boolean hasNestedList) {
		static TaskResponse fromEntity(Task task) {
			return new TaskResponse(task.getId(), task.getName(), task.getMinDuration(), task.getChildList() != null);
		}
	}
}
