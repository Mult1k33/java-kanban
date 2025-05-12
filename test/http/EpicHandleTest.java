package http;

import model.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import static org.junit.jupiter.api.Assertions.*;

public class EpicHandleTest extends HttpTaskManagerTestBase {

    // Проверка создания эпика (код ответа 201)
    @Test
    void testCreateEpic() throws IOException, InterruptedException {
        // Подготовка данных для теста
        Epic epic = new Epic("Эпик", "Описание эпика");
        String taskJson = gson.toJson(epic);

        // Отправка запроса
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверка ответа
        assertEquals(201, response.statusCode(), "Неверный код ответа при создании");

        // Проверки состояния менеджера задач
        List<Epic> epics = taskManager.getAllEpics();
        Epic createdEpic = epics.getFirst();
        Epic expectedEpic = new Epic(epic.getTitle(), epic.getDescription());

        expectedEpic.setId(createdEpic.getId());

        assertEquals(expectedEpic, createdEpic, "Созданный эпик не соответствует ожидаемому");
    }

    // Проверка обновления эпика (код ответа 201)
    @Test
    void testCreateAndUpdateEpic() throws IOException, InterruptedException {
        // Подготовка и создание исходного эпика
        Epic epic = new Epic("Эпик", "Описание эпика");
        String taskJson = gson.toJson(epic);

        // Отправка запроса на создание
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> createResponse = httpClient.send(createRequest, HttpResponse.BodyHandlers.ofString());

        // Проверка создания
        assertEquals(201, createResponse.statusCode(), "Неверный код ответа при создании");

        // Получение созданного эпика
        List<Epic> epics = taskManager.getAllEpics();
        Epic createdEpic = epics.getFirst();

        // Подготовка обновленных данных
        Epic updatedEpicData = new Epic(createdEpic.getId(),
                "Обновленный эпик",
                "Новое описание");

        String updateJson = gson.toJson(updatedEpicData);

        // Отправка запроса на обновление
        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(updateJson))
                .build();

        HttpResponse<String> updateResponse = httpClient.send(updateRequest, HttpResponse.BodyHandlers.ofString());

        // Проверка обновления
        assertEquals(201, updateResponse.statusCode(), "Неверный код ответа при обновлении");

        // Проверка конечного состояния
        Epic finalEpic = taskManager.getEpicById(createdEpic.getId());
        Task expectedUpdatedEpic = new Task(updatedEpicData.getTitle(),
                updatedEpicData.getDescription());

        expectedUpdatedEpic.setId(finalEpic.getId());

        assertEquals(expectedUpdatedEpic, finalEpic, "Обновленный эпик не соответствует ожидаемой");
    }

    // Проверка получения всех эпиков (код ответа 200)
    @Test
    public void testCreateAndGetAllEpics() throws IOException, InterruptedException {
        // Создание исходных эпиков
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        taskManager.createEpic(epic2);

        // Отправка запроса на получение
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Десериализация ответа
        Type taskListType = new TypeToken<List<Epic>>() {
        }.getType();
        List<Epic> epics = gson.fromJson(response.body(), taskListType);

        assertEquals(200, response.statusCode(), "Неверный код ответа при получении всех эпиков");
        assertEquals(2, epics.size(), "Некорректное количество эпиков");
        assertEquals("Эпик 1", epics.get(0).getTitle(), "Некорректное имя эпика");
        assertEquals("Эпик 2", epics.get(1).getTitle(), "Некорректное имя эпика");
    }

    // Проверка получения эпика по ID (код ответа 200)
    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        // Подготовка исходных данных
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        taskManager.createEpic(epic2);

        // Отправка запроса на получение
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic2.getId()))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Десериализация ответа
        Epic epic = gson.fromJson(response.body(), Epic.class);

        assertEquals(200, response.statusCode(), "Неверный код ответа при получении эпика");
        assertEquals(epic2.getId(), epic.getId(), "Эпик должен находиться по ID");
    }

    // Проверка удаления эпика по ID (код ответа 200)
    @Test
    public void testDeleteEpicById() throws IOException, InterruptedException {
        // Подготовка исходных данных
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        taskManager.createEpic(epic2);

        // Отправка запроса на удаление
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic2.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа при удалении эпика");
        assertEquals(1, taskManager.getAllEpics().size(), "Должен остаться один эпик");
        assertNotNull(taskManager.getEpicById(epic1.getId()), "Эпик 1 должен остаться");
    }

    // Проверка, что все эпики удаляются (код успеха 200)
    @Test
    public void testDeleteAllEpics() throws IOException, InterruptedException {
        // Подготовка исходных данных
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        taskManager.createEpic(epic2);

        // Отправка запроса на удаление
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа при удалении всех эпиков");
        assertEquals(0, taskManager.getAllEpics().size(), "Все эпики должны быть удалены");
    }

    // Проверка, что нельзя отправить неподдерживаемый метод (код ответа 405)
    @Test
    public void testNotAllowedMethod() throws IOException, InterruptedException {

        // Отправка запроса с неподдерживаемым методом
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Метод PUT не поддерживается");
    }

    // Проверка запроса на получение несуществующего эпика (код ответа 404)
    @Test
    public void testGetEpicByIdNotFound() throws IOException, InterruptedException {
        // Отправка запроса на получение несуществующего эпика
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    // Проверка отправки некорректного запроса (код ответа 400)
    @Test
    public void testAddEpicEmptyTitle() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        epic1.setTitle(null);

        String taskForRequest = gson.toJson(epic1);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskForRequest))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
    }
}
