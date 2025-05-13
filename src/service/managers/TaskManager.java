package service.managers;

import model.*;
import java.util.List;


public interface TaskManager {

    //Методы получения списков всех созданных задач, эпиков и подзадач
    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    //Методы для удаления всех задач, эпиков и подзадач
    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    //Методы для получения задач, эпиков, подзадач по идентификатору
    Task getTaskById(Integer idTask);

    Epic getEpicById(Integer idEpic);

    Subtask getSubtaskById(Integer idSubtask);

    //Методы для создания задач, эпиков и подзадач
    void createTask(Task task);

    void createEpic(Epic epic);

    void createSubtask(Subtask subtask);

    //Методы обновления задач, эпиков и подзадач
    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    //Методы удаления задач по идентификатору
    void deleteTask(Integer idTask);

    void deleteEpic(Integer idEpic);

    void deleteSubtask(Integer idSubtask);

    //Дополнительный метод получения списка всех подзадач определённого эпика
    List<Subtask> getSubtaskByEpic(Integer idEpic);

    //Метод для отображения последних просмотренных задач
    List<Task> getHistory();

    // Метод получения отсортированного списка задач по приоритету
    List<Task> getPrioritizedTasks();
}