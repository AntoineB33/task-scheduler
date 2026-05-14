package com.example.task_scheduler.controller;

import com.example.task_scheduler.entity.Task;
import com.example.task_scheduler.repository.TaskRepository;
import com.example.task_scheduler.security.AuthenticatedUser;
import com.example.task_scheduler.service.InfiniteTaskScheduler;
import com.example.task_scheduler.service.SchedulingService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

	private final SchedulingService schedulingService;
	private final TaskRepository taskRepository;

	public ScheduleController(SchedulingService schedulingService, TaskRepository taskRepository) {
		this.schedulingService = schedulingService;
		this.taskRepository = taskRepository;
	}

	@GetMapping("/{rootListId}")
	public Map<UUID, Double> baseCycle(
			@PathVariable UUID rootListId,
			@AuthenticationPrincipal AuthenticatedUser user) {
		return schedulingService.generateBaseCycleSchedule(rootListId, user.getId());
	}

	@GetMapping("/{rootListId}/global-weights")
	public List<GlobalWeightRow> globalWeights(
			@PathVariable UUID rootListId,
			@AuthenticationPrincipal AuthenticatedUser user) {
		Map<UUID, Double> weights = schedulingService.calculateGlobalWeights(rootListId, user.getId());
		Map<UUID, String> names = taskRepository.findAllById(weights.keySet()).stream()
				.collect(Collectors.toMap(Task::getId, Task::getName));
		return weights.entrySet().stream()
				.sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
				.map(e -> new GlobalWeightRow(e.getKey(), names.getOrDefault(e.getKey(), "?"), e.getValue() * 100.0))
				.toList();
	}

	@GetMapping("/{rootListId}/cycles")
	public List<SchedulerCycleRow> cycles(
			@PathVariable UUID rootListId,
			@RequestParam(name = "limit", defaultValue = "50") int limit,
			@AuthenticationPrincipal AuthenticatedUser user) {
		Map<UUID, Double> weights = schedulingService.calculateGlobalWeights(rootListId, user.getId());
		Map<UUID, String> names = taskRepository.findAllById(weights.keySet()).stream()
				.collect(Collectors.toMap(Task::getId, Task::getName));
		List<InfiniteTaskScheduler.Step> steps = schedulingService.firstSchedulerCycles(rootListId, user.getId(), limit);
		List<SchedulerCycleRow> rows = new ArrayList<>(steps.size());
		for (int i = 0; i < steps.size(); i++) {
			InfiniteTaskScheduler.Step s = steps.get(i);
			rows.add(new SchedulerCycleRow(i + 1, s.taskId(), names.getOrDefault(s.taskId(), "?"), s.duration()));
		}
		return rows;
	}

	public record GlobalWeightRow(UUID taskId, String taskName, double globalPercent) {
	}

	public record SchedulerCycleRow(int cycle, UUID taskId, String taskName, double duration) {
	}
}
