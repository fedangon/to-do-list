package com.portfolio.todo.service;

import com.portfolio.todo.dto.CategoryRequest;
import com.portfolio.todo.dto.CategoryResponse;
import com.portfolio.todo.exception.ResourceNotFoundException;
import com.portfolio.todo.mapper.CategoryMapper;
import com.portfolio.todo.model.Category;
import com.portfolio.todo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Category already exists: " + request.getName());
        }
        return categoryMapper.toResponse(categoryRepository.save(categoryMapper.toEntity(request)));
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse findById(Long id) {
        return categoryMapper.toResponse(findCategoryById(id));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findCategoryById(id);
        category.setName(request.getName());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    public void delete(Long id) {
        findCategoryById(id);
        categoryRepository.deleteById(id);
    }

    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}
