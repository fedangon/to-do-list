package com.portfolio.todo.repository;

import com.portfolio.todo.model.Category;
import com.portfolio.todo.model.Priority;
import com.portfolio.todo.model.Task;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class TaskSpecification {

    public static Specification<Task> hasCompleted(Boolean completed) {
        return (root, query, cb) ->
                completed == null ? null : cb.equal(root.get("completed"), completed);
    }

    public static Specification<Task> hasPriority(Priority priority) {
        return (root, query, cb) ->
                priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return null;
            query.distinct(true);
            Join<Task, Category> join = root.join("categories", JoinType.INNER);
            return cb.equal(join.get("id"), categoryId);
        };
    }
}
