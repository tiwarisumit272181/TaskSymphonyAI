package com.SymphonyAI.tasktracker.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends IntegrationTestSupport {

    private String email() {
        return unique("user") + "@example.com";
    }

    @Test
    void create_returns201AndBody() throws Exception {
        String email = email();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"" + email + "\",\"role\":\"QA\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("QA"));
    }

    @Test
    void create_invalidEmail_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").value("Email must be valid"));
    }

    @Test
    void create_missingName_returns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").value("Name is required"));
    }

    @Test
    void create_duplicateEmail_returns409() throws Exception {
        String email = email();
        createUser(email);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Other\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_returnsUser() throws Exception {
        String email = email();
        long id = createUser(email);

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void getById_missing_returns404() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 999999"));
    }

    @Test
    void update_changesUser() throws Exception {
        long id = createUser(email());
        String newEmail = email();

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renamed\",\"email\":\"" + newEmail + "\",\"role\":\"MANAGER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.email").value(newEmail))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    void delete_returns204ThenGetReturns404() throws Exception {
        long id = createUser(email());

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_userWithTasks_returns409UntilTaskRemoved() throws Exception {
        long userId = createUser(email());
        long projectId = createProject(unique("userdel"));
        long taskId = createTask("Assigned task", projectId, userId);

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void list_paginationAndSorting() throws Exception {
        createUser(email());
        createUser(email());
        createUser(email());

        mockMvc.perform(get("/api/users")
                        .param("page", "0").param("size", "2")
                        .param("sortBy", "email").param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    void list_invalidSortBy_returns400() throws Exception {
        mockMvc.perform(get("/api/users").param("sortBy", "password"))
                .andExpect(status().isBadRequest());
    }
}
