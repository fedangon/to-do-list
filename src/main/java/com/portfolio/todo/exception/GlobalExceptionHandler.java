package com.portfolio.todo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

// Intercepta excecoes lancadas em qualquer controller da aplicacao
// @RestControllerAdvice combina @ControllerAdvice + @ResponseBody:
//   - @ControllerAdvice: aplica os handlers a todos os controllers
//   - @ResponseBody: garante que as respostas sejam serializadas como JSON
// Centraliza o tratamento de erros em um unico lugar, evitando try/catch nos controllers
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Captura ResourceNotFoundException lancada quando um recurso nao e encontrado no banco
    // Retorna 404 Not Found com a mensagem da excecao
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Recurso nao encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // Captura erros de validacao do @Valid nos controllers (ex: campos @NotBlank vazios)
    // getBindingResult() contem todos os erros de campo; extraimos "campo: mensagem" para cada um
    // Retorna 400 Bad Request com a lista de campos invalidos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();

        log.warn("Falha de validacao: {} erro(s) - {}", errors.size(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Validation failed")
                        .errors(errors)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // Captura IllegalArgumentException lancada em regras de negocio (ex: categoria duplicada)
    // Retorna 409 Conflict indicando conflito com o estado atual do recurso
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento invalido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    // Captura qualquer excecao nao tratada pelos handlers acima (erro generico/inesperado)
    // Loga com ERROR e stack trace completo para facilitar a investigacao
    // Retorna 500 Internal Server Error sem expor detalhes internos ao cliente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Erro inesperado: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("An unexpected error occurred")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}
