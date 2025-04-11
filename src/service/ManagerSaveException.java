package service;

/**
 * Исключение в случае проблем с сохранением данных таск-менеджером в файл
 */

public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException() {
        super();
    }

    public ManagerSaveException(String message) {
        super(message);
    }
}
