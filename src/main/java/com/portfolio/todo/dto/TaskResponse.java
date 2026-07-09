package com.portfolio.todo.dto;

import com.portfolio.todo.model.Priority;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private Boolean completed;
    private Priority priority;
    private Set<CategoryResponse> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
