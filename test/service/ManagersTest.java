package service;

import org.junit.jupiter.api.Test;
import service.managers.HistoryManager;
import service.managers.Managers;
import service.managers.TaskManager;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    public void getDefaultShouldBeInitializeInMemoryHistoryManager() {
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(taskManager, "Экземпляр класса Managers должен быть проинициализирован и" +
                " готов к работе");
        assertNotNull(historyManager, "Экземпляр класса Managers должен быть проинициализирован и" +
                " готов к работе");
    }
}