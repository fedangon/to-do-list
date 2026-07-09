package com.portfolio.todo.dto;

import com.portfolio.todo.model.Priority;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class TaskStatsResponse {

    private long total;
    private long completed;
    private long pending;
    private Map<Priority, Long> byPriority;
}
