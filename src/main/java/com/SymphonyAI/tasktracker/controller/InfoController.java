package com.SymphonyAI.tasktracker.controller;

import com.SymphonyAI.tasktracker.config.AppProperties;
import com.SymphonyAI.tasktracker.dto.InfoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/info")
public class InfoController {

    private final AppProperties appProperties;

    public InfoController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping
    public InfoResponse info() {
        return new InfoResponse(appProperties.getName(), appProperties.getVersion());
    }
}
