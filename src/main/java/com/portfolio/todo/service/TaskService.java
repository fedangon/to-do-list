package com.portfolio.todo.service;

import com.portfolio.todo.dto.TaskRequest;
import com.portfolio.todo.dto.TaskResponse;
import com.portfolio.todo.dto.TaskStatsResponse;
import com.portfolio.todo.exception.ResourceNotFoundException;
import com.portfolio.todo.mapper.TaskMapper;
import com.portfolio.todo.model.Category;
import com.portfolio.todo.model.Priority;
import com.portfolio.todo.model.Task;
import com.portfolio.todo.repository.TaskRepository;
import com.portfolio.todo.repository.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final CategoryService categoryService;

    public TaskResponse create(TaskRequest request) {
        Task task = taskMapper.toEntity(request);
        task.setCompleted(false);
        task.setCategories(resolveCategories(request));
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> findAll(Boolean completed, Priority priority, Long categoryId, Pageable pageable) {
        Specification<Task> spec = Specification
                .where(TaskSpecification.hasCompleted(completed))
                .and(TaskSpecification.hasPriority(priority))
                .and(TaskSpecification.hasCategoryId(categoryId));

        return taskRepository.findAll(spec, pageable).map(taskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return taskMapper.toResponse(findTaskById(id));
    }

    public TaskResponse update(Long id, TaskRequest request) {
        Task task = findTaskById(id);
        taskMapper.updateEntity(request, task);
        task.setCategories(resolveCategories(request));
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public TaskStatsResponse getStats() {
        Map<Priority, Long> byPriority = new LinkedHashMap<>();
        Arrays.stream(Priority.values())
                .forEach(p -> byPriority.put(p, taskRepository.countByPriority(p)));

        return TaskStatsResponse.builder()
                .total(taskRepository.count())
                .completed(taskRepository.countByCompleted(true))
                .pending(taskRepository.countByCompleted(false))
                .byPriority(byPriority)
                .build();
    }

    public void delete(Long id) {
        findTaskById(id);
        taskRepository.deleteById(id);
    }

    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    private Set<Category> resolveCategories(TaskRequest request) {
        if (request.getCategoryIds() == null || request.getCategoryIds().isEmpty()) {
            return Set.of();
        }
        return request.getCategoryIds().stream()
                .map(categoryService::findCategoryById)
                .collect(Collectors.toSet());
    }
}
