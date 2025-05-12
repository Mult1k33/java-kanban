package service.handlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import model.Task;
import service.exeptions.ManagerSaveException;
import service.exeptions.NotFoundException;
import service.exeptions.TimeOverlapException;
import service.managers.TaskManager;

import java.io.IOException;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGetRequest(exchange, path);
                    break;
                case "POST":
                    handlePostRequest(exchange);
                    break;
                case "DELETE":
                    handleDeleteRequest(exchange, path);
                    break;
                default:
                    sendMethodNotAllowed(exchange, path);
            }
        } catch (JsonSyntaxException e) {
            sendErrorRequest(exchange, "Неверный формат JSON");
        } catch (NumberFormatException e) {
            sendErrorRequest(exchange, "Неверный формат данных");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (TimeOverlapException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (ManagerSaveException e) {
            sendServerErrorResponse(exchange, e.getMessage());
        } catch (Exception e) {
            sendServerErrorResponse(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }

    // Метод для обработки получения задач
    private void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.equals("/tasks")) {
                List<Task> tasks = taskManager.getAllTasks();
                sendText(exchange, tasks);
            } else {
                int taskId = parseIdFromPath(path);
                Task task = taskManager.getTaskById(taskId);
                sendText(exchange, task);
            }
        } catch (NumberFormatException e) {
            sendErrorRequest(exchange, "Неверный формат ID задачи");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (Exception e) {
            sendServerErrorResponse(exchange, "Внутренняя ошибка сервера");
        }
    }

    // Метод для обработки создания или обновления задач
    private void handlePostRequest(HttpExchange exchange) throws IOException {
        try {
            Task task = parseRequestBody(exchange, Task.class);
            if (task.getId() == null || task.getId() == 0) {
                taskManager.createTask(task);
                sendCreateOrUpdateItem(exchange);
            } else {
                taskManager.updateTask(task);
                sendCreateOrUpdateItem(exchange);
            }
        } catch (JsonSyntaxException e) {
            sendErrorRequest(exchange, "Неверный формат JSON");
        } catch (TimeOverlapException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendErrorRequest(exchange, e.getMessage());
        }
    }

    // Метод для обработки удаления задач
    private void handleDeleteRequest(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.equals("/tasks")) {
                taskManager.deleteAllTasks();
                sendText(exchange, "Все задачи успешно удалены");
            } else {
                int taskId = parseIdFromPath(path);
                taskManager.deleteTask(taskId);
                sendText(exchange, "Задача успешно удалена");
            }
        } catch (NumberFormatException e) {
            sendErrorRequest(exchange, "Неверный формат ID задачи");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (ManagerSaveException e) {
            sendServerErrorResponse(exchange, e.getMessage());
        }
    }
}