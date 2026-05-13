package com.example.task_scheduler.controller;

import com.example.task_scheduler.service.SchedulingService;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

	private final SchedulingService schedulingService;

	public ScheduleController(SchedulingService schedulingService) {
		this.schedulingService = schedulingService;
	}

	@GetMapping("/{rootListId}")
	public Map<UUID, Double> baseCycle(@PathVariable UUID rootListId) {
		return schedulingService.generateBaseCycleSchedule(rootListId);
	}
}
