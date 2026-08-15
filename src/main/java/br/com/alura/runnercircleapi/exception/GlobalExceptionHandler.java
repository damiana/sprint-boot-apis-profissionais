package br.com.alura.runnercircleapi.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> tratarErroDeValidacao(MethodArgumentNotValidException exception) {
        Map<String, String> erros = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(erro ->
                erros.put(erro.getField(), erro.getDefaultMessage())
        );

        return erros;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> tratarErroDeValidacao(ConstraintViolationException exception) {
        Map<String, String> erros = new LinkedHashMap<>();

        exception.getConstraintViolations().forEach(violacao ->
                erros.put(violacao.getPropertyPath().toString(), violacao.getMessage())
        );

        return erros;
    }
}
