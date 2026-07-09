package com.portfolio.todo.service;

import com.portfolio.todo.dto.TaskRequest;
import com.portfolio.todo.dto.TaskResponse;
import com.portfolio.todo.exception.ResourceNotFoundException;
import com.portfolio.todo.mapper.TaskMapper;
import com.portfolio.todo.model.Category;
import com.portfolio.todo.model.Task;
import com.portfolio.todo.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public List<TaskResponse> findAll(Long categoryId) {
        List<Task> tasks = categoryId != null
                ? taskRepository.findByCategoriesId(categoryId)
                : taskRepository.findAll();
        return tasks.stream().map(taskMapper::toResponse).toList();
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
