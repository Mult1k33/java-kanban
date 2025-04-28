package model;

import enums.*;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.time.LocalDateTime;
import java.time.Duration;

public class Task {

    private Integer id;
    private String title;
    private String description;
    private Status status;
    private LocalDateTime startTime;
    private Duration duration;

    public Task(Integer id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = Status.NEW;
    }

    public Task(Integer id, String title, String description, Status status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        status = Status.NEW;
    }

    public Task(Integer id,
                String title,
                String description,
                Status status,
                LocalDateTime startTime,
                Duration duration) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String title, String description, LocalDateTime startTime, Duration duration) {
        this.title = title;
        this.description = description;
        this.status = Status.NEW;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String title, String description, Status status, LocalDateTime startTime, Duration duration) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }


    public void setStatus(Status status) {
        this.status = status;
    }

    public TaskType getTaskType() {
        return TaskType.TASK;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    public static Task parseTaskFromString(String[] words) {
        Task task = new Task(Integer.parseInt(words[0]),
                words[2],
                words[3]
        );
        task.setStatus(Status.valueOf(words[4]));
        task.setStartTime(parseDateTime(words[6]));
        task.setDuration(parseDuration(words[7]));
        return task;
    }

    @Override
    public String toString() {
        return String.join(",",
                getId().toString(),
                TaskType.TASK.toString(),
                getTitle(),
                getDescription() != null ? getDescription() : "",
                getStatus() != null ? getStatus().toString() : Status.NEW.toString(),
                "",
                getStartTime() != null ? getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                getDuration() != null ? String.valueOf(duration.toMinutes()) : "0");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task task)) return false;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    protected static LocalDateTime parseDateTime(String value) {
        return value.isEmpty() ? null :
                LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    protected static Duration parseDuration(String value) {
        return Duration.ofMinutes(Long.parseLong(value.isEmpty() ? "0" : value));
    }
}