package com.portfolio.todo.service;

import com.portfolio.todo.dto.TaskRequest;
import com.portfolio.todo.dto.TaskResponse;
import com.portfolio.todo.dto.TaskStatsResponse;
import com.portfolio.todo.exception.ResourceNotFoundException;
import com.portfolio.todo.mapper.TaskMapper;
import com.portfolio.todo.model.Category;
import com.portfolio.todo.model.Priority;
import com.portfolio.todo.model.Task;
import com.portfolio.todo.repository.TaskRepository;
import com.portfolio.todo.repository.TaskSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// Camada de servico responsavel pela logica de negocio das tasks
// @Service registra a classe como componente Spring gerenciado
// @RequiredArgsConstructor gera o construtor com os campos final (injecao de dependencia via construtor)
// @Transactional garante que cada metodo publico rode dentro de uma transacao do banco de dados
// Se uma excecao ocorrer no meio de um metodo, o banco faz rollback automatico
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    // Repositorio JPA para operacoes de banco na tabela de tasks
    private final TaskRepository taskRepository;

    // Mapper gerado pelo MapStruct para converter entre Task (entidade) e TaskRequest/TaskResponse (DTOs)
    private final TaskMapper taskMapper;

    // Servico de categorias usado para buscar e validar as categorias de uma task
    private final CategoryService categoryService;

    // Cria uma nova task com os dados recebidos no request
    public TaskResponse create(TaskRequest request) {
        log.info("Criando nova task: title='{}', priority={}, categorias={}",
                request.getTitle(), request.getPriority(), request.getCategoryIds());

        // Converte o DTO (TaskRequest) para a entidade Task usando o MapStruct
        Task task = taskMapper.toEntity(request);

        // Toda task criada começa como nao concluida
        task.setCompleted(false);

        // Busca as entidades Category a partir dos IDs enviados no request
        task.setCategories(resolveCategories(request));

        Task saved = taskRepository.save(task);
        log.info("Task criada com sucesso: id={}, title='{}'", saved.getId(), saved.getTitle());

        // Converte a entidade salva para DTO de resposta
        return taskMapper.toResponse(saved);
    }

    // Busca tasks com filtros opcionais e paginacao
    // readOnly = true informa ao banco que nao haverá escrita, melhorando a performance
    @Transactional(readOnly = true)
    public Page<TaskResponse> findAll(Boolean completed, Priority priority, Long categoryId, Pageable pageable) {
        log.debug("Buscando tasks com filtros: completed={}, priority={}, categoryId={}, page={}, size={}",
                completed, priority, categoryId, pageable.getPageNumber(), pageable.getPageSize());

        // Specification permite combinar filtros dinamicamente sem criar multiplos metodos no repository
        // where() inicia com um filtro nulo (sem restricao), e os .and() adicionam os filtros opcionais
        Specification<Task> spec = Specification
                .where(TaskSpecification.hasCompleted(completed))
                .and(TaskSpecification.hasPriority(priority))
                .and(TaskSpecification.hasCategoryId(categoryId));

        // findAll com Specification e Pageable retorna uma Page com os resultados e metadados de paginacao
        Page<TaskResponse> result = taskRepository.findAll(spec, pageable).map(taskMapper::toResponse);
        log.debug("Encontradas {} tasks (total: {})", result.getNumberOfElements(), result.getTotalElements());

        return result;
    }

    // Busca uma task pelo ID e lanca excecao se nao existir
    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        log.debug("Buscando task por id: {}", id);
        TaskResponse response = taskMapper.toResponse(findTaskById(id));
        log.debug("Task encontrada: id={}, title='{}'", id, response.getTitle());
        return response;
    }

    // Atualiza os campos da task existente com os dados do request
    public TaskResponse update(Long id, TaskRequest request) {
        log.info("Atualizando task id={}: title='{}', priority={}", id, request.getTitle(), request.getPriority());

        // Garante que a task existe antes de tentar atualizar
        Task task = findTaskById(id);

        // updateEntity usa @BeanMapping com IGNORE para nao sobrescrever campos nulos do request
        taskMapper.updateEntity(request, task);

        // Substitui as categorias atuais pelas do novo request
        task.setCategories(resolveCategories(request));

        Task saved = taskRepository.save(task);
        log.info("Task id={} atualizada com sucesso", id);

        return taskMapper.toResponse(saved);
    }

    // Remove uma task do banco pelo ID
    public void delete(Long id) {
        log.info("Deletando task id={}", id);

        // Valida que a task existe antes de deletar (lanca 404 se nao encontrar)
        findTaskById(id);
        taskRepository.deleteById(id);
        log.info("Task id={} deletada com sucesso", id);
    }

    // Retorna um resumo estatistico das tasks (totais por status e por prioridade)
    @Transactional(readOnly = true)
    public TaskStatsResponse getStats() {
        log.debug("Calculando estatisticas das tasks");

        // Itera sobre todos os valores do enum Priority e conta quantas tasks existem de cada tipo
        // LinkedHashMap preserva a ordem de insercao (LOW -> MEDIUM -> HIGH)
        Map<Priority, Long> byPriority = new LinkedHashMap<>();
        Arrays.stream(Priority.values())
                .forEach(p -> byPriority.put(p, taskRepository.countByPriority(p)));

        TaskStatsResponse stats = TaskStatsResponse.builder()
                .total(taskRepository.count())
                .completed(taskRepository.countByCompleted(true))
                .pending(taskRepository.countByCompleted(false))
                .byPriority(byPriority)
                .build();

        log.debug("Estatisticas: total={}, concluidas={}, pendentes={}, byPriority={}",
                stats.getTotal(), stats.getCompleted(), stats.getPending(), stats.getByPriority());

        return stats;
    }

    // Metodo auxiliar privado que busca a entidade Task pelo ID
    // Centraliza o tratamento de "nao encontrado" para evitar repeticao nos outros metodos
    private Task findTaskById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> {
            log.warn("Task nao encontrada: id={}", id);
            return new ResourceNotFoundException("Task not found with id: " + id);
        });
    }

    // Converte a lista de IDs de categorias do request para entidades Category do banco
    // Retorna um HashSet mutavel (necessario para o JPA gerenciar o relacionamento ManyToMany)
    private Set<Category> resolveCategories(TaskRequest request) {
        if (request.getCategoryIds() == null || request.getCategoryIds().isEmpty()) {
            return new HashSet<>();
        }
        log.debug("Resolvendo {} categoria(s): ids={}", request.getCategoryIds().size(), request.getCategoryIds());
        return request.getCategoryIds().stream()
                .map(categoryService::findCategoryById)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
