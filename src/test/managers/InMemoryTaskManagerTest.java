package test.managers;

import enums.Status;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private static TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault();
    }


    //Проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id;
    @Test
    public void addDifferentTasksAndFindById() {
        Task taskTest = new Task(1, "Тест", "Написать тесты для ФЗ", Status.NEW);
        taskManager.createTask(taskTest);
        assertEquals(taskTest, taskManager.getTaskById(1),
                "Задача должна добавиться и находиться по id!");

        Epic epicTest = new Epic(2, "ЭпикТест", "Описание эпиктеста", Status.NEW);
        taskManager.createEpic(epicTest);
        assertEquals(epicTest, taskManager.getEpicById(2),
                "Эпик должен добавиться и находиться по id!");

        Subtask subtaskTest = new Subtask(3, "Подзадача 5",
                "Описание подзадачи 5", Status.NEW, epicTest.getId());
        taskManager.createSubtask(subtaskTest);
        assertEquals(subtaskTest, taskManager.getSubtaskById(3),
                "Подзадача должна добавить и находиться по id!");
    }


    //Проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    @Test
    public void checkСonflictSpecifiedIdAndGeneratedIdTasks() {
        Task taskTest2 = new Task(4, "Задача с заданным id", "Описание задачи", Status.NEW);
        taskManager.createTask(taskTest2);
        taskManager.getTaskById(4);
        assertNotNull(taskTest2, "Задача не найдена!");

        Task taskTest3 = new Task("Задача с сгенерированным id", "Описание задачи");
        taskManager.createTask(taskTest3);
        taskManager.getTaskById(5);
        assertNotNull(taskTest3, "Задача не найдена!");

        assertNotEquals(taskTest2.getId(), taskTest3.getId(),
                "Задачи с заданным и сгенерированым id не должны конфликтовать!");
    }

    @Test
    public void checkСonflictSpecifiedIdAndGeneratedIdEpics() {
        Epic epicTest2 = new Epic(6, "Эпик с заданным id", "Описание эпика", Status.NEW);
        taskManager.createEpic(epicTest2);
        taskManager.getEpicById(6);
        assertNotNull(epicTest2, "Эпик не найден!");

        Epic epicTest3 = new Epic("Эпик с сгенерированным id", "Описание эпика");
        taskManager.createEpic(epicTest3);
        taskManager.getEpicById(7);
        assertNotNull(epicTest3, "Эпик не найден!");

        assertNotEquals(epicTest2.getId(), epicTest3.getId(),
                "Эпики с заданным и сгенерированным id не должны конфликтовать!");
    }

    @Test
    public void checkСonflictSpecifiedIdAndGeneratedIdSubtasks() {
        Epic epicTest4 = new Epic("Эпик с подздачами", "Описание эпика");
        taskManager.createEpic(epicTest4);

        Subtask subtaskTest2 = new Subtask(8, "Подзадача с заданным id",
                "Описание подзадачи", Status.NEW, epicTest4.getId());
        taskManager.createSubtask(subtaskTest2);
        taskManager.getSubtaskById(8);
        assertNotNull(subtaskTest2, "Подзадача не найдена!");

        Subtask subtaskTest3 = new Subtask("Подзадача с сгенерированным id",
                "Описание подзадачи", epicTest4.getId());
        taskManager.createSubtask(subtaskTest3);
        taskManager.getSubtaskById(9);
        assertNotNull(subtaskTest3, "Подзадача не найдена!");

        assertNotEquals(taskManager.getSubtaskByEpic(epicTest4), subtaskTest3.getId(),
                "Подзадачи с заданным и сгенерированным id не должны конфликтовать!");
    }


    //Проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    @Test
    public void checkTaskForImmutability() {
        Task task = new Task("Новая задача", "Описание новой задачи");
        taskManager.createTask(task);
        Task newTask = taskManager.getTaskById(1);
        assertEquals(task.getId(), newTask.getId(), "id задачи не должен меняться!");
        assertEquals(task.getTitle(), newTask.getTitle(), "Название задачи не должно меняться!");
        assertEquals(task.getDescription(), newTask.getDescription(), "Описание задачи не должно меняться!");
        assertEquals(task.getStatus(), newTask.getStatus(), "Статус задачи не должен меняться!");
    }

    @Test
    public void checkEpicForImmutability() {
        Epic epic = new Epic(1, "Новый эпик", "Описание нового эпика", Status.NEW);
        taskManager.createEpic(epic);
        Epic newEpic = taskManager.getEpicById(1);
        assertEquals(epic.getId(), newEpic.getId(), "id эпика не должен меняться!");
        assertEquals(epic.getTitle(), newEpic.getTitle(), "Название эпика не должно меняться!");
        assertEquals(epic.getDescription(), newEpic.getDescription(), "Описание эпика не должно меняться!");
        assertEquals(epic.getStatus(), newEpic.getStatus(), "Статус эпика не должен меняться!");
    }

    @Test
    public void checkSubtaskForImmutability() {
        Epic epicForSubtasks = new Epic("Эпик с подзадачами", "Описание эпика");
        taskManager.createEpic(epicForSubtasks);
        Subtask subtask = new Subtask(2, "Подзадача для эпика",
                "Описание подзадачи", Status.NEW, epicForSubtasks.getId());
        taskManager.createSubtask(subtask);
        Subtask newSubtask = taskManager.getSubtaskById(2);
        assertEquals(subtask.getId(), newSubtask.getId(), "id подзадачи не должен меняться!");
        assertEquals(subtask.getTitle(), newSubtask.getTitle(), "Название подзадачи не должна меняться!");
        assertEquals(subtask.getDescription(), newSubtask.getDescription(),
                "Описание подзадачи не должно меняться!");
        assertEquals(subtask.getStatus(), newSubtask.getStatus(), "Статус задачи не должен меняться!");
    }
}