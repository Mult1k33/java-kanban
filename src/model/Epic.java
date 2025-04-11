package model;

import enums.Status;
import enums.TaskType;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<Integer> subtasksId = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
        this.subtasksId = new ArrayList<>();
    }

    public Epic(Integer id, String title, String description, Status status) {
        super(id, title, description, status);
        this.subtasksId = new ArrayList<>();
    }

    public Epic(Integer id, String title, String description) {
        super(id, title, description);
        this.subtasksId = new ArrayList<>();
    }

    public void addSubtask(Integer subtaskId) {
        if (subtaskId != this.getId()) {
            subtasksId.add(subtaskId);
        }
    }

    public List<Integer> getSubtasksId() {
        return subtasksId;
    }

    public void clearSubtasksId() {
        subtasksId.clear();
    }

    public void deleteSubtaskById(Integer id) {
        subtasksId.remove(id);
    }

    public void setSubtasksId(List<Integer> subtasksId) {
        this.subtasksId = subtasksId;
    }

    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return String.join(",",
                getId().toString(),
                TaskType.EPIC.toString(),
                getTitle(),
                getStatus().toString(),
                getDescription(),
                "");
    }
}