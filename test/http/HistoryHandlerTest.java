package http;

import com.google.gson.reflect.TypeToken;
import model.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HistoryHandlerTest extends HttpTaskManagerTestBase {

    // Проверка получения истории (код ответа 200)
    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        // Подготовка исходных данных
        Task task = new Task("Задача для истории", "Описание задачи");
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId());

        // Отправка запроса
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/history"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        assertEquals(1, history.size(), "Передана не корректная история");
        assertEquals("Задача для истории", history.getFirst().getTitle(),
                "Название задачи из истории не совпадает");
    }

    // Проверка, что нельзя отправить неподдерживаемый метод (код ответа 405)
    @Test
    public void testNotAllowedMethod() throws IOException, InterruptedException {
        // Отправка запроса с неподдерживаемым методом
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/history"))
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Метод PUT не поддерживается");
    }
}
