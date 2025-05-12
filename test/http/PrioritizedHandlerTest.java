package http;

import com.google.gson.reflect.TypeToken;
import enums.Status;
import model.Task;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrioritizedHandlerTest extends HttpTaskManagerTestBase {

    // Проверка получения приоритетных задач (код ответа 200)
    @Test
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        // Подготовка данных для теста
        Task task = new Task("Приоритетная задача", "Описание задачи 1", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(30));
        taskManager.createTask(task);
        Task task2 = new Task("Обычная задача", "Описание");
        taskManager.createTask(task2);

        // Отправка запроса на получение
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Неверный код ответа");
        List<Task> prioritized = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        assertEquals(1, prioritized.size(), "Передана не корректная история");
        assertEquals("Приоритетная задача", prioritized.getFirst().getTitle(),
                "Название приоритетной задачи не совпадает");
    }

    // Проверка, что нельзя отправить неподдерживаемый метод (код ответа 405)
    @Test
    public void testNotAllowedMethod() throws IOException, InterruptedException {
        // Отправка запроса с неподдерживаемым методом
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prioritized"))
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Метод PUT не поддерживается");
    }
}
