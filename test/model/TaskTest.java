package model;

import enums.Status;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    static Task task1 = new Task(3, "Задача 1", "Описание задачи 1", Status.NEW);
    static Task task2 = new Task(3, "Задача 2", "Описание задачи 2", Status.DONE);

    //проверьте, что экземпляры класса Task равны друг другу, если равен их id;
    @Test
    public void tasksWithEqualIdShouldBeEqual() {
        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны!");
    }
}