package manager;

import task.Task;

import java.util.List;

/** Интерфейс для управления историей просмотра задач.
 * Позволяет добавлять задачи в истории просмотров.
 * Позволяет получать список просмотроенных задач.
 */

public interface HistoryManager {

    void add(Task task);

    List<Task> getHistory();
}

