package dev.guilhermealves.assets.portfolio.api.app.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String id) {
        super(String.format("%s não encontrado com ID: %s", resourceName, id));
    }
}
