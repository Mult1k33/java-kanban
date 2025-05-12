package service;

import enums.Status;
import org.junit.jupiter.api.*;
import model.*;
import service.managers.InMemoryHistoryManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private static InMemoryHistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = new InMemoryHistoryManager();
    }

    // Проверка добавления задач в историю
    @Test
    public void shouldAddTasksToHistory() {
        Task task1 = new Task(1, "Задача 1", "Описание задачи 1", Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание задачи 2", Status.IN_PROGRESS);
        historyManager.add(task1);
        historyManager.add(task2);
        List<Task> historyList = historyManager.getHistory();
        assertEquals(2, historyList.size(), "Список должен состоять из 2 задач!");
        assertEquals(task1, historyList.get(0), "Задача 1 не была добавлена в историю!");
        assertEquals(task2, historyList.get(1), "Задача 2 не была добавлена в историю!");
    }

    // Проверка удаления задачи из истории
    @Test
    public void shouldRemoveTaskFromHistory() {
        Task task = new Task(1, "Задача для удаления", "Описание задачи", Status.NEW);
        historyManager.add(task);
        historyManager.remove(1);
        assertTrue(historyManager.getHistory().isEmpty(), "Список истории должен быть пуст!");
    }

    // Проверка порядка задач в истории
    @Test
    public void shouldMaintainInsertionOrder() {
        Task task1 = new Task(1, "Задача 1", "Описание задачи 1", Status.NEW);
        Epic epic1 = new Epic(2, "Эпик", "Описание эпика", Status.IN_PROGRESS);
        Task task2 = new Task(3, "Задача", "Описание задачи 2", Status.DONE);
        historyManager.add(task1);
        historyManager.add(epic1);
        historyManager.add(task2);
        List<Task> historyList = historyManager.getHistory();
        assertEquals(List.of(task1, epic1, task2), historyList, "Порядок в истории сохранен некорректно!");
    }

    // Проверка обновления истории при повторном просмотре
    @Test
    public void shouldMoveTaskToEndWhenReadded() {
        Task task1 = new Task(1, "Задача 1", "Описание задачи 1", Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание задачи 2", Status.IN_PROGRESS);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);
        List<Task> historyList = historyManager.getHistory();
        assertEquals(2, historyList.size(), "Задача 1 не может быть повторно добавлена!");
        assertEquals(task2, historyList.get(0), "Задача 2 должна быть в начале списка!");
        assertEquals(task1, historyList.get(1),
                "Задача 1 должна быть в конце списка после повторного добавления!");
    }

    // Проверка, что история пустая
    @Test
    public void shouldHandleEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой при инициализации");
    }

    // Проверка, что история не содержит дубликатов
    @Test
    public void shouldRemoveDuplicates() {
        Task task1 = new Task(1, "Задача 1", "Описание задачи 1", Status.NEW);
        historyManager.add(task1);
        historyManager.add(task1);
        assertEquals(1, historyManager.getHistory().size(), "История не должна содержать дубликатов");
    }

    // Проверка удаления из начала истории
    @Test
    public void shouldRemoveFromBeginning() {
        Task task1 = new Task(1, "Задача 1", "Описание задачи 1", Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание задачи 2", Status.IN_PROGRESS);
        Task task3 = new Task(3, "Задача 3", "Описание задачи 3", Status.DONE);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(1);
        List<Task> historyList = historyManager.getHistory();
        assertEquals(2, historyList.size(), "В истории должно быть 2 элемента!");
        assertEquals(task2, historyList.get(0), "Задача 2 должна быть в начале списка!");
        assertEquals(task3, historyList.get(1), "Задача 3 должна сместиться в истории!");
    }

    //Проверка удаления из середины истории
    @Test
    public void shouldRemoveTaskFromMiddle() {
        Task task1 = new Task(1, "Задача 1", "Описание задачи 1", Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание задачи 2", Status.IN_PROGRESS);
        Task task3 = new Task(3, "Задача 3", "Описание задачи 3", Status.DONE);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(2);
        List<Task> historyList = historyManager.getHistory();
        assertEquals(2, historyList.size(), "В истории должно быть 2 элемента!");
        assertEquals(task1, historyList.get(0), "Задача 1 должна быть в начале списка!");
        assertEquals(task3, historyList.get(1), "Задача 3 должна сместиться в истории!");
    }

    // Проверка удаления из конца истории
    @Test
    public void shouldRemoveTaskFromEnd() {
        Task task1 = new Task(1, "Задача 1", "Описание задачи 1", Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание задачи 2", Status.IN_PROGRESS);
        Task task3 = new Task(3, "Задача 3", "Описание задачи 3", Status.DONE);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(3);
        List<Task> historyList = historyManager.getHistory();
        assertEquals(2, historyList.size(), "В истории должно быть 2 элемента!");
        assertEquals(task1, historyList.get(0), "Задача 1 должна быть в начале списка!");
        assertEquals(task2, historyList.get(1), "Задача 3 должна сместиться в истории!");
    }

    // Проверка граничных случаев удаления
    @Test
    public void shouldRemoveFirstAndLastElements() {
        Task task1 = new Task(1, "Задача 1", "Описание задачи 1", Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание задачи 2", Status.IN_PROGRESS);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);
        assertEquals(1, historyManager.getHistory().size(), "В истории должен остаться 1 элемент!");
        assertEquals(task2, historyManager.getHistory().get(0), "Задача 2 должна сместиться в истории!");
        historyManager.remove(2);
        assertTrue(historyManager.getHistory().isEmpty(), "Список истории должен быть пуст!");
    }

    // Проверка получения истории просмотров
    @Test
    public void checkGetHistory() {
        Epic epic1 = new Epic(2, "Эпик", "Описание эпика", Status.IN_PROGRESS);
        Task task1 = new Task(1, "Задача 1", "Описание задачи 1", Status.NEW);
        historyManager.add(epic1);
        historyManager.add(task1);
        List<Task> historyList = historyManager.getHistory();
        assertEquals(2, historyList.size(), "В истории должно быть 2 элемента");
        assertTrue(historyList.contains(epic1), "Эпик отсутствует в истории");
        assertTrue(historyList.contains(task1), "Задача отсутствует в истории");
    }
}