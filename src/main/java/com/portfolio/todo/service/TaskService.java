package com.portfolio.todo.service;

import com.portfolio.todo.model.Task;
import com.portfolio.todo.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public Task create(Task task) {
        task.setCompleted(false);
        return taskRepository.save(task);
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }

    public Task update(Long id, Task updated) {
        Task existing = findById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setCompleted(updated.getCompleted());
        existing.setPriority(updated.getPriority());
        return taskRepository.save(existing);
    }

    public void delete(Long id) {
        findById(id);
        taskRepository.deleteById(id);
    }
}
