package service.handlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import model.*;
import service.exeptions.ManagerSaveException;
import service.exeptions.NotFoundException;
import service.managers.TaskManager;

import java.io.IOException;
import java.util.List;

public class EpicHandler extends BaseHttpHandler {

    public EpicHandler(TaskManager taskManager) {
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
        } catch (ManagerSaveException e) {
            sendServerErrorResponse(exchange, e.getMessage());
        } catch (Exception e) {
            sendServerErrorResponse(exchange, "Ошибка сервера: " + e.getMessage());
        }
    }

    // Метод для обработки получения эпиков
    private void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.equals("/epics")) {
                List<Epic> epics = taskManager.getAllEpics();
                sendText(exchange, epics);
            } else if (path.startsWith("/epics/") && path.endsWith("/subtasks")) {
                int epicId = parseIdFromPath(path.replace("/subtasks", ""));
                List<Subtask> subtasks = taskManager.getSubtaskByEpic(epicId);
                sendText(exchange, subtasks);
            } else {
                int epicId = parseIdFromPath(path);
                Epic epic = taskManager.getEpicById(epicId);
                sendText(exchange, epic);
            }
        } catch (NumberFormatException e) {
            sendErrorRequest(exchange, "Неверный формат ID эпика");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (Exception e) {
            sendServerErrorResponse(exchange, "Внутренняя ошибка сервера");
        }
    }

    // Метод для обработки создания или обновления эпиков
    private void handlePostRequest(HttpExchange exchange) throws IOException {
        try {
            Epic epic = parseRequestBody(exchange, Epic.class);
            if (epic.getId() == null || epic.getId() == 0) {
                taskManager.createEpic(epic);
                sendCreateOrUpdateItem(exchange);
            } else {
                taskManager.updateEpic(epic);
                sendCreateOrUpdateItem(exchange);
            }
        } catch (JsonSyntaxException e) {
            sendErrorRequest(exchange, "Неверный формат JSON");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendErrorRequest(exchange, e.getMessage());
        }
    }

    // Метод для обработки удаления эпиков
    private void handleDeleteRequest(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.equals("/epics")) {
                taskManager.deleteAllEpics();
                sendText(exchange, "Все эпики успешно удалены");
            } else {
                int epicId = parseIdFromPath(path);
                taskManager.deleteEpic(epicId);
                sendText(exchange, "Эпик успешно удален");
            }
        } catch (NumberFormatException e) {
            sendErrorRequest(exchange, "Неверный формат ID эпика");
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (ManagerSaveException e) {
            sendServerErrorResponse(exchange, e.getMessage());
        }
    }
}