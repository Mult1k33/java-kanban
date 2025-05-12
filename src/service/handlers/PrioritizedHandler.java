package service.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.exeptions.NotFoundException;
import service.managers.TaskManager;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equals(method)) {
                List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
                sendText(exchange, prioritizedTasks);
            } else {
                sendMethodNotAllowed(exchange, path);
            }
        } catch (Exception e) {
            sendServerErrorResponse(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }
}
