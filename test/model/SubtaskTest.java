package model;

import enums.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    static Subtask subtask1 = new Subtask(10, "Подзадача 1", "Описание 1", Status.NEW, 5);
    static Subtask subtask2 = new Subtask(10, "Подзадача 2", "Описание 2", Status.DONE, 5);

    //проверьте, что наследники класса Task равны друг другу, если равен их id;
    @Test
    public void tasksWithEqualIdShouldBeEqual() {
        assertEquals(subtask1, subtask2, "Наследники класса task.Task с одинаковым id должны быть равны! ");
    }
}