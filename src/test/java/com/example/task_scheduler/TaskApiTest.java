package com.example.task_scheduler;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class TaskApiTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void tasksRequireAuthentication() throws Exception {
		mockMvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/task-lists")).andExpect(status().isUnauthorized());
	}

	@Test
	void taskCrud_roundTrip() throws Exception {
		String username = "u_" + UUID.randomUUID();
		String token = register(username, "secret");

		MvcResult create = mockMvc
				.perform(post("/api/tasks").header("Authorization", "Bearer " + token)
						.contentType(APPLICATION_JSON)
						.content("{\"name\":\"alpha\",\"minDuration\":12.5}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("alpha"))
				.andExpect(jsonPath("$.minDuration").value(12.5))
				.andExpect(jsonPath("$.hasNestedList").value(false))
				.andReturn();
		UUID id = UUID.fromString(JsonPath.read(create.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(id.toString()));

		mockMvc.perform(get("/api/tasks/" + id).header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("alpha"));

		mockMvc.perform(put("/api/tasks/" + id).header("Authorization", "Bearer " + token)
						.contentType(APPLICATION_JSON)
						.content("{\"name\":\"beta\",\"minDuration\":20}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("beta"))
				.andExpect(jsonPath("$.minDuration").value(20.0));

		mockMvc.perform(delete("/api/tasks/" + id).header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/tasks/" + id).header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound());
	}

	@Test
	void taskLists_withDemoUser_returnsCatalog() throws Exception {
		String token = loginDemo();
		mockMvc.perform(get("/api/task-lists").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").exists())
				.andExpect(jsonPath("$[0].name").exists());
	}

	@Test
	void deleteAssignedDemoTask_returnsConflict() throws Exception {
		String token = loginDemo();
		MvcResult list = mockMvc.perform(get("/api/tasks").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andReturn();
		UUID anyId = UUID.fromString(JsonPath.read(list.getResponse().getContentAsString(), "$[0].id"));
		mockMvc.perform(delete("/api/tasks/" + anyId).header("Authorization", "Bearer " + token))
				.andExpect(status().isConflict());
	}

	private String register(String username, String password) throws Exception {
		MvcResult r = mockMvc
				.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
						.content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		return JsonPath.read(r.getResponse().getContentAsString(), "$.token");
	}

	private String loginDemo() throws Exception {
		MvcResult r = mockMvc
				.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
						.content("{\"username\":\"demo\",\"password\":\"demo\"}"))
				.andExpect(status().isOk())
				.andReturn();
		return JsonPath.read(r.getResponse().getContentAsString(), "$.token");
	}
}
