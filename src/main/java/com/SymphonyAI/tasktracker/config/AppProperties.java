package com.SymphonyAI.tasktracker.config;

import com.SymphonyAI.tasktracker.entity.Priority;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AppProperties {

    private final String name;
    private final String version;
    private final Priority defaultPriority;
    private final int defaultPageSize;

    public AppProperties(@Value("${app.name}") String name,
                         @Value("${app.version}") String version,
                         @Value("${task.default-priority}") Priority defaultPriority,
                         @Value("${pagination.default-page-size}") int defaultPageSize) {
        this.name = name;
        this.version = version;
        this.defaultPriority = defaultPriority;
        this.defaultPageSize = defaultPageSize;
    }

}
