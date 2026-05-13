package com.example.task_scheduler.config;

import com.example.task_scheduler.entity.Task;
import com.example.task_scheduler.entity.TaskAssignment;
import com.example.task_scheduler.entity.TaskList;
import com.example.task_scheduler.repository.TaskListRepository;
import com.example.task_scheduler.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a small nested list so the app is usable immediately after start.
 */
@Component
public class DemoDataLoader implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

	private final TaskRepository taskRepository;
	private final TaskListRepository taskListRepository;

	public DemoDataLoader(TaskRepository taskRepository, TaskListRepository taskListRepository) {
		this.taskRepository = taskRepository;
		this.taskListRepository = taskListRepository;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (taskListRepository.count() > 0) {
			return;
		}

		Task taskA = taskRepository.save(new Task("A", 10.0));
		Task taskB = taskRepository.save(new Task("B", 20.0));
		Task taskX = taskRepository.save(new Task("X", 5.0));
		Task taskY = taskRepository.save(new Task("Y", 15.0));

		TaskList subList = new TaskList("nested-under-A");
		subList.addAssignment(new TaskAssignment(taskX, 40.0));
		subList.addAssignment(new TaskAssignment(taskY, 60.0));
		subList = taskListRepository.save(subList);

		taskA.setChildList(subList);
		taskA = taskRepository.save(taskA);

		TaskList root = new TaskList("root");
		root.addAssignment(new TaskAssignment(taskA, 50.0));
		root.addAssignment(new TaskAssignment(taskB, 50.0));
		root = taskListRepository.save(root);

		log.info("Demo data loaded. Root task list id: {} — GET /api/schedule/{}", root.getId(), root.getId());
	}
}
