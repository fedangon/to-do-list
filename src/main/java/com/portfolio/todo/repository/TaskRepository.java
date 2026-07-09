package com.portfolio.todo.repository;

import com.portfolio.todo.model.Priority;
import com.portfolio.todo.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    long countByCompleted(boolean completed);

    long countByPriority(Priority priority);
}
