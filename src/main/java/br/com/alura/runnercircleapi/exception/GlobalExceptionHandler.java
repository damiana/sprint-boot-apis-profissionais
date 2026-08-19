package br.com.alura.runnercircleapi.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

    @ExceptionHandler({TreinoNotFoundException.class, ComentarioNotFoundException.class, UsuarioNaoEncontradoException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> tratarRecursoNaoEncontrado(RuntimeException exception) {
        return Map.of("erro", exception.getMessage());
    }

    @ExceptionHandler(ImagemInvalidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> tratarImagemInvalida(ImagemInvalidaException exception) {
        return Map.of("erro", exception.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Map<String, String> tratarContentTypeNaoSuportado(HttpMediaTypeNotSupportedException exception) {
        return Map.of("erro", "tipo de conteúdo não suportado");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> tratarTamanhoDeUploadExcedido(MaxUploadSizeExceededException exception) {
        return Map.of("erro", "o arquivo enviado excede o tamanho máximo permitido");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> tratarErroInterno(Exception exception) {
        return Map.of("erro", "ocorreu um erro interno no servidor");
    }
}
