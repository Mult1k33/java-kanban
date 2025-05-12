package http;

import com.google.gson.reflect.TypeToken;
import enums.Status;
import model.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SubtaskHandleTest extends HttpTaskManagerTestBase {

    // Проверка создания подзадачи (код ответа 201)
    @Test
    void testCreateSubtask() throws IOException, InterruptedException {
        // Подготовка данных для теста
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи",
                LocalDateTime.now(), Duration.ofMinutes(30), epic.getId());
        String taskJson = gson.toJson(subtask);

        // Отправка запроса
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверка ответа
        assertEquals(201, response.statusCode(), "Неверный код ответа при создании");

        // Проверки состояния менеджера задач
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        Subtask createdSubtask = subtasks.getFirst();
        Subtask expectedSubtask = new Subtask(subtask.getTitle(), subtask.getDescription(),
                subtask.getStartTime(), subtask.getDuration(), epic.getId());

        expectedSubtask.setId(createdSubtask.getId());
        assertEquals(expectedSubtask, createdSubtask, "Созданная подзадача не соответствует ожидаемой");
    }

    // Проверка обновления подзадачи (код ответа 201)
    @Test
    void testCreateAndUpdateSubtask() throws IOException, InterruptedException {
        // Подготовка данных для теста
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи",
                LocalDateTime.now(), Duration.ofMinutes(30), epic.getId());
        String taskJson = gson.toJson(subtask);

        // Отправка запроса на создание
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());

        // Проверка создания
        assertEquals(201, createResponse.statusCode(), "Неверный код ответа при создании");

        // Получение созданной задачи
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        Subtask createdSubtask = subtasks.getFirst();

        // Подготовка обновленных данных
        Subtask updatedSubtaskData = new Subtask(createdSubtask.getId(),
                "Обновленная задача",
                "Новое описание",
                Status.IN_PROGRESS,
                createdSubtask.getStartTime().plusDays(1),
                createdSubtask.getDuration().plusMinutes(15),
                createdSubtask.getEpicId());
        String updateJson = gson.toJson(createdSubtask);

        // Отправка запроса на обновление
        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(updateJson))
                .build();

        HttpResponse<String> updateResponse = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        // Проверка обновления
        assertEquals(201, updateResponse.statusCode(), "Неверный код ответа при обновлении");

        // Проверка конечного состояния
        Subtask finalSubtask = taskManager.getSubtaskById(createdSubtask.getId());
        Subtask expectedUpdatedSubtask = new Subtask(updatedSubtaskData.getId(),
                updatedSubtaskData.getTitle(),
                updatedSubtaskData.getDescription(),
                updatedSubtaskData.getStatus(),
                updatedSubtaskData.getStartTime(),
                updatedSubtaskData.getDuration(),
                updatedSubtaskData.getEpicId());

        expectedUpdatedSubtask.setId(finalSubtask.getId());

        assertEquals(expectedUpdatedSubtask, finalSubtask, "Обновленная подзадача не соответствует ожидаемой");
    }

    // Проверка получения всех подзадач (код ответа 200)
    @Test
    public void testCreateAndGetAllSubtasks() throws IOException, InterruptedException {
        // Подготовка данных для теста
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", epic.getId());
        taskManager.createSubtask(subtask);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epic.getId());
        taskManager.createSubtask(subtask2);

        // Отправка запроса на получение
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Десериализация ответа
        Type taskListType = new TypeToken<List<Subtask>>() {
        }.getType();
        List<Subtask> subtasks = gson.fromJson(response.body(), taskListType);

        assertEquals(200, response.statusCode(), "Неверный код ответа при получении всех задач");
        assertEquals(2, subtasks.size(), "Некорректное количество задач");
        assertEquals("Подзадача", subtasks.get(0).getTitle(), "Некорректное имя задачи");
        assertEquals("Подзадача 2", subtasks.get(1).getTitle(), "Некорректное имя задачи");
    }

    // Проверка получения подзадачи по ID (код ответа 200)
    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        // Подготовка данных для теста
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача", "Описание подзадачи", epic.getId());
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epic.getId());
        taskManager.createSubtask(subtask2);

        // Отправка запроса на получение
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask2.getId()))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Десериализация ответа
        Subtask subtask = gson.fromJson(response.body(), Subtask.class);

        assertEquals(200, response.statusCode(), "Неверный код ответа при получении задачи");
        assertEquals(subtask2.getId(), subtask.getId(), "Задача должна находиться по ID");
    }

    // Проверка удаления подзадачи по ID (код ответа 200)
    @Test
    public void testDeleteSubtaskById() throws IOException, InterruptedException {
        // Подготовка данных для теста
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи", epic.getId());
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epic.getId());
        taskManager.createSubtask(subtask2);

        // Отправка запроса на получение
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask2.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа при удалении подзадачи");
        assertEquals(1, taskManager.getAllSubtasks().size(), "Должна остаться 1 подзадача");
        assertNotNull(taskManager.getSubtaskById(subtask1.getId()), "Подзадача 1 должна остаться");
    }

    // Проверка, что все подзадачи удаляются (код успеха 200)
    @Test
    public void testDeleteAllSubtasks() throws IOException, InterruptedException {
        // Подготовка данных для теста
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи", epic.getId());
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epic.getId());
        taskManager.createSubtask(subtask2);

        // Отправка запроса на удаление
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа при удалении всех подзадач");
        assertEquals(0, taskManager.getAllTasks().size(), "Все подзадачи должны быть удалены");
    }

    // Проверка, что подзадачи не могут пересекаться по времени (код ответа 406)
    @Test
    public void testCreateSubtasksTimeOverlap() throws IOException, InterruptedException {
        // Подготовка исходных данных
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic);

        LocalDateTime startTime = LocalDateTime.of(2025, 5,12,12,0);
        Duration duration = Duration.ofMinutes(20);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1",
                startTime, duration, epic.getId());

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2",
                startTime.plusMinutes(10), duration, epic.getId());

        String taskJson = gson.toJson(subtask1);

        // Отправка запроса
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Подзадача 1 должна быть создана");

        taskJson = gson.toJson(subtask2);

        // Отправка запроса
        HttpRequest overlapRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> overlapResponse = httpClient.send(overlapRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, overlapResponse.statusCode(),
                "Подзадачи не должны пересекаться по времени");
    }

    // Проверка, что нельзя отправить неподдерживаемый метод (код ответа 405)
    @Test
    public void testNotAllowedMethod() throws IOException, InterruptedException {
        // Отправка запроса с неподдерживаемым методом
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Метод PUT не поддерживается");
    }

    // Проверка запроса на получение несуществующей задачи (код ответа 404)
    @Test
    public void testGetSubtaskByIdNotFound() throws IOException, InterruptedException {
        // Отправка запроса на получение несуществующей подзадачи
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    // Проверка отправки некорректного запроса (код ответа 400)
    @Test
    public void testAddSubtaskEmptyTitle() throws IOException, InterruptedException {
        // Подготовка исходных данных
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId());
        subtask.setTitle(null);

        String taskForRequest = gson.toJson(subtask);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskForRequest))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
    }
}