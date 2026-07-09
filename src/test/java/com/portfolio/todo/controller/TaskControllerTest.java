package com.portfolio.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.todo.dto.TaskRequest;
import com.portfolio.todo.model.Category;
import com.portfolio.todo.model.Priority;
import com.portfolio.todo.model.Task;
import com.portfolio.todo.repository.CategoryRepository;
import com.portfolio.todo.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

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

    private TaskRequest buildRequest(String title, Priority priority) {
        TaskRequest request = new TaskRequest();
        request.setTitle(title);
        request.setPriority(priority);
        return request;
    }

    private Task saveTask(String title, Priority priority, boolean completed) {
        return taskRepository.save(Task.builder()
                .title(title)
                .priority(priority)
                .completed(completed)
                .build());
    }

    @Test
    void createTask_deveRetornar201QuandoDadosValidos() throws Exception {
        TaskRequest request = buildRequest("Estudar Spring Boot", Priority.HIGH);
        request.setDescription("Revisar JPA e Hibernate");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Estudar Spring Boot"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void createTask_deveRetornar400QuandoTituloVazio() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setPriority(Priority.LOW);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void createTask_deveRetornar400QuandoPriorityNula() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("Task sem priority");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createTask_deveAssociarCategoriaQuandoInformada() throws Exception {
        Category category = categoryRepository.save(Category.builder().name("Trabalho").build());

        TaskRequest request = buildRequest("Reunião", Priority.MEDIUM);
        request.setCategoryIds(Set.of(category.getId()));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categories[0].name").value("Trabalho"));
    }

    @Test
    void findAll_deveRetornarPaginaComTasks() throws Exception {
        saveTask("Task A", Priority.LOW, false);
        saveTask("Task B", Priority.HIGH, true);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void findAll_deveFiltrarPorCompleted() throws Exception {
        saveTask("Pendente", Priority.LOW, false);
        saveTask("Concluída", Priority.HIGH, true);

        mockMvc.perform(get("/api/tasks").param("completed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Pendente"));
    }

    @Test
    void findAll_deveFiltrarPorPriority() throws Exception {
        saveTask("Alta", Priority.HIGH, false);
        saveTask("Baixa", Priority.LOW, false);

        mockMvc.perform(get("/api/tasks").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"));
    }

    @Test
    void findById_deveRetornarTaskQuandoEncontrada() throws Exception {
        Task saved = saveTask("Minha task", Priority.MEDIUM, false);

        mockMvc.perform(get("/api/tasks/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Minha task"));
    }

    @Test
    void findById_deveRetornar404QuandoTaskInexistente() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void update_deveAtualizarTaskExistente() throws Exception {
        Task saved = saveTask("Título antigo", Priority.LOW, false);

        TaskRequest request = buildRequest("Título atualizado", Priority.HIGH);

        mockMvc.perform(put("/api/tasks/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Título atualizado"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void update_deveRetornar404QuandoTaskInexistente() throws Exception {
        TaskRequest request = buildRequest("Qualquer", Priority.LOW);

        mockMvc.perform(put("/api/tasks/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_deveRetornar204QuandoTaskExistente() throws Exception {
        Task saved = saveTask("Para deletar", Priority.LOW, false);

        mockMvc.perform(delete("/api/tasks/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_deveRetornar404QuandoTaskInexistente() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStats_deveRetornarEstatisticasCorretas() throws Exception {
        saveTask("Pendente 1", Priority.HIGH, false);
        saveTask("Pendente 2", Priority.LOW, false);
        saveTask("Concluída", Priority.MEDIUM, true);

        mockMvc.perform(get("/api/tasks/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.pending").value(2))
                .andExpect(jsonPath("$.byPriority.HIGH").value(1))
                .andExpect(jsonPath("$.byPriority.LOW").value(1))
                .andExpect(jsonPath("$.byPriority.MEDIUM").value(1));
    }
}
