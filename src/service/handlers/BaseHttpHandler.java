package service.handlers;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpHandler;
import service.managers.TaskManager;
import service.typeadapters.DurationAdapter;
import service.typeadapters.LocalDateTimeAdapter;

/**
 * Общий для всех обработчиков базовый класс,
 * который содержит общие методы для чтения и отправки данных:
 */

public abstract class BaseHttpHandler implements HttpHandler {

    protected final Gson gson;
    protected final TaskManager taskManager;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    // Отправка общего ответа в случае успеха c данными
    protected void sendText(HttpExchange exchange, Object data) throws IOException {
        String jsonResponse = gson.toJson(data);
        sendMessage(exchange, jsonResponse, 200);
    }

    // Отправка общего ответа в случае успеха без данных
    protected void sendCreateOrUpdateItem(HttpExchange exchange) throws IOException {
        sendMessage(exchange, "", 201);
    }

    // Отправка ответа в случае, если объект не был найден
    protected void sendNotFound(HttpExchange exchange, String text) throws IOException {
        sendMessage(exchange, text, 404);
    }

    //Отправка ответа, если при создании или обновлении задача пересекается с уже существующими
    protected void sendHasInteractions(HttpExchange exchange, String text) throws IOException {
        sendMessage(exchange, text, 406);
    }

    // Отправка ответа в случае ошибки при обработке запроса
    protected void sendServerErrorResponse(HttpExchange exchange, String text) throws IOException {
        sendMessage(exchange, text, 500);
    }

    // Вспомогательный метод для обработки невалидных запросов
    protected void sendErrorRequest(HttpExchange exchange, String text) throws IOException {
        sendMessage(exchange, text, 400);
    }

    // Вспомогательный метод для обработки неподдерживаемых HTTP-методов
    protected void sendMethodNotAllowed(HttpExchange exchange, String text) throws IOException {
        sendMessage(exchange, text, 405);
    }

    // Парсинг
    protected <T> T parseRequestBody(HttpExchange exchange, Class<T> type) throws IOException, JsonSyntaxException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body == null || body.trim().isEmpty()) {
            throw new JsonSyntaxException("Пустое тело запроса");
        }
        return gson.fromJson(body, type);
    }

    // Извлечение ID из пути URL
    protected int parseIdFromPath(String path) throws IOException {
        try {
            String[] parts = path.split("/");
            if (parts.length < 3) {
                throw new IOException("Идентификатор не указан в пути");
            }
            return Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new IOException("Неверный формат ID");
        }
    }

    // Базовый метод для отправки HTTP-ответа в формате JSON
    private void sendMessage(HttpExchange exchange, String json, int statusCode) throws IOException {
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");

        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            if (responseBytes.length > 0) {
                os.write(responseBytes);
            }
        }
    }
}