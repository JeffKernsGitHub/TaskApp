package info.jeffkerns.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageRequest;

import info.jeffkerns.taskmanager.service.TaskService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class TaskmanagerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TaskService taskService;

	@Test
	void contextLoads() {
	}

	@Test
	void testGetTasks() {
		taskService.getTasks(null, null, PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "priority")));
	}

	@Test
	void testListTasksEndpoint() throws Exception {
		mockMvc.perform(get("/api/v1/tasks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.page").exists())
				.andExpect(jsonPath("$.content").isArray());
	}

}
