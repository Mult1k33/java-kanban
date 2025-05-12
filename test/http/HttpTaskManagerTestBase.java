package http;

import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import service.HttpTaskServer;
import service.managers.*;
import com.google.gson.Gson;
import service.typeadapters.DurationAdapter;
import service.typeadapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;


public class HttpTaskManagerTestBase {
    protected TaskManager taskManager;
    protected HttpTaskServer taskServer;
    protected HttpClient httpClient;
    protected Gson gson;
    protected static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() throws IOException {

        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        taskServer.start();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        httpClient = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }
}
