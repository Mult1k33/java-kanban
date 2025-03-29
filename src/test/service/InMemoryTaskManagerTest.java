package test.service;

import enums.Status;
import service.Managers;
import service.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private static TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault();
    }

    //Проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи;
    @Test
    public void epicCannotAddItselfAsASubtask() {
        Epic epic = new Epic(1, "ЭпикТест 1", "Описание эпикТеста 1", Status.NEW);
        taskManager.createEpic(epic);
        epic.addSubtask(epic.getId());
        assertFalse(epic.getSubtasksId().contains(epic.getId()),
                "Эпик нельзя добавить в самого себя в виде подзадачи!");
    }

    // Проверьте, что объект Subtask не может быть своим же эпиком
    @Test
    void subtaskCannotBeItsOwnEpic() {
        Epic epic = new Epic(1, "ЭпикТест 2", "Описание эпикТеста 2", Status.NEW);
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask(1, "Подзадача 1", "Описание подзадачи 1", Status.NEW, 1);
        taskManager.createSubtask(subtask);
        subtask.setEpicId(subtask.getId()); // Устанавливаем ID подзадачи как ID эпика
        assertEquals(1,subtask.getEpicId(), "Подзадачу нельзя сделать своим эпиком!");
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
                "Подзадача должна добавиться и находиться по id!");
    }

    //Проверьте, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    @Test
    public void checkСonflictSpecifiedIdAndGeneratedIdTasks() {
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
    public void checkСonflictSpecifiedIdAndGeneratedIdEpics() {
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
    public void checkСonflictSpecifiedIdAndGeneratedIdSubtasks() {
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

    // Проверка, что задачи создаются и добавляются в менеджер
    @Test
    public void checkAddNewTask() {
        Task testTask = new Task("Тестовая задача", "Описание тестовой задачи");
        taskManager.createTask(testTask);
        assertEquals(1, taskManager.getAllTasks().size(), "Ожидался список из 1 элемента");
        assertTrue(taskManager.getAllTasks().contains(testTask), "Добавленная задача не найдена!");
    }

    @Test
    public void checkAddNewEpic() {
        Epic testEpic = new Epic(1,"Тестовый эпик", "Описание тестового эпика",Status.IN_PROGRESS);
        taskManager.createEpic(testEpic);
        assertEquals(1, taskManager.getAllEpics().size(), "Ожидался список из 1 элемента");
        assertTrue(taskManager.getAllEpics().contains(testEpic), "Добавленный эпик не найден!");
    }

    @Test
    public void checkAddNewSubTask() {
        Epic testEpic = new Epic("Тестовый эпик", "Описание тестового эпика");
        taskManager.createEpic(testEpic);
        Subtask testSubtask = new Subtask("Тестовая подзадача", "Описание подзадачи", testEpic.getId());
        taskManager.createSubtask(testSubtask);
        assertEquals(1, taskManager.getAllSubtasks().size(), "Ожидался список из 1 элементов");
        assertTrue(taskManager.getAllSubtasks().contains(testSubtask), "Добавленный эпик не найден!");
        List<Integer> subtaskList = taskManager.getEpicById(testEpic.getId()).getSubtasksId();
        assertEquals(1, subtaskList.size(), "Ожидался список из 1 элемента");
        assertTrue(subtaskList.contains(testSubtask.getId()), "Добавленная подзадача не найдена!");
    }

    // Проверка, что задачи, эпики и подзадачи удаляются корректно
    @Test
    public void checkDeleteTaskById() {
        Task taskForDelete = new Task("Задача 1", "Задача для удаления");
        taskManager.createTask(taskForDelete);
        taskManager.deleteTask(taskForDelete.getId());
        assertEquals(0, taskManager.getAllTasks().size(), "Список должен быть пуст!");
        assertNull(taskManager.getTaskById(taskForDelete.getId()), "Задача должна быть удалена!");
    }

    @Test
    public void checkDeleteEpicById() {
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Epic epicForDelete = new Epic("Эпик 2", "Эпик для удаления");
        taskManager.createEpic(epic1);
        taskManager.createEpic(epicForDelete);
        taskManager.deleteEpic(epicForDelete.getId());
        assertEquals(1, taskManager.getAllEpics().size(), "Ожидался список из 1 эпика");
        assertNull(taskManager.getEpicById(epicForDelete.getId()), "Эпик должен быть удален!");
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
        assertNull(taskManager.getSubtaskById(subtaskForDelete.getId()), "Подзадача 2 должна быть удалена!");
    }

    // Убедитесь, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    public void getHistoryShouldReturnOldTaskAfterUpdate() {
        Task task = new Task("Задача 1", "Описание задачи 1");
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId());
        taskManager.updateTask(new Task(task.getId(), "Обновить задачу 1",
                "Описание обновленной задачи", Status.IN_PROGRESS));
        List<Task> tasks = taskManager.getHistory();
        Task oldTask = tasks.getFirst();
        assertEquals(task.getTitle(), oldTask.getTitle(),
                "В истории не сохранилась старая версия задачи!");
        assertEquals(task.getDescription(), oldTask.getDescription(),
                "В истории не сохранилась старая версия задачи!");
    }

    @Test
    public void getHistoryShouldReturnOldEpicAfterUpdate() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic);
        taskManager.getEpicById(epic.getId());
        taskManager.updateEpic(new Epic(epic.getId(), "Обновить эпик 1",
                "Описание обновленного эпика", Status.DONE));
        List<Task> epics = taskManager.getHistory();
        Epic oldEpic = (Epic) epics.getFirst();
        assertEquals(epic.getTitle(), oldEpic.getTitle(),
                "В истории не сохранилась старая версия эпика!");
        assertEquals(epic.getDescription(), oldEpic.getDescription(),
                "В истории не сохранилась старая версия эпика!");
    }

    @Test
    public void getHistoryShouldReturnOldSubtaskAfterUpdate() {
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        taskManager.createEpic(epic2);
        Subtask subtask = new Subtask("Подзадача эпика 2",
                "Описание подзадачи", epic2.getId());
        taskManager.createSubtask(subtask);
        taskManager.getSubtaskById(subtask.getId());
        taskManager.updateSubtask(new Subtask(subtask.getId(), "Обновить подзадачу",
                "Описание обновленной подзадачи", Status.IN_PROGRESS, epic2.getId()));
        List<Task> subtasks = taskManager.getHistory();
        Subtask oldSubtask = (Subtask) subtasks.getFirst();
        assertEquals(subtask.getTitle(), oldSubtask.getTitle(),
                "В истории не сохранилась старая версия подзадачи!");
        assertEquals(subtask.getDescription(), oldSubtask.getDescription(),
                "В истории не сохранилась старая версия подзадачи!");
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