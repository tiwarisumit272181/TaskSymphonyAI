package com.SymphonyAI.tasktracker.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    protected long createProject(String name) throws Exception {
        String body = "{\"name\":\"" + name + "\",\"description\":\"test project\"}";
        MvcResult result = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return idOf(result);
    }

    protected long createUser(String email) throws Exception {
        String body = "{\"name\":\"Test User\",\"email\":\"" + email + "\",\"role\":\"DEVELOPER\"}";
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return idOf(result);
    }

    protected long createTask(String title, long projectId, Long assigneeId) throws Exception {
        String body = "{\"title\":\"" + title + "\",\"projectId\":" + projectId
                + (assigneeId != null ? ",\"assigneeId\":" + assigneeId : "") + "}";
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return idOf(result);
    }

    private long idOf(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
