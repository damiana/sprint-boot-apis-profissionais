package br.com.alura.runnercircleapi.exception;

public class ComentarioNotFoundException extends RuntimeException {

    public ComentarioNotFoundException(String message) {
        super(message);
    }
}
