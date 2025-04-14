package service;

import enums.Status;
import model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    private File file;
    public FileBackedTaskManager manager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    // Создание временного файла и менеджера для записи задач перед каждым тестом
    @BeforeEach
    public void beforeEach() throws IOException {
        file = File.createTempFile("task",".csv");
        manager = new FileBackedTaskManager(file);

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        writer.write("id,type,name,status,description,epic\n");
        writer.write(task + "\n");
        writer.write(epic + "\n");
        writer.write(subtask + "\n");
        writer.close();
    }

    // Удаление временного файла после каждого теста
    @AfterEach
    public void cleanUp() {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    // Проверки, что задачи, эпики и подзадачи создаются в менеджере файлов
    @Test
    public void testCreateTasksAllType() throws Exception {
        Task task = new Task("Задача", "Описание задачи");
        manager.createTask(task);
        assertEquals(task, manager.getTaskById(1), "Задача должна быть создана и находиться по id!");

        Epic epic = new Epic("Эпик", "Описание эпика");
        manager.createEpic(epic);
        assertEquals(epic, manager.getEpicById(2), "Эпик должен быть создан и находиться по id!");

        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", 2);
        manager.createSubtask(subtask);
        assertFalse(manager.getSubtaskByEpic(epic.getId()).isEmpty(),
                "Подзадача должна находить по id своего эпика!");
        assertEquals(subtask, manager.getSubtaskById(3),
                "Подзадача должна быть создана и находиться по id!");
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

    // Проверки, что задачи всех типов удаляются по id
    @Test
    public void testDeleteTask() {
        Task task = new Task("Задача для удаления", "Описание задачи");
        manager.createTask(task);
        manager.deleteTask(task.getId());
        assertFalse(manager.getAllTasks().contains(task), "Задача должна быть удалена!");
    }

    @Test
    public void testDeleteEpic() {
        Epic epic = new Epic("Эпик для удаления", "Описание эпика");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", epic.getId());
        manager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", epic.getId());
        manager.createSubtask(subtask2);

        // Проверка, что эпик и подзадачи созданы
        assertEquals(epic, manager.getEpicById(epic.getId()), "Эпик должен создаться и находить по id!");
        assertEquals(2, manager.getAllSubtasks().size(), "У эпика должно быть 2 подзадачи!");

        // Проверка, что эпик удалился
        manager.deleteEpic(epic.getId());
        assertFalse(manager.getAllEpics().contains(epic), "Эпик должен быть удален!");
        assertNull(manager.getEpicById(epic.getId()), "Эпик должен быть удален и не может найтись по id!");

        // Проверка, что подзадачи эпика тоже удалены
        assertTrue(manager.getAllSubtasks().isEmpty(), "Подзадачи эпика должны быть удалены!");
        assertNull(manager.getSubtaskById(subtask1.getId()), "Подзадача 1 должна быть удалена");
        assertNull(manager.getSubtaskById(subtask2.getId()), "Подзадача 2 должна быть удалена");
    }

    // Проверка, что все задачи удаляются сразу
    @Test
    public void testDeleteAllTasks() {
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        manager.createTask(task1);
        Task task2 = new Task("Задача 2", "Описание задачи 2");
        manager.createTask(task2);
        Task task3 = new Task("Задача 3", "Описание задачи 3");
        manager.createTask(task3);

        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty(), "Все подзадачи должны быть удалены!");
    }

    @Test
    public void testDeleteAllEpics() {
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        manager.createEpic(epic1);
        Epic epic2 = new Epic("Эпик с подзадачами", "Описание эпика");
        manager.createEpic(epic2);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", epic2.getId());
        manager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", epic2.getId());
        manager.createSubtask(subtask2);

        manager.deleteAllEpics();
        assertTrue(manager.getAllEpics().isEmpty(), "Все эпики должны быть удалены!");
        assertTrue(manager.getAllSubtasks().isEmpty(), "Подзадачи эпика должны быть удалены!");
        assertNull(manager.getSubtaskById(subtask1.getId()), "Подзадача 1 должна быть удалена");
        assertNull(manager.getSubtaskById(subtask2.getId()), "Подзадача 2 должна быть удалена");
    }

    @Test
    public void testDeleteAllSubtasks() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", epic.getId());
        manager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", epic.getId());
        manager.createSubtask(subtask2);

        manager.deleteAllSubtasks();
        assertTrue(manager.getAllSubtasks().isEmpty(), "Все подзадачи должны быть удалены!");
        assertEquals(epic, manager.getEpicById(1),
                "Эпик не должен быть удален после удаления всех подзадач!");
    }
}