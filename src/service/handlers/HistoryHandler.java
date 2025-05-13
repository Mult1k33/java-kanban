package service.handlers;

import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.managers.TaskManager;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equals(method)) {
                List<Task> history = taskManager.getHistory();
                sendText(exchange, history);
            } else {
                sendMethodNotAllowed(exchange, path);
            }
        } catch (Exception e) {
            sendServerErrorResponse(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }
}