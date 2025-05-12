package http;

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
import com.google.gson.reflect.TypeToken;

import static org.junit.jupiter.api.Assertions.*;

public class TaskHandleTest extends HttpTaskManagerTestBase {

    // Проверка создания задачи (код ответа 201)
    @Test
    void testCreateTask() throws IOException, InterruptedException {
        // Подготовка данных для теста
        Task task = new Task("Задача 1", "Описание задачи 1", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        String taskJson = gson.toJson(task);

        // Отправка запроса
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверка ответа
        assertEquals(201, response.statusCode(), "Неверный код ответа при создании");

        // Проверки состояния менеджера задач
        List<Task> tasks = taskManager.getAllTasks();
        Task createdTask = tasks.getFirst();
        Task expectedTask = new Task(task.getTitle(), task.getDescription(), task.getStatus(),
                task.getStartTime(), task.getDuration());

        expectedTask.setId(createdTask.getId());
        assertEquals(expectedTask, createdTask, "Созданная задача не соответствует ожидаемой");
    }

    // Проверка обновления задачи (код ответа 201)
    @Test
    void testCreateAndUpdateTask() throws IOException, InterruptedException {
        // Подготовка и создание исходной задачи
        Task task2 = new Task("Задача 2", "Описание задачи 2", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        String taskJson = gson.toJson(task2);

        // Отправка запроса на создание
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());

        // Проверка создания
        assertEquals(201, createResponse.statusCode(), "Неверный код ответа при создании");

        // Получение созданной задачи
        List<Task> tasks = taskManager.getAllTasks();
        Task createdTask = tasks.getFirst();

        // Подготовка обновленных данных
        Task updatedTaskData = new Task(createdTask.getId(),
                "Обновленная задача",
                "Новое описание",
                Status.IN_PROGRESS,
                createdTask.getStartTime().plusDays(1),
                createdTask.getDuration().plusMinutes(15));
        String updateJson = gson.toJson(updatedTaskData);

        // Отправка запроса на обновление
        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(updateJson))
                .build();

        HttpResponse<String> updateResponse = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        // Проверка обновления
        assertEquals(201, updateResponse.statusCode(), "Неверный код ответа при обновлении");

        // Проверка конечного состояния
        Task finalTask = taskManager.getTaskById(createdTask.getId());
        Task expectedUpdatedTask = new Task(updatedTaskData.getTitle(),
                updatedTaskData.getDescription(),
                updatedTaskData.getStatus(),
                updatedTaskData.getStartTime(),
                updatedTaskData.getDuration());

        expectedUpdatedTask.setId(finalTask.getId());

        assertEquals(expectedUpdatedTask, finalTask, "Обновленная задача не соответствует ожидаемой");
    }

    // Проверка получения всех задач (код ответа 200)
    @Test
    public void testCreateAndGetAllTasks() throws IOException, InterruptedException {
        // Создание исходных задач
        Task task3 = new Task("Задача 3", "Описание задачи 3", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        taskManager.createTask(task3);
        Task task4 = new Task("Задача 4", "Описание задачи 4");
        taskManager.createTask(task4);

        // Отправка запроса на получение
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Десериализация ответа
        Type taskListType = new TypeToken<List<Task>>(){}.getType();
        List<Task> tasks = gson.fromJson(response.body(), taskListType);

        assertEquals(200, response.statusCode(), "Неверный код ответа при получении всех задач");
        assertEquals(2, tasks.size(), "Некорректное количество задач");
        assertEquals("Задача 3", tasks.get(0).getTitle(), "Некорректное имя задачи");
        assertEquals("Задача 4", tasks.get(1).getTitle(), "Некорректное имя задачи");
    }

    // Проверка получения задачи по ID (код ответа 200)
    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        // Подготовка исходных данных
        Task task5 = new Task("Задача 5", "Описание задачи 5");
        taskManager.createTask(task5);
        Task task6 = new Task("Задача 6", "Описание задачи 6");
        taskManager.createTask(task6);

        // Отправка запроса на получение
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task5.getId()))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Десериализация ответа
        Task task = gson.fromJson(response.body(), Task.class);

        assertEquals(200, response.statusCode(), "Неверный код ответа при получении задачи");
        assertEquals(task5.getId(), task.getId(), "Задача должна находиться по ID");
    }

    // Проверка удаления задачи по ID (код ответа 200)
    @Test
    public void testDeleteTaskById() throws IOException, InterruptedException {
        // Подготовка исходных данных
        Task task7 = new Task("Задача 7", "Описание задачи 7");
        taskManager.createTask(task7);
        Task task8 = new Task("Задача 8", "Описание задачи 8");
        taskManager.createTask(task8);

        // Отправка запроса на удаление
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task7.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа при удалении задачи");
        assertEquals(1, taskManager.getAllTasks().size(), "Должна остаться 1 задача");
        assertNotNull(taskManager.getTaskById(task8.getId()), "Задача 8 должна остаться");
    }

    // Проверка, что все задачи удаляются (код успеха 200)
    @Test
    public void testDeleteAllTasks() throws IOException, InterruptedException {
        // Подготовка исходных данных
        Task task9 = new Task("Задача 9", "Описание задачи 9");
        taskManager.createTask(task9);
        Task task10 = new Task("Задача 10", "Описание задачи 10");
        taskManager.createTask(task10);

        // Отправка запроса на удаление
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа при удалении всех задач");
        assertEquals(0, taskManager.getAllTasks().size(), "Все задачи должны быть удалены");
    }

    // Проверка, что задачи не могут пересекаться по времени (код ответа 406)
    @Test
    public void testCreateTasksTimeOverlap() throws IOException, InterruptedException {
        // Подготовка исходных данных
        LocalDateTime startTime = LocalDateTime.of(2025, 5,12,12,0);
        Duration duration = Duration.ofMinutes(20);
        Task task = new Task("Задача 1", "Описание", startTime, duration);
        Task task2 = new Task("Задача 2", "Описание", startTime.plusMinutes(10), duration);

        String taskJson = gson.toJson(task);

        // Отправка запроса
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Задача 1 должна быть создана");

        taskJson = gson.toJson(task2);

        // Отправка запроса
        HttpRequest overlapRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> overlapResponse = httpClient.send(overlapRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, overlapResponse.statusCode(), "Задачи не должны пересекаться по времени");
    }

    // Проверка, что нельзя отправить неподдерживаемый метод (код ответа 405)
    @Test
    public void testNotAllowedMethod() throws IOException, InterruptedException {

        // Отправка запроса с неподдерживаемым методом
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Метод PUT не поддерживается");
    }

    // Проверка запроса на получение несуществующей задачи (код ответа 404)
    @Test
    public void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        // Отправка запроса на получение несуществующей задачи
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    // Проверка отправки некорректного запроса (код ответа 400)
    @Test
    public void testAddTaskEmptyTitle() throws IOException, InterruptedException {
        Task task = new Task("Задача", "Описание", LocalDateTime.now(), Duration.ofMinutes(5));
        task.setTitle(null);

        String taskForRequest = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskForRequest))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
    }
}