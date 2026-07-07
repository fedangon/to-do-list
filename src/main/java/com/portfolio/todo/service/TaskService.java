package com.portfolio.todo.service;

import com.portfolio.todo.dto.TaskRequest;
import com.portfolio.todo.dto.TaskResponse;
import com.portfolio.todo.exception.ResourceNotFoundException;
import com.portfolio.todo.mapper.TaskMapper;
import com.portfolio.todo.model.Task;
import com.portfolio.todo.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskResponse create(TaskRequest request) {
        Task task = taskMapper.toEntity(request);
        task.setCompleted(false);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    public List<TaskResponse> findAll() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    public TaskResponse findById(Long id) {
        return taskMapper.toResponse(findTaskById(id));
    }

    public TaskResponse update(Long id, TaskRequest request) {
        Task task = findTaskById(id);
        taskMapper.updateEntity(request, task);
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
}
