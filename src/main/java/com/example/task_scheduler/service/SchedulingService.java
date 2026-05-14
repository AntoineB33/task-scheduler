package com.example.task_scheduler.service;

import com.example.task_scheduler.entity.Task;
import com.example.task_scheduler.entity.TaskAssignment;
import com.example.task_scheduler.entity.TaskList;
import com.example.task_scheduler.repository.TaskListRepository;
import com.example.task_scheduler.repository.TaskRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SchedulingService {

	private final TaskListRepository taskListRepository;
	private final TaskRepository taskRepository;

	public SchedulingService(TaskListRepository taskListRepository, TaskRepository taskRepository) {
		this.taskListRepository = taskListRepository;
		this.taskRepository = taskRepository;
	}

	@Transactional(readOnly = true)
	public Map<UUID, Double> calculateGlobalWeights(UUID rootListId) {
		TaskList root = loadTree(rootListId);
		Map<UUID, Double> globalWeights = new HashMap<>();
		Set<UUID> visiting = new HashSet<>();
		traverse(root, 1.0, globalWeights, visiting);
		return globalWeights;
	}

	/**
	 * One full base cycle: per-task wall time so that each leaf task receives at least its
	 * {@link Task#getMinDuration()} in proportion to its global weight (same scaling idea as the
	 * Python sample's derived cycle length).
	 */
	@Transactional(readOnly = true)
	public Map<UUID, Double> generateBaseCycleSchedule(UUID rootListId) {
		Map<UUID, Double> globalWeights = calculateGlobalWeights(rootListId);

		List<Task> allTasks = taskRepository.findAll();
		double cycleDuration = 0.0;
		for (Task task : allTasks) {
			double weight = globalWeights.getOrDefault(task.getId(), 0.0);
			if (weight > 0) {
				double requiredCycle = task.getMinDuration() / weight;
				if (requiredCycle > cycleDuration) {
					cycleDuration = requiredCycle;
				}
			}
		}

		Map<UUID, Double> schedule = new HashMap<>();
		for (Task task : allTasks) {
			double weight = globalWeights.getOrDefault(task.getId(), 0.0);
			if (weight > 0) {
				schedule.put(task.getId(), cycleDuration * weight);
			}
		}
		return schedule;
	}

	@Transactional(readOnly = true)
	public List<InfiniteTaskScheduler.Step> firstSchedulerCycles(UUID rootListId, int limit) {
		if (limit < 1 || limit > 10_000) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be between 1 and 10000");
		}
		Map<UUID, Double> weights = calculateGlobalWeights(rootListId);
		Map<UUID, Double> durations = new HashMap<>();
		for (Task t : taskRepository.findAll()) {
			durations.put(t.getId(), t.getMinDuration());
		}
		InfiniteTaskScheduler scheduler = new InfiniteTaskScheduler(weights, durations);
		List<InfiniteTaskScheduler.Step> out = new ArrayList<>(limit);
		for (int i = 0; i < limit; i++) {
			out.add(scheduler.next());
		}
		return out;
	}

	private TaskList loadTree(UUID rootListId) {
		TaskList root = taskListRepository.findById(rootListId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task list not found"));
		initialiseTree(root);
		return root;
	}

	private static void initialiseTree(TaskList list) {
		list.getAssignments().size();
		for (TaskAssignment assignment : list.getAssignments()) {
			Task task = assignment.getTask();
			task.getName();
			if (task.getChildList() != null) {
				initialiseTree(task.getChildList());
			}
		}
	}

	private static void traverse(TaskList currentList, double currentWeight, Map<UUID, Double> globalWeights,
			Set<UUID> visiting) {
		UUID listId = currentList.getId();
		if (visiting.contains(listId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Circular dependency detected at list " + currentList.getName());
		}
		visiting.add(listId);

		List<ParsedEntry> parsed = parseListEntries(currentList);
		for (ParsedEntry entry : parsed) {
			Task task = entry.task();
			double taskBranchWeight = currentWeight * (entry.percentage() / 100.0);
			if (task.getChildList() != null) {
				traverse(task.getChildList(), taskBranchWeight, globalWeights, visiting);
			}
			else {
				globalWeights.compute(task.getId(), (id, existing) ->
						(existing == null ? 0.0 : existing) + taskBranchWeight);
			}
		}

		visiting.remove(listId);
	}

	private static List<ParsedEntry> parseListEntries(TaskList list) {
		List<TaskAssignment> ordered = new ArrayList<>(list.getAssignments());

		if (ordered.isEmpty()) {
			return List.of();
		}

		if (!list.isImplicitWeights()) {
			List<ParsedEntry> out = new ArrayList<>(ordered.size());
			for (TaskAssignment a : ordered) {
				out.add(new ParsedEntry(a.getTask(), a.getPercentage()));
			}
			return out;
		}

		double factor = list.getDecreaseFactor() != null ? list.getDecreaseFactor() : 1.0;
		int n = ordered.size();
		if (factor == 1.0) {
			double each = 100.0 / n;
			List<ParsedEntry> out = new ArrayList<>(n);
			for (TaskAssignment a : ordered) {
				out.add(new ParsedEntry(a.getTask(), each));
			}
			return out;
		}

		double denom = (1.0 - Math.pow(factor, n)) / (1.0 - factor);
		List<ParsedEntry> out = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			double fraction = Math.pow(factor, i) / denom;
			out.add(new ParsedEntry(ordered.get(i).getTask(), fraction * 100.0));
		}
		return out;
	}

	private record ParsedEntry(Task task, double percentage) {
	}
}
