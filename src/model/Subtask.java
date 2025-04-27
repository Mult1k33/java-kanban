package model;

import enums.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    public Subtask(String title, String description, LocalDateTime startTime, Duration duration, Integer epicId) {
        super(title, description, startTime, duration);
        this.setStatus(Status.NEW);
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
                getDescription() != null ? getDescription() : "",
                getStatus() != null ? getStatus().toString() : Status.NEW.toString(),
                getEpicId().toString(),
                getStartTime() != null ? getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                getDuration() != null ? String.valueOf(getDuration().toMinutes()) : "0");
    }
}