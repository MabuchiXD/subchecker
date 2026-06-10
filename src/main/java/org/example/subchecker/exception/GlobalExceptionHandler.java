package org.example.subchecker.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleNotFound(ResourceNotFoundException ex){
        log.error("\uD83D\uDFE5 Ошибка поиска:{} \uD83D\uDFE5", ex.getMessage());
    }
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public void handleValidation(jakarta.validation.ConstraintViolationException ex){
        log.warn("⚠\uFE0F Кривые руки пользователя: {} ⚠\uFE0F", ex.getMessage());
    }

}
