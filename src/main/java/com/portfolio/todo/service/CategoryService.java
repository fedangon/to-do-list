package com.portfolio.todo.service;

import com.portfolio.todo.dto.CategoryRequest;
import com.portfolio.todo.dto.CategoryResponse;
import com.portfolio.todo.exception.ResourceNotFoundException;
import com.portfolio.todo.mapper.CategoryMapper;
import com.portfolio.todo.model.Category;
import com.portfolio.todo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

// Camada de servico responsavel pela logica de negocio das categorias
// Nao possui @Transactional no nivel da classe pois cada metodo e simples o suficiente
// para gerenciar a transacao individualmente via Spring Data JPA
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    // Repositorio JPA para operacoes de banco na tabela de categorias
    private final CategoryRepository categoryRepository;

    // Mapper gerado pelo MapStruct para converter entre Category e CategoryRequest/CategoryResponse
    private final CategoryMapper categoryMapper;

    // Cria uma nova categoria verificando se o nome ja existe no banco
    public CategoryResponse create(CategoryRequest request) {
        log.info("Criando categoria: name='{}'", request.getName());

        // Valida unicidade do nome antes de tentar inserir
        // Lanca IllegalArgumentException que o GlobalExceptionHandler converte em 409 Conflict
        if (categoryRepository.existsByName(request.getName())) {
            log.warn("Tentativa de criar categoria duplicada: name='{}'", request.getName());
            throw new IllegalArgumentException("Category already exists: " + request.getName());
        }

        // Converte o DTO para entidade e salva diretamente em uma linha
        Category saved = categoryRepository.save(categoryMapper.toEntity(request));
        log.info("Categoria criada com sucesso: id={}, name='{}'", saved.getId(), saved.getName());

        return categoryMapper.toResponse(saved);
    }

    // Retorna todas as categorias cadastradas como lista de DTOs
    public List<CategoryResponse> findAll() {
        log.debug("Buscando todas as categorias");
        List<CategoryResponse> categories = categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
        log.debug("Encontradas {} categoria(s)", categories.size());
        return categories;
    }

    // Retorna uma categoria pelo ID como DTO de resposta
    public CategoryResponse findById(Long id) {
        log.debug("Buscando categoria por id: {}", id);

        // findCategoryById retorna a entidade; aqui convertemos para DTO
        CategoryResponse response = categoryMapper.toResponse(findCategoryById(id));
        log.debug("Categoria encontrada: id={}, name='{}'", id, response.getName());
        return response;
    }

    // Atualiza o nome da categoria e loga a mudanca (nome antigo -> nome novo)
    public CategoryResponse update(Long id, CategoryRequest request) {
        log.info("Atualizando categoria id={}: novo name='{}'", id, request.getName());

        Category category = findCategoryById(id);

        // Guarda o nome antigo para exibir no log de confirmacao
        String oldName = category.getName();
        category.setName(request.getName());
        Category saved = categoryRepository.save(category);

        log.info("Categoria id={} atualizada: '{}' -> '{}'", id, oldName, saved.getName());
        return categoryMapper.toResponse(saved);
    }

    // Remove uma categoria pelo ID
    public void delete(Long id) {
        log.info("Deletando categoria id={}", id);

        // Valida existencia antes de deletar para retornar 404 se nao encontrar
        findCategoryById(id);
        categoryRepository.deleteById(id);
        log.info("Categoria id={} deletada com sucesso", id);
    }

    // Metodo publico que retorna a entidade Category (nao o DTO)
    // Usado internamente pelo CategoryService e tambem pelo TaskService ao resolver categorias de uma task
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> {
            log.warn("Categoria nao encontrada: id={}", id);
            return new ResourceNotFoundException("Category not found with id: " + id);
        });
    }
}
