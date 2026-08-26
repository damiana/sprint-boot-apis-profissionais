package br.com.alura.runnercircleapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse tratarErroDeValidacao(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String mensagem = exception.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Erro de validação em {}: {}", request.getRequestURI(), mensagem);

        return construirErro(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse tratarErroDeValidacao(ConstraintViolationException exception, HttpServletRequest request) {
        String mensagem = exception.getConstraintViolations().stream()
                .map(violacao -> violacao.getPropertyPath() + ": " + violacao.getMessage())
                .collect(Collectors.joining("; "));

        log.warn("Erro de validação em {}: {}", request.getRequestURI(), mensagem);

        return construirErro(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler({TreinoNotFoundException.class, ComentarioNotFoundException.class, UsuarioNaoEncontradoException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse tratarRecursoNaoEncontrado(RuntimeException exception, HttpServletRequest request) {
        log.warn("Recurso não encontrado em {}: {}", request.getRequestURI(), exception.getMessage());

        return construirErro(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse tratarCredenciaisInvalidas(CredenciaisInvalidasException exception, HttpServletRequest request) {
        log.warn("Tentativa de login com credenciais inválidas em {}", request.getRequestURI());

        return construirErro(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
    }

    @ExceptionHandler(ImagemInvalidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse tratarImagemInvalida(ImagemInvalidaException exception, HttpServletRequest request) {
        log.warn("Imagem inválida em {}: {}", request.getRequestURI(), exception.getMessage());

        return construirErro(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ErrorResponse tratarContentTypeNaoSuportado(HttpMediaTypeNotSupportedException exception, HttpServletRequest request) {
        log.warn("Tipo de conteúdo não suportado em {}: {}", request.getRequestURI(), exception.getContentType());

        return construirErro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "tipo de conteúdo não suportado", request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse tratarTamanhoDeUploadExcedido(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        log.warn("Tamanho de upload excedido em {}", request.getRequestURI());

        return construirErro(HttpStatus.BAD_REQUEST, "o arquivo enviado excede o tamanho máximo permitido", request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse tratarErroInterno(Exception exception, HttpServletRequest request) {
        log.error("Erro interno não tratado em {}", request.getRequestURI(), exception);

        return construirErro(HttpStatus.INTERNAL_SERVER_ERROR, "ocorreu um erro interno no servidor", request);
    }

    private ErrorResponse construirErro(HttpStatus status, String mensagem, HttpServletRequest request) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), mensagem, request.getRequestURI());
    }
}
