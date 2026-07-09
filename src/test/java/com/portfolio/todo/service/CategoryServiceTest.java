package com.portfolio.todo.service;

import com.portfolio.todo.dto.CategoryRequest;
import com.portfolio.todo.dto.CategoryResponse;
import com.portfolio.todo.exception.ResourceNotFoundException;
import com.portfolio.todo.mapper.CategoryMapper;
import com.portfolio.todo.model.Category;
import com.portfolio.todo.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryRequest request;
    private CategoryResponse response;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Trabalho")
                .build();

        request = new CategoryRequest();
        request.setName("Trabalho");

        response = new CategoryResponse();
        response.setId(1L);
        response.setName("Trabalho");
    }

    @Test
    void create_deveSalvarCategoriaComSucesso() {
        when(categoryRepository.existsByName("Trabalho")).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Trabalho");
        verify(categoryRepository).save(category);
    }

    @Test
    void create_deveLancarExcecaoParaNomeDuplicado() {
        when(categoryRepository.existsByName("Trabalho")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trabalho");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void findAll_deveRetornarListaDeCategorias() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        List<CategoryResponse> result = categoryService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Trabalho");
    }

    @Test
    void findById_deveRetornarCategoriaQuandoEncontrada() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = categoryService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_deveLancarExcecaoQuandoNaoEncontrada() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_deveAtualizarNomeDaCategoria() {
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Pessoal");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(response);

        categoryService.update(1L, updateRequest);

        assertThat(category.getName()).isEqualTo("Pessoal");
        verify(categoryRepository).save(category);
    }

    @Test
    void delete_deveDeletarCategoriaExistente() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void delete_deveLancarExcecaoQuandoCategoriaInexistente() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).deleteById(any());
    }
}
