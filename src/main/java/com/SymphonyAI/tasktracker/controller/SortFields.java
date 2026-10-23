package com.SymphonyAI.tasktracker.controller;

import java.util.List;

final class SortFields {

    static final List<String> PROJECT = List.of("id", "name", "createdAt");
    static final List<String> USER = List.of("id", "name", "email", "role");
    static final List<String> TASK = List.of("id", "title", "status", "priority", "dueDate");

    private SortFields() {
    }
}
