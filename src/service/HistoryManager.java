package service;

import model.Task;

import java.util.List;

/** Интерфейс для управления историей просмотра задач.
 * Позволяет добавлять задачи в историю просмотров.
 * Позволяет удалять задачу из истории просмотров.
 * Позволяет получать список просмотренных задач.
 */

public interface HistoryManager {

    void add(Task task);

    void remove(Integer id);

    List<Task> getHistory();
}

