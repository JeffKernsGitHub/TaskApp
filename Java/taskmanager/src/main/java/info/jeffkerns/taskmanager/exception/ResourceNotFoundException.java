package info.jeffkerns.taskmanager.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        // JDK 25 Flexible Constructor Bodies: pre-super preparation
        var cleanMessage = (message != null) ? message.strip() : "Requested resource was not found";
        super(cleanMessage);
    }
}
