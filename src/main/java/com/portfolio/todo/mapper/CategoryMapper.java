package com.portfolio.todo.mapper;

import com.portfolio.todo.dto.CategoryRequest;
import com.portfolio.todo.dto.CategoryResponse;
import com.portfolio.todo.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category category);
}
