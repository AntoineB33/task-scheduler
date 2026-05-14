package com.example.task_scheduler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.task_scheduler.repository.TaskListRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ScheduleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TaskListRepository taskListRepository;

	@Test
	void scheduleRequiresAuthentication() throws Exception {
		var rootId = taskListRepository.findAll().stream()
				.filter(list -> "root".equals(list.getName()))
				.findFirst()
				.orElseThrow()
				.getId();
		mockMvc.perform(get("/api/schedule/{id}", rootId)).andExpect(status().isUnauthorized());
	}

	@Test
	void scheduleWithTokenReturnsOk() throws Exception {
		String token = loginDemo();
		var rootId = taskListRepository.findAll().stream()
				.filter(list -> "root".equals(list.getName()))
				.findFirst()
				.orElseThrow()
				.getId();
		mockMvc.perform(get("/api/schedule/{id}", rootId).header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/schedule/{id}/global-weights", rootId).header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/schedule/{id}/cycles", rootId).param("limit", "5")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	private String loginDemo() throws Exception {
		MvcResult r = mockMvc.perform(post("/api/auth/login")
						.contentType(APPLICATION_JSON)
						.content("{\"username\":\"demo\",\"password\":\"demo\"}"))
				.andExpect(status().isOk())
				.andReturn();
		return JsonPath.read(r.getResponse().getContentAsString(), "$.token");
	}
}
