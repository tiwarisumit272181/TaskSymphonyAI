package com.SymphonyAI.tasktracker.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void create_returns201AndBody() throws Exception {
        String name = unique("proj");

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"description\":\"Q4\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value("Q4"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void create_blankName_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.name").value("Name is required"));
    }

    @Test
    void create_duplicateName_returns409() throws Exception {
        String name = unique("dup");
        createProject(name);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void getById_returnsProject() throws Exception {
        String name = unique("get");
        long id = createProject(name);

        mockMvc.perform(get("/api/projects/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    void getById_missing_returns404() throws Exception {
        mockMvc.perform(get("/api/projects/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Project not found: 999999"));
    }

    @Test
    void getById_invalidId_returns400() throws Exception {
        mockMvc.perform(get("/api/projects/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_changesProject() throws Exception {
        long id = createProject(unique("upd"));
        String newName = unique("renamed");

        mockMvc.perform(put("/api/projects/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + newName + "\",\"description\":\"changed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(newName))
                .andExpect(jsonPath("$.description").value("changed"));
    }

    @Test
    void delete_returns204ThenGetReturns404() throws Exception {
        long id = createProject(unique("del"));

        mockMvc.perform(delete("/api/projects/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/projects/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_alsoDeletesTasksOfProject() throws Exception {
        long projectId = createProject(unique("cascade"));
        long taskId = createTask("Will be deleted", projectId, null);

        mockMvc.perform(delete("/api/projects/{id}", projectId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_usesDefaultPageSizeFromProperties() throws Exception {
        createProject(unique("list"));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    void list_paginationLimitsContent() throws Exception {
        createProject(unique("page"));
        createProject(unique("page"));
        createProject(unique("page"));

        mockMvc.perform(get("/api/projects").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(3)));
    }

    @Test
    void list_sortByNameDescending_returns200() throws Exception {
        createProject(unique("sort"));

        mockMvc.perform(get("/api/projects").param("sortBy", "name").param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void list_invalidSortBy_returns400() throws Exception {
        mockMvc.perform(get("/api/projects").param("sortBy", "nonsense"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void list_invalidSize_returns400() throws Exception {
        mockMvc.perform(get("/api/projects").param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void projectTasks_returnsOnlyThatProjectsTasks_sorted() throws Exception {
        long projectId = createProject(unique("tasks"));
        long otherProjectId = createProject(unique("other"));
        createTask("B", projectId, null);
        createTask("A", projectId, null);
        createTask("C", projectId, null);
        createTask("Elsewhere", otherProjectId, null);

        mockMvc.perform(get("/api/projects/{id}/tasks", projectId)
                        .param("sortBy", "title").param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].title").value("A"))
                .andExpect(jsonPath("$.content[2].title").value("C"));

        mockMvc.perform(get("/api/projects/{id}/tasks", projectId)
                        .param("sortBy", "title").param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("C"));
    }

    @Test
    void projectTasks_missingProject_returns404() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/tasks", 999999))
                .andExpect(status().isNotFound());
    }
}