package dev.guilhermealves.assets.portfolio.api.app.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Recurso Não Encontrado");
        problem.setType(URI.create("https://api.assets-portfolio.dev/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Regra de Negócio Violada");
        problem.setType(URI.create("https://api.assets-portfolio.dev/errors/business-rule"));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation error: {}", errors);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais campos contêm valores inválidos.");
        problem.setTitle("Erro de Validação");
        problem.setType(URI.create("https://api.assets-portfolio.dev/errors/validation"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON or invalid value: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou malformado. Verifique os tipos e enums informados.");
        problem.setTitle("Requisição Inválida");
        problem.setType(URI.create("https://api.assets-portfolio.dev/errors/bad-request"));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Parâmetro Inválido");
        problem.setType(URI.create("https://api.assets-portfolio.dev/errors/illegal-argument"));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Acesso Negado");
        problem.setType(URI.create("https://api.assets-portfolio.dev/errors/forbidden"));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof org.springframework.security.access.AccessDeniedException ade) {
                return handleAccessDenied(ade);
            }
            if (cause instanceof ResourceNotFoundException rnfe) {
                return handleResourceNotFoundException(rnfe);
            }
            cause = cause.getCause();
        }

        log.error("Internal server error: ", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno no servidor. Tente novamente mais tarde.");
        problem.setTitle("Erro Interno do Servidor");
        problem.setType(URI.create("https://api.assets-portfolio.dev/errors/internal-server-error"));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
