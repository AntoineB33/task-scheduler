package com.example.task_scheduler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.task_scheduler.repository.TaskListRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ScheduleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TaskListRepository taskListRepository;

	@Test
	void scheduleEndpointReturnsOk() throws Exception {
		var rootId = taskListRepository.findAll().stream()
				.filter(list -> "root".equals(list.getName()))
				.findFirst()
				.orElseThrow()
				.getId();
		mockMvc.perform(get("/api/schedule/{id}", rootId)).andExpect(status().isOk());
		mockMvc.perform(get("/api/schedule/{id}/global-weights", rootId)).andExpect(status().isOk());
		mockMvc.perform(get("/api/schedule/{id}/cycles", rootId).param("limit", "5")).andExpect(status().isOk());
	}
}
