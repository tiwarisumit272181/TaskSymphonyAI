package com.SymphonyAI.tasktracker.integration;


import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InfoControllerIntegrationTest extends IntegrationTestSupport {

    @Test
    void info_returnsNameAndVersionFromProperties() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Task Tracker API"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }
}
