package service.managers;

import java.util.*;
import java.util.List;
import java.util.Map;

import model.Task;
import model.TaskNode;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, TaskNode> historyMap;
    private TaskNode head;
    private TaskNode tail;

    public InMemoryHistoryManager() {
        this.historyMap = new HashMap<>();
        this.head = null;
        this.tail = null;
    }

    // Метод добавляет задачу в список просмотренных задач.
    @Override
    public void add(Task task) {
        int id = task.getId();
        remove(id);

        linkLast(task);
        historyMap.put(id, tail);
    }

    // Метод удаляет задачу из истории просмотров
    @Override
    public void remove(Integer id) {
        if (historyMap.containsKey(id)) {
            removeNode(historyMap.get(id));
            historyMap.remove(id);
        }
    }

    // Метод возвращает список последних просмотренных задач
    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryHistoryManager that = (InMemoryHistoryManager) o;
        return Objects.equals(getTasks(), that.getTasks());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTasks());
    }

    // Метод добавляет задачу в конец списка истории просмотров
    private void linkLast(Task task) {
        if (head == null) {
            head = new TaskNode(task);
            tail = head;
        } else {
            tail.setNext(new TaskNode(task, tail));
            tail = tail.getNext();
        }
    }

    // Метод удаляет узел из двусвязного списка
    private void removeNode(TaskNode taskNode) {
        if (taskNode.getNext() == null && taskNode.getPrev() == null) {
            head = null;
            tail = null;
        } else if (taskNode.getPrev() == null) {
            head = taskNode.getNext();
            head.setPrev(null);
        } else if (taskNode.getNext() == null) {
            tail = taskNode.getPrev();
            tail.setNext(null);
        } else {
            taskNode.getPrev().setNext(taskNode.getNext());
            taskNode.getNext().setPrev(taskNode.getPrev());
        }
    }

    // Метод собирает задачи из двусвязного списка в обычный ArrayList
    private List<Task> getTasks() {
        List<Task> listHistory = new ArrayList<>();
        TaskNode current = head;

        while (current != null) {
            listHistory.add(current.getTask());
            current = current.getNext();
        }
        return listHistory;
    }
}