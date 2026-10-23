package com.SymphonyAI.tasktracker.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void create_usesDefaultStatusAndPriority() throws Exception {
        long projectId = createProject(unique("task-proj"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Design homepage\",\"projectId\":" + projectId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Design homepage"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.projectId").value(projectId));
    }

    @Test
    void create_withAllFields() throws Exception {
        long projectId = createProject(unique("task-proj"));
        long userId = createUser(unique("assignee") + "@example.com");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Fix bug\",\"status\":\"IN_PROGRESS\",\"priority\":\"HIGH\","
                                + "\"projectId\":" + projectId + ",\"assigneeId\":" + userId
                                + ",\"dueDate\":\"2026-12-31\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.assigneeId").value(userId))
                .andExpect(jsonPath("$.dueDate").value("2026-12-31"));
    }

    @Test
    void create_missingProjectId_returns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"No project\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.projectId").value("projectId is required"));
    }

    @Test
    void create_blankTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"projectId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").value("Title is required"));
    }

    @Test
    void create_projectNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Orphan\",\"projectId\":999999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project not found: 999999"));
    }

    @Test
    void create_assigneeNotFound_returns404() throws Exception {
        long projectId = createProject(unique("task-proj"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"T\",\"projectId\":" + projectId + ",\"assigneeId\":999999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 999999"));
    }

    @Test
    void create_invalidEnum_returns400() throws Exception {
        long projectId = createProject(unique("task-proj"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"T\",\"priority\":\"URGENT\",\"projectId\":" + projectId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Malformed JSON")));
    }

    @Test
    void getById_returnsTask() throws Exception {
        long projectId = createProject(unique("task-proj"));
        long taskId = createTask("Find me", projectId, null);

        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Find me"));
    }

    @Test
    void getById_missing_returns404() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found: 999999"));
    }

    @Test
    void update_changesStatusAndDetails() throws Exception {
        long projectId = createProject(unique("task-proj"));
        long taskId = createTask("Old title", projectId, null);

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New title\",\"status\":\"DONE\",\"priority\":\"LOW\","
                                + "\"projectId\":" + projectId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New title"))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.priority").value("LOW"));
    }

    @Test
    void delete_returns204ThenGetReturns404() throws Exception {
        long projectId = createProject(unique("task-proj"));
        long taskId = createTask("Delete me", projectId, null);

        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_filtersByProjectAndStatus() throws Exception {
        long projectId = createProject(unique("filter"));
        createTask("T1", projectId, null);
        long doneTaskId = createTask("T2", projectId, null);

        mockMvc.perform(put("/api/tasks/{id}", doneTaskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"T2\",\"status\":\"DONE\",\"projectId\":" + projectId + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks").param("projectId", String.valueOf(projectId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/api/tasks")
                        .param("projectId", String.valueOf(projectId))
                        .param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("T2"));
    }

    @Test
    void list_paginationAndSorting() throws Exception {
        long projectId = createProject(unique("paging"));
        createTask("B", projectId, null);
        createTask("A", projectId, null);
        createTask("C", projectId, null);

        mockMvc.perform(get("/api/tasks")
                        .param("projectId", String.valueOf(projectId))
                        .param("sortBy", "title").param("direction", "asc")
                        .param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].title").value("A"))
                .andExpect(jsonPath("$.content[1].title").value("B"));

        mockMvc.perform(get("/api/tasks")
                        .param("projectId", String.valueOf(projectId))
                        .param("sortBy", "title").param("direction", "asc")
                        .param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("C"));
    }

    @Test
    void list_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/api/tasks").param("status", "WRONG"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("status")));
    }

    @Test
    void list_invalidSortBy_returns400() throws Exception {
        mockMvc.perform(get("/api/tasks").param("sortBy", "assignee"))
                .andExpect(status().isBadRequest());
    }
}
