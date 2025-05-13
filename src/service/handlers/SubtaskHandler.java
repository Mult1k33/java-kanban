package service.handlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import model.Subtask;
import service.exeptions.ManagerSaveException;
import service.exeptions.NotFoundException;
import service.exeptions.TimeOverlapException;
import service.managers.TaskManager;

import java.io.IOException;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler {

    public SubtaskHandler(TaskManager taskManager) {
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

    // Метод для обработки получения подзадач
    private void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.equals("/subtasks")) {
                List<Subtask> subtasks = taskManager.getAllSubtasks();
                sendText(exchange, subtasks);
            } else {
                int subtaskId = parseIdFromPath(path);
                Subtask subtask = taskManager.getSubtaskById(subtaskId);
                sendText(exchange, subtask);
            }
        } catch (NumberFormatException e) {
            sendErrorRequest(exchange, "Неверный формат ID подзадачи");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (Exception e) {
            sendServerErrorResponse(exchange, "Внутренняя ошибка сервера");
        }
    }

    // Метод для обработки создания или обновления подзадач
    private void handlePostRequest(HttpExchange exchange) throws IOException {
        try {
            Subtask subtask = parseRequestBody(exchange, Subtask.class);
            if (subtask.getId() == null || subtask.getId() == 0) {
                taskManager.createSubtask(subtask);
                sendCreateOrUpdateItem(exchange);
            } else {
                taskManager.updateSubtask(subtask);
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

    // Метод для обработки удаления подзадач
    private void handleDeleteRequest(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.equals("/subtasks")) {
                taskManager.deleteAllSubtasks();
                sendText(exchange, "Все подзадачи успешно удалены");
            } else {
                int subtaskId = parseIdFromPath(path);
                taskManager.deleteSubtask(subtaskId);
                sendText(exchange, "Подзадача успешно удалена");
            }
        } catch (NumberFormatException e) {
            sendErrorRequest(exchange, "Неверный формат ID подзадачи");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (ManagerSaveException e) {
            sendServerErrorResponse(exchange, e.getMessage());
        }
    }
}