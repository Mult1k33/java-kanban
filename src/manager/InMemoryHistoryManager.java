package manager;

import java.util.ArrayList;
import java.util.List;
import task.Task;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int MAX_HISTORY_SIZE = 10;
    private final List<Task> taskHistory = new ArrayList<>();

    // Метод добавляет задачи в список просмотренных задач.
    // Если в списке 10 задач, то удаляется первая просмотренная. Новая добавляется в конец списка.
    @Override
    public void add(Task task) {
        if (taskHistory.size() == MAX_HISTORY_SIZE) {
            taskHistory.removeFirst();
        }
        taskHistory.add(task);
    }

    // Метод возвращает список последних просмотренных задач
    @Override
    public List<Task> getHistory(){
        return taskHistory;
    }
}