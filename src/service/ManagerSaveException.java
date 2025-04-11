package service;

/**
 * Исключение в случае проблем с чтением таск-менеджером из файла
 */

public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException() {
        super();
    }

    public ManagerSaveException(String message) {
        super(message);
    }
}
