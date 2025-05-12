package http;

import service.HttpTaskServer;
import service.managers.Managers;
import service.managers.TaskManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskServerTest {

    @Test
    public void checkCreateHttpTaskServer() throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(taskManager);

        assertNotNull(server, "Сервер должен был быть проинициализированным");
    }
}
