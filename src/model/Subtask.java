package model;

import enums.Status;
import enums.TaskType;

public class Subtask extends Task {

    private Integer epicId;

    public Subtask(Integer id, String title, String description, Status status, Integer epicId) {
        super(id, title, description, status);
        this.epicId = epicId;
    }

    public Subtask(String title, String description, Integer epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(Integer id, String title, String description, Integer epicId) {
        super(id, title, description);
        this.epicId = epicId;
    }


    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicId) {
        if (epicId != this.getId()) {
            this.epicId = epicId;
        }
    }

    public TaskType getTaskType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return String.join(",",
                getId().toString(),
                TaskType.SUBTASK.toString(),
                getTitle(),
                getStatus().toString(),
                getDescription(),
                getEpicId().toString());
    }
}