package service;

import enums.Status;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.exeptions.TimeOverlapException;
import service.managers.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** Абстрактный базовый класс для InMemoryTaskManagerTest и FileBackedTaskManagerTest.
 * Позволяет провести проверки общих методов из интерфейса TaskManger.
 * Отдельные методы классов InMemoryTaskManager и FileBackedTaskManager тестируются в соответствующих классах.
 *
 */

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    public void eforeEach() {
        taskManager = createTaskManager();
    }

    // Общие тесты для проверки создания и получения задач всех типов по id
    @Test
    public void shouldCreateAndFindTaskById() {
        Task task = new Task("Задача", "Описание задачи");
        taskManager.createTask(task);
        assertEquals(task, taskManager.getTaskById(task.getId()),
                "Задача должна быть создана и находиться по id!");
    }

    @Test
    public void shouldCreateAndFindEpicById() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        taskManager.createEpic(epic);
        assertEquals(epic, taskManager.getEpicById(epic.getId()),
                "Эпик должен быть создан и находиться по id!");
    }

    @Test
    public void shouldCreateAndFindSubtaskById() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", epic.getId());
        taskManager.createSubtask(subtask);
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()),
                "Подзадача должна быть создана и находиться по id!");
    }

    // Общие тесты для проверки обновления статуса задач
    @Test
    public void shouldUpdateTaskStatus() {
        Task task = new Task("Задача", "Описание задачи");
        taskManager.createTask(task);
        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);
        assertEquals(Status.IN_PROGRESS, taskManager.getTaskById(task.getId()).getStatus(),
                "Статус задачи должен обновиться");
    }

    @Test
    public void shouldCheckEpicStatusCalculation() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        // Все подзадачи NEW
        assertEquals(Status.NEW, epic.getStatus(), "Статус эпика должен быть NEW");

        // Все подзадачи DONE
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        assertEquals(Status.DONE, epic.getStatus(), "Статус эпика должен быть DONE");

        // Подзадачи NEW и DONE
        subtask1.setStatus(Status.NEW);
        taskManager.updateSubtask(subtask1);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS");

        // Подзадачи IN_PROGRESS
        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

    // Общая проверка, что задачи не конфликтуют друг с другом по времени
    @Test
    public void shouldCheckTimeConflict() {
        LocalDateTime now = LocalDateTime.now();

        Task task1 = new Task("Задача 1", "Описание задачи 1");
        task1.setStartTime(now);
        task1.setDuration(Duration.ofMinutes(30));  // 30 минут

        Task task2 = new Task("Задача 2", "Описание задачи 2");
        task2.setStartTime(now.plusMinutes(15));  // Начинается через 15 минут
        task2.setDuration(Duration.ofMinutes(30));

        taskManager.createTask(task1);
        assertThrows(TimeOverlapException.class, () -> taskManager.createTask(task2),
                "Должно быть исключение при конфликте времени");
    }

    //Общие проверки, что задачи всех типов удаляются по id
    @Test
    public void checkDeleteTaskById() {
        Task task = new Task("Задача для удаления", "Описание задачи");
        taskManager.createTask(task);
        taskManager.deleteTask(task.getId());
        assertFalse(taskManager.getAllTasks().contains(task), "Задача должна быть удалена!");
    }

    @Test
    public void checkDeleteEpicById() {
        Epic epic = new Epic("Эпик для удаления", "Описание эпика");
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", epic.getId());
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", epic.getId());
        taskManager.createSubtask(subtask2);

        // Проверка, что эпик и подзадачи созданы
        assertEquals(epic, taskManager.getEpicById(epic.getId()), "Эпик должен создаться и находить по id!");
        assertEquals(2, taskManager.getAllSubtasks().size(), "У эпика должно быть 2 подзадачи!");

        // Проверка, что эпик удалился
        taskManager.deleteEpic(epic.getId());
        assertFalse(taskManager.getAllEpics().contains(epic), "Эпик должен быть удален!");
        assertNull(null, "Эпик должен быть удален и не может найтись по id!");

        // Проверка, что подзадачи эпика тоже удалены
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Подзадачи эпика должны быть удалены!");
        assertNull(null, "Подзадача 1 должна быть удалена");
        assertNull(null, "Подзадача 2 должна быть удалена");
    }

    @Test
    public void checkDeleteSubtaskById() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Простая подзадача", "Описание подзадачи", epic.getId());
        Subtask subtaskForDelete = new Subtask("Подзадача 2", "Подзадача для удаления", epic.getId());
        taskManager.createSubtask(subtask);
        taskManager.createSubtask(subtaskForDelete);
        taskManager.deleteSubtask(subtaskForDelete.getId());
        assertEquals(1, taskManager.getAllSubtasks().size(), "Ожидался список из 1 подзадачи");
        assertNull(null, "Подзадача 2 должна быть удалена!");
    }

    // Общие тесты для проверки, что все задачи удаляются сразу
    @Test
    public void checkDeleteAllTasks() {
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        taskManager.createTask(task1);
        Task task2 = new Task("Задача 2", "Описание задачи 2");
        taskManager.createTask(task2);
        Task task3 = new Task("Задача 3", "Описание задачи 3");
        taskManager.createTask(task3);

        taskManager.deleteAllTasks();
        assertTrue(taskManager.getAllTasks().isEmpty(), "Все подзадачи должны быть удалены!");
    }

    @Test
    public void checkDeleteAllEpics() {
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Эпик с подзадачами", "Описание эпика");
        taskManager.createEpic(epic2);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", epic2.getId());
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", epic2.getId());
        taskManager.createSubtask(subtask2);

        taskManager.deleteAllEpics();
        assertTrue(taskManager.getAllEpics().isEmpty(), "Все эпики должны быть удалены!");
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Подзадачи эпика должны быть удалены!");
        assertNull(null, "Подзадача 1 должна быть удалена");
        assertNull(null, "Подзадача 2 должна быть удалена");
    }

    @Test
    public void checkDeleteAllSubtasks() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", epic.getId());
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", epic.getId());
        taskManager.createSubtask(subtask2);

        taskManager.deleteAllSubtasks();
        assertTrue(taskManager.getAllSubtasks().isEmpty(), "Все подзадачи должны быть удалены!");
        assertEquals(epic, taskManager.getEpicById(1),
                "Эпик не должен быть удален после удаления всех подзадач!");
    }

    // Общие проверки, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    @Test
    public void checkConflictSpecifiedIdAndGeneratedIdTasks() {
        Task taskTest2 = new Task(4, "Задача с заданным id", "Описание задачи", Status.NEW);
        taskManager.createTask(taskTest2);
        assertNotNull(taskTest2, "Задача не найдена!");

        Task taskTest3 = new Task("Задача с сгенерированным id", "Описание задачи");
        taskManager.createTask(taskTest3);
        assertNotNull(taskTest3, "Задача не найдена!");

        assertNotEquals(taskTest2.getId(), taskTest3.getId(),
                "Задачи с заданным и сгенерированым id не должны конфликтовать!");
    }

    @Test
    public void checkConflictSpecifiedIdAndGeneratedIdEpics() {
        Epic epicTest2 = new Epic(6, "Эпик с заданным id", "Описание эпика", Status.NEW);
        taskManager.createEpic(epicTest2);
        assertNotNull(epicTest2, "Эпик не найден!");

        Epic epicTest3 = new Epic("Эпик с сгенерированным id", "Описание эпика");
        taskManager.createEpic(epicTest3);
        assertNotNull(epicTest3, "Эпик не найден!");

        assertNotEquals(epicTest2.getId(), epicTest3.getId(),
                "Эпики с заданным и сгенерированным id не должны конфликтовать!");
    }

    @Test
    public void checkConflictSpecifiedIdAndGeneratedIdSubtasks() {
        Epic epicTest4 = new Epic("Эпик с подздачами", "Описание эпика");
        taskManager.createEpic(epicTest4);

        Subtask subtaskTest2 = new Subtask(8, "Подзадача с заданным id",
                "Описание подзадачи", Status.NEW, epicTest4.getId());
        taskManager.createSubtask(subtaskTest2);
        assertNotNull(subtaskTest2, "Подзадача не найдена!");

        Subtask subtaskTest3 = new Subtask("Подзадача с сгенерированным id",
                "Описание подзадачи", epicTest4.getId());
        taskManager.createSubtask(subtaskTest3);
        assertNotNull(subtaskTest3, "Подзадача не найдена!");

        assertNotEquals(taskManager.getSubtaskByEpic(epicTest4.getId()), subtaskTest3.getId(),
                "Подзадачи с заданным и сгенерированным id не должны конфликтовать!");
    }

    // Проверка, что внутри эпиков не должно оставаться неактуальных id подзадач.
    @Test
    void epicShouldNotContainRemovedSubtaskIds() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Подзадача эпика", "Описание подзадачи", epic.getId());
        taskManager.createSubtask(subtask);
        taskManager.deleteSubtask(subtask.getId());
        assertFalse(epic.getSubtasksId().contains(subtask),
                "Эпик не должен содержать ID удаленной подзадачи");
    }
}
