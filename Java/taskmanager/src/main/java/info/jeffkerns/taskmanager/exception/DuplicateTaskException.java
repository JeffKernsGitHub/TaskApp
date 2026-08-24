package info.jeffkerns.taskmanager.exception;

public class DuplicateTaskException extends RuntimeException {
         public DuplicateTaskException(String title) {
            super("A task with title '" + title + "' already exists");
        }
}
