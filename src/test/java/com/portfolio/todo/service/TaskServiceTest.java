package com.portfolio.todo.service;

import com.portfolio.todo.dto.TaskRequest;
import com.portfolio.todo.dto.TaskResponse;
import com.portfolio.todo.dto.TaskStatsResponse;
import com.portfolio.todo.exception.ResourceNotFoundException;
import com.portfolio.todo.mapper.TaskMapper;
import com.portfolio.todo.model.Priority;
import com.portfolio.todo.model.Task;
import com.portfolio.todo.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskRequest request;
    private TaskResponse response;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id(1L)
                .title("Estudar Spring Boot")
                .description("Revisar conceitos de JPA")
                .completed(false)
                .priority(Priority.HIGH)
                .categories(Set.of())
                .build();

        request = new TaskRequest();
        request.setTitle("Estudar Spring Boot");
        request.setDescription("Revisar conceitos de JPA");
        request.setPriority(Priority.HIGH);

        response = new TaskResponse();
        response.setId(1L);
        response.setTitle("Estudar Spring Boot");
        response.setCompleted(false);
        response.setPriority(Priority.HIGH);
    }

    @Test
    void create_deveSalvarTaskComCompletedFalse() {
        when(taskMapper.toEntity(request)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(response);

        TaskResponse result = taskService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getCompleted()).isFalse();
        verify(taskRepository).save(task);
    }

    @Test
    void findAll_deveRetornarPaginaDeRespostas() {
        Page<Task> page = new PageImpl<>(List.of(task));
        Pageable pageable = PageRequest.of(0, 10);

        when(taskRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(taskMapper.toResponse(task)).thenReturn(response);

        Page<TaskResponse> result = taskService.findAll(null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Estudar Spring Boot");
    }

    @Test
    void findById_deveRetornarTaskQuandoEncontrada() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(response);

        TaskResponse result = taskService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_deveLancarExcecaoQuandoNaoEncontrada() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_deveAtualizarERetornarTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(response);

        TaskResponse result = taskService.update(1L, request);

        assertThat(result).isNotNull();
        verify(taskMapper).updateEntity(request, task);
        verify(taskRepository).save(task);
    }

    @Test
    void update_deveLancarExcecaoQuandoTaskNaoEncontrada() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_deveDeletarTaskExistente() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.delete(1L);

        verify(taskRepository).deleteById(1L);
    }

    @Test
    void delete_deveLancarExcecaoQuandoTaskNaoEncontrada() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void getStats_deveRetornarEstatisticasCorretamente() {
        when(taskRepository.count()).thenReturn(10L);
        when(taskRepository.countByCompleted(true)).thenReturn(4L);
        when(taskRepository.countByCompleted(false)).thenReturn(6L);
        when(taskRepository.countByPriority(Priority.LOW)).thenReturn(2L);
        when(taskRepository.countByPriority(Priority.MEDIUM)).thenReturn(5L);
        when(taskRepository.countByPriority(Priority.HIGH)).thenReturn(3L);

        TaskStatsResponse stats = taskService.getStats();

        assertThat(stats.getTotal()).isEqualTo(10L);
        assertThat(stats.getCompleted()).isEqualTo(4L);
        assertThat(stats.getPending()).isEqualTo(6L);
        assertThat(stats.getByPriority()).containsEntry(Priority.LOW, 2L);
        assertThat(stats.getByPriority()).containsEntry(Priority.MEDIUM, 5L);
        assertThat(stats.getByPriority()).containsEntry(Priority.HIGH, 3L);
    }
}
