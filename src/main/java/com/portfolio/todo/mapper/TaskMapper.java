package com.portfolio.todo.mapper;

import com.portfolio.todo.dto.TaskRequest;
import com.portfolio.todo.dto.TaskResponse;
import com.portfolio.todo.model.Task;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CategoryMapper.class})
public interface TaskMapper {

    @Mapping(target = "categories", ignore = true)
    Task toEntity(TaskRequest request);

    TaskResponse toResponse(Task task);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "categories", ignore = true)
    void updateEntity(TaskRequest request, @MappingTarget Task task);
}
