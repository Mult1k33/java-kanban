package test.task;

import enums.Status;
import org.junit.jupiter.api.Test;
import task.Subtask;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    //проверьте, что наследники класса Task равны друг другу, если равен их id;
    @Test
    public void tasksWithEqualIdShouldBeEqual() {
        Subtask subtask1 = new Subtask(10, "Подзадача 1", "Описание 1", Status.NEW, 5);
        Subtask subtask2 = new Subtask(10, "Подзадача 2", "Описание 2", Status.DONE, 5);
        assertEquals(subtask1, subtask2, "Наследники класса task.Task с одинаковым id должны быть равны! ");
    }

}