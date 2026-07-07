package com.portfolio.todo.dto;

import com.portfolio.todo.model.Priority;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private Boolean completed;
    private Priority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
