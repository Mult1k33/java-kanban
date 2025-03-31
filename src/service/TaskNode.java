package service;

import model.Task;

// Вспомогательный класс Node для работы с узлами двусвязного списка

public class TaskNode {

    private Task task;
    private TaskNode prev;
    private TaskNode next;

    public TaskNode(Task task, TaskNode prev) {
        this.task = task;
        this.prev = prev;
        this.next = null;
    }

    public TaskNode(Task task) {
        this(task, null);
    }

    public Task getTask() {
        return task;
    }

    public TaskNode getPrev() {
        return prev;
    }

    public void setPrev(TaskNode prev) {
        this.prev = prev;
    }

    public TaskNode getNext() {
        return next;
    }

    public void setNext(TaskNode next) {
        this.next = next;
    }
}