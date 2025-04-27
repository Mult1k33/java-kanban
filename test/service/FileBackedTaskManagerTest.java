package service;

import enums.Status;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File file;
    public FileBackedTaskManager manager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    // Создание временного файла и менеджера для записи задач перед каждым тестом
    @BeforeEach
    public void beforeEach() throws IOException {
        file = File.createTempFile("task", ".csv");
        manager = new FileBackedTaskManager(file);
        taskManager = manager;

       try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));) {
           writer.write("id,type,title,description,status,epic\n");
           writer.write(task + "\n");
           writer.write(epic + "\n");
           writer.write(subtask + "\n");
       }
    }

    // Удаление временного файла после каждого теста
    @AfterEach
    public void cleanUp() {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            file = File.createTempFile("tasks", ".csv");
            return new FileBackedTaskManager(file);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл", e);
        }
    }

    /* Проверка, что задачи сохраняются в файл;
    проверка, что файл не пуст;
    проверка, что содержимое файла выводится;
    проверка, что менеджер загрузки работает корректно
     */
    @Test
    public void checkReadFromFileAnyTypes() throws IOException {
        Task task = new Task("Задача", "Описание задачи");
        manager.createTask(task);
        Epic epic = new Epic("Эпик", "Описание эпика");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", epic.getId());
        manager.createSubtask(subtask);

        // Проверка, что задачи всех типов есть в менеджере
        assertEquals(1, manager.getAllTasks().size(), "Должна быть 1 задача в менеджере");
        assertEquals(1, manager.getAllEpics().size(), "Должен быть 1 эпик в менеджере");
        assertEquals(1, manager.getAllSubtasks().size(), "Должна быть 1 подзадача в менеджере");

        // Проверка записи в файл
        assertTrue(file.exists(), "Файл не создан!");
        assertTrue(file.length() > 0, "Файл пустой!");

        // Выводим содержимое файла для отладки
        String fileContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        System.out.println("Содержимое файла:\n" + fileContent);

        // Проверка наличия нужных строк в файле
        assertTrue(fileContent.contains("Задача"), "Файл должен содержать задачу");
        assertTrue(fileContent.contains("Эпик"), "Файл должен содержать эпик");
        assertTrue(fileContent.contains("Подзадача"), "Файл должен содержать подзадачу");

        // Загрузка из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверки
        assertEquals(1, loadedManager.getAllTasks().size(), "Нет задач после загрузки");
        assertEquals(1, loadedManager.getAllEpics().size(), "Нет эпиков после загрузки");
        assertEquals(1, loadedManager.getAllSubtasks().size(), "Нет подзадач после загрузки");
    }

    // Проверки, что задачи всех типов обновляются в менеджере файлов
    @Test
    public void testUpdateTask() {
        Task task = new Task("Выполнить ФЗ-7", "Добавить новый функционал");
        manager.createTask(task);
        task.setStatus(Status.IN_PROGRESS);
        task.setDescription("Исправить замечания ФЗ-7");
        manager.updateTask(task);
        assertEquals("Исправить замечания ФЗ-7", task.getDescription(),
                "Задача должна обновить свое описание!");
        assertEquals(Status.IN_PROGRESS, task.getStatus(),
                "Задача должна обновить статус!");
    }

    @Test
    public void testUpdateEpic() {
        Epic epic = new Epic("Выполнить ФЗ-7", "Добавить новый функционал");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача эпика", "Описание подзадачи", epic.getId());
        manager.createSubtask(subtask);
        epic.setDescription("Исправить замечания ФЗ-7");
        subtask.setStatus(Status.DONE);
        manager.checkEpicStatus(epic.getId());
        assertEquals("Исправить замечания ФЗ-7", epic.getDescription(),
                "Эпик должен обновить свое описание!");
        assertEquals(Status.DONE, epic.getStatus(),
                "Эпик должен обновить статус после обновления статуса подзадачи!");
    }

    @Test
    public void testUpdateSubtask() {
        Epic epic = new Epic("Выполнить ФЗ-7", "Добавить новый функционал");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Юнит-тесты", "Добавить тесты для нового класса", epic.getId());
        manager.createSubtask(subtask);
        subtask.setTitle("Исключения");
        subtask.setDescription("Добавить классы для обработки исключений");
        manager.updateSubtask(subtask);
        assertEquals("Исключения", subtask.getTitle(),
                "Подзадача должна обновить свой заголовок!");
        assertEquals("Добавить классы для обработки исключений", subtask.getDescription(),
                "Подзадача должна обновить свое описание!");
    }
}