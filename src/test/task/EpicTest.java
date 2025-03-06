package test.task;

import enums.Status;
import org.junit.jupiter.api.Test;
import task.Epic;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    //проверьте, что наследники класса Task равны друг другу, если равен их id;
    @Test
    public void tasksWithEqualIdShouldBeEqual() {
        Epic epic1 = new Epic(5, "Эпик 1", "Описание эпика 1", Status.NEW);
        Epic epic2 = new Epic(5, "Эпик 2", "Описание эпика 2", Status.IN_PROGRESS);
        assertEquals(epic1, epic2, "Наследники класса task.Task с одинаковым id должны быть равны!");
    }
}