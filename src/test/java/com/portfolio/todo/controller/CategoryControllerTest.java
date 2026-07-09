package com.portfolio.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.todo.dto.CategoryRequest;
import com.portfolio.todo.model.Category;
import com.portfolio.todo.repository.CategoryRepository;
import com.portfolio.todo.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void createCategory_deveRetornar201QuandoDadosValidos() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Trabalho");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Trabalho"));
    }

    @Test
    void createCategory_deveRetornar400QuandoNomeVazio() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createCategory_deveRetornar409QuandoNomeDuplicado() throws Exception {
        categoryRepository.save(Category.builder().name("Trabalho").build());

        CategoryRequest request = new CategoryRequest();
        request.setName("Trabalho");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void findAll_deveRetornarListaDeCategorias() throws Exception {
        categoryRepository.save(Category.builder().name("Trabalho").build());
        categoryRepository.save(Category.builder().name("Pessoal").build());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void findById_deveRetornarCategoriaQuandoEncontrada() throws Exception {
        Category saved = categoryRepository.save(Category.builder().name("Estudos").build());

        mockMvc.perform(get("/api/categories/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Estudos"));
    }

    @Test
    void findById_deveRetornar404QuandoNaoEncontrada() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void update_deveAtualizarNomeDaCategoria() throws Exception {
        Category saved = categoryRepository.save(Category.builder().name("Trabalho").build());

        CategoryRequest request = new CategoryRequest();
        request.setName("Trabalho Remoto");

        mockMvc.perform(put("/api/categories/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Trabalho Remoto"));
    }

    @Test
    void delete_deveRetornar204QuandoCategoriaExistente() throws Exception {
        Category saved = categoryRepository.save(Category.builder().name("Temporária").build());

        mockMvc.perform(delete("/api/categories/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_deveRetornar404QuandoCategoriaInexistente() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
