package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    Integer generateId();

    //Методы получения списков всех созданных задач, эпиков и подзадач
    ArrayList<Task> getAllTasks();

    ArrayList<Epic> getAllEpics();

    ArrayList<Subtask> getAllSubtasks();

    //Методы для удаления всех задач, эпиков и подзадач
    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    //Методы для получения задач, эпиков, подзадач по идентификатору
    Task getTaskById(Integer idTask);

    Epic getEpicById(Integer idEpic);

    Subtask getSubtaskById(Integer idSubtask);

    //Методы для создания задач, эпиков и подзадач
    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    //Методы обновления задач, эпиков и подзадач
    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Subtask updateSubtask(Subtask subtask);

    //Методы удаления задач по идентификатору
    Task deleteTask(Integer idTask);

    Epic deleteEpic(Integer idEpic);

    Subtask deleteSubtask(Integer idSubtask);

    //Дополнительный метод получения списка всех подзадач определённого эпика
    ArrayList<Subtask> getSubtaskByEpic(Epic epic);

    //Метод для отображения последних просмотренных 10 задач
    List<Task> getHistory();
}