package com.example.task_scheduler.config;

import com.example.task_scheduler.entity.AppUser;
import com.example.task_scheduler.entity.Task;
import com.example.task_scheduler.entity.TaskAssignment;
import com.example.task_scheduler.entity.TaskList;
import com.example.task_scheduler.repository.AppUserRepository;
import com.example.task_scheduler.repository.TaskListRepository;
import com.example.task_scheduler.repository.TaskRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the nested task graph mirrored from the Python sample ({@code task_db} / {@code list_db}).
 */
@Component
public class DemoDataLoader implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

	private static final double DEFAULT_MIN_DURATION = 100.0;

	private final TaskRepository taskRepository;
	private final TaskListRepository taskListRepository;
	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;

	public DemoDataLoader(
			TaskRepository taskRepository,
			TaskListRepository taskListRepository,
			AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder) {
		this.taskRepository = taskRepository;
		this.taskListRepository = taskListRepository;
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		AppUser demo = appUserRepository.findByUsername("demo")
				.orElseGet(() -> appUserRepository.save(new AppUser("demo", passwordEncoder.encode("demo"))));
		if (taskListRepository.existsByOwner_Id(demo.getId())) {
			return;
		}

		Map<String, Task> tasks = new LinkedHashMap<>();
		putTask(tasks, demo, "get a job interview", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "succeed in the job interview", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "Java scheduler", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "ANC", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "underwear", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "finance", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "other", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "apply", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "portfolio", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "extend CV", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "english", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "java/springBoot certifications", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "projet Kotlin", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "l'offre", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "entreprise", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "RH", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "technique", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "logique", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "CEO", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "questions", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "soft skills", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "coder", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "LCM", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "Python", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "CV", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "Java", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "Spring Boot", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "négociation", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "communiquer régulièrement", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "infinite nested list", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "eye care scheduler", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "food planner", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "media sorting spreadsheet", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "english listen", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "english speak", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "english write", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "Gemini discussion", DEFAULT_MIN_DURATION);
		putTask(tasks, demo, "vocabulary", DEFAULT_MIN_DURATION);

		Map<String, TaskList> lists = new LinkedHashMap<>();

		lists.put("root", list("root", true, 0.75, demo));
		lists.put("get a job interview", list("get a job interview", true, 0.75, demo));
		lists.put("extend CV", list("extend CV", true, 0.5, demo));
		lists.put("succeed in the job interview", list("succeed in the job interview", false, null, demo));
		lists.put("RH", list("RH", true, null, demo));
		lists.put("technique", list("technique", true, 0.8, demo));
		lists.put("coder", list("coder", true, null, demo));
		lists.put("CEO", list("CEO", true, null, demo));
		lists.put("soft skills", list("soft skills", true, null, demo));
		lists.put("projet Kotlin", list("projet Kotlin", true, null, demo));
		lists.put("english", list("english", true, null, demo));
		lists.put("english listen", list("english listen", true, null, demo));

		for (TaskList l : lists.values()) {
			taskListRepository.save(l);
		}

		tasks.get("get a job interview").setChildList(lists.get("get a job interview"));
		tasks.get("succeed in the job interview").setChildList(lists.get("succeed in the job interview"));
		tasks.get("extend CV").setChildList(lists.get("extend CV"));
		tasks.get("english").setChildList(lists.get("english"));
		tasks.get("projet Kotlin").setChildList(lists.get("projet Kotlin"));
		tasks.get("RH").setChildList(lists.get("RH"));
		tasks.get("technique").setChildList(lists.get("technique"));
		tasks.get("coder").setChildList(lists.get("coder"));
		tasks.get("CEO").setChildList(lists.get("CEO"));
		tasks.get("soft skills").setChildList(lists.get("soft skills"));
		tasks.get("english listen").setChildList(lists.get("english listen"));
		for (Task t : tasks.values()) {
			taskRepository.save(t);
		}

		addImplicit(lists.get("root"), tasks,
				"get a job interview", "succeed in the job interview", "Java scheduler", "ANC", "underwear", "finance",
				"other");
		addImplicit(lists.get("get a job interview"), tasks, "apply", "portfolio", "extend CV", "other");
		addImplicit(lists.get("extend CV"), tasks, "java/springBoot certifications", "projet Kotlin", "english",
				"other");

		TaskList succeed = lists.get("succeed in the job interview");
		succeed.addAssignment(new TaskAssignment(tasks.get("l'offre"), 16.666));
		succeed.addAssignment(new TaskAssignment(tasks.get("entreprise"), 16.666));
		succeed.addAssignment(new TaskAssignment(tasks.get("RH"), 16.666));
		succeed.addAssignment(new TaskAssignment(tasks.get("technique"), 16.666));
		succeed.addAssignment(new TaskAssignment(tasks.get("logique"), 16.666));
		succeed.addAssignment(new TaskAssignment(tasks.get("CEO"), 16.67));

		addImplicit(lists.get("RH"), tasks, "questions", "soft skills");
		addImplicit(lists.get("technique"), tasks, "coder", "LCM", "Python", "CV");
		addImplicit(lists.get("coder"), tasks, "Java", "Spring Boot");
		addImplicit(lists.get("CEO"), tasks, "négociation", "soft skills");
		addImplicit(lists.get("soft skills"), tasks, "communiquer régulièrement");
		addImplicit(lists.get("projet Kotlin"), tasks, "infinite nested list", "eye care scheduler", "food planner",
				"media sorting spreadsheet");
		addImplicit(lists.get("english"), tasks, "english listen", "english speak", "english write");
		addImplicit(lists.get("english listen"), tasks, "Gemini discussion", "vocabulary");

		for (TaskList l : lists.values()) {
			taskListRepository.save(l);
		}

		TaskList root = lists.get("root");
		log.info("Demo data for user 'demo' (password: demo). Root list id: {} — use Bearer token from POST /api/auth/login", root.getId());
	}

	private static TaskList list(String name, boolean implicit, Double decreaseFactor, AppUser owner) {
		TaskList l = new TaskList(name, owner);
		l.setImplicitWeights(implicit);
		l.setDecreaseFactor(decreaseFactor);
		return l;
	}

	private void putTask(Map<String, Task> tasks, AppUser owner, String name, double minDuration) {
		tasks.put(name, taskRepository.save(new Task(name, minDuration, owner)));
	}

	private static void addImplicit(TaskList list, Map<String, Task> tasks, String... taskNames) {
		for (String n : taskNames) {
			list.addAssignment(new TaskAssignment(tasks.get(n), 0.0));
		}
	}
}
