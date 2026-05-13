package com.example.task_scheduler.service;

import com.example.task_scheduler.entity.Task;
import com.example.task_scheduler.entity.TaskAssignment;
import com.example.task_scheduler.entity.TaskList;
import com.example.task_scheduler.repository.TaskListRepository;
import com.example.task_scheduler.repository.TaskRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	/**
	 * Computes per-task duration for one base cycle so minimum durations are respected when the
	 * pattern repeats. Weights propagate through nested lists using assignment percentages.
	 */
	@Transactional(readOnly = true)
	public Map<UUID, Double> generateBaseCycleSchedule(UUID rootListId) {
		TaskList root = taskListRepository.findById(rootListId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task list not found"));

		Map<UUID, Double> globalWeights = new HashMap<>();
		calculateWeights(root, 1.0, globalWeights);

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

	private static void calculateWeights(TaskList currentList, double incomingWeight, Map<UUID, Double> weights) {
		for (TaskAssignment assignment : currentList.getAssignments()) {
			Task task = assignment.getTask();
			double taskWeightContribution = incomingWeight * (assignment.getPercentage() / 100.0);
			weights.merge(task.getId(), taskWeightContribution, Double::sum);
			if (task.getChildList() != null) {
				calculateWeights(task.getChildList(), taskWeightContribution, weights);
			}
		}
	}
}
