package service;

import enums.Status;
import org.junit.jupiter.api.Test;
import model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
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
        assertEquals(1, subtask.getEpicId(), "Подзадачу нельзя сделать своим эпиком!");
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
}