package com.portfolio.todo.mapper;

import com.portfolio.todo.dto.TaskRequest;
import com.portfolio.todo.dto.TaskResponse;
import com.portfolio.todo.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    Task toEntity(TaskRequest request);

    TaskResponse toResponse(Task task);

    void updateEntity(TaskRequest request, @MappingTarget Task task);
}
