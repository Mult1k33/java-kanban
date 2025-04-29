package model;

import enums.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

public class Epic extends Task {

    private List<Integer> subtasksId = new ArrayList<>();
    private LocalDateTime endTime;

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

    public Epic(String title, String description, LocalDateTime startTime, Duration duration) {
        super(title, description, startTime, duration);
        this.subtasksId = new ArrayList<>();
    }

    public void addSubtask(Integer subtaskId) {
        if (subtaskId == null || subtaskId.equals(this.getId())) {
            return;
        }
        if (!subtasksId.contains(subtaskId)) {
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

    public void deleteAllSubtasks() {
        subtasksId.clear();
    }

    public void setSubtasksId(List<Integer> subtasksId) {
        this.subtasksId = subtasksId;
    }

    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    public LocalDateTime getEndTime() {
        return this.endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd|HH:mm");
        return String.join(",",
                getId().toString(),
                TaskType.EPIC.toString(),
                getTitle(),
                getDescription() != null ? getDescription() : "",
                getStatus() != null ? getStatus().toString() : Status.NEW.toString(),
                "",
                getStartTime() != null ? getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                getDuration() != null ? String.valueOf(getDuration().toMinutes()) : "0");
    }

    public static Epic parseEpicFromString(String[] words) {
        Epic epic = new Epic(
                Integer.parseInt(words[0]),
                words[2],
                words[3]
        );
        epic.setStatus(Status.valueOf(words[4]));
        epic.setStartTime(Epic.parseDateTime(words[6]));
        epic.setDuration(Epic.parseDuration(words[7]));
        return epic;
    }
}