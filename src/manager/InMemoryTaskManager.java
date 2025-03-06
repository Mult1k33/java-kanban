package manager;

import enums.Status;
import task.*;

import java.util.*;
import java.util.ArrayList;

public class InMemoryTaskManager implements TaskManager {

    private final HashMap<Integer, Task> allTasks = new HashMap<>();
    private final HashMap<Integer, Epic> allEpics = new HashMap<>();
    private final HashMap<Integer, Subtask> allSubtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private Integer id = 1;


    @Override
    public Integer generateId() {
        return this.id++;
    }

    //Методы получения списков всех созданных задач, эпиков и подзадач
    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(allTasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(allEpics.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(allSubtasks.values());
    }

    //Методы для удаления всех задач, эпиков и подзадач
    @Override
    public void deleteAllTasks() {
        allTasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        allEpics.clear();
        allSubtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        allSubtasks.clear();
        for (Epic epic : allEpics.values()) {
            epic.clearSubtasks();
            epic.setStatus(Status.NEW);
        }
    }

    //Методы для получения задач, эпиков, подзадач по идентификатору и занесение в историю просмотров
    @Override
    public Task getTaskById(Integer idTask) {
        Task task = allTasks.get(idTask);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(Integer idEpic) {
        Epic epic = allEpics.get(idEpic);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(Integer idSubtask) {
        Subtask subtask = allSubtasks.get(idSubtask);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    //Методы для создания задач, эпиков и подзадач
    @Override
    public Task createTask(Task task) {
        task.setId(generateId());
        allTasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(generateId());
        allEpics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(generateId());
        Epic epic = allEpics.get(subtask.getEpicId());
        epic.addSubtask(subtask);
        allSubtasks.put(subtask.getId(), subtask);
        checkEpicStatus(epic);
        return subtask;
    }

    //Методы обновления задач, эпиков и подзадач
    @Override
    public Task updateTask(Task task) {
        Integer idTask = task.getId();
        if (!allTasks.containsKey(idTask)) {
            return null;
        }
        allTasks.replace(idTask, task);
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Integer idEpic = epic.getId();
        if (!allEpics.containsKey(idEpic)) {
            return null;
        }
        // Если у обновленного эпика были подзадачи, удаляем их из allSubtask
        Epic oldEpic = allEpics.get(idEpic);
        ArrayList<Subtask> oldEpicSubtasks = oldEpic.getSubtasks();
        if (!oldEpicSubtasks.isEmpty()) {
            for (Subtask subtask : oldEpicSubtasks) {
                allSubtasks.remove(subtask.getId());
            }
        }
        allEpics.replace(idEpic, epic);
        // Если у обновленного эпика есть подзадачи, добавляем их в allSubtask
        ArrayList<Subtask> newEpicSubtasks = epic.getSubtasks();
        if (!newEpicSubtasks.isEmpty()) {
            for (Subtask subtask : newEpicSubtasks) {
                allSubtasks.put(subtask.getId(), subtask);
            }
        }
        checkEpicStatus(epic);
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Integer idSubtask = subtask.getId();
        if (!allSubtasks.containsKey(idSubtask)) {
            return null;
        }
        Integer idEpic = subtask.getEpicId();
        Subtask oldSubtask = allSubtasks.get(idSubtask);
        allSubtasks.replace(idSubtask, subtask);
        // Обновление подзадачи в списке подзадач эпика и проверяем статус эпика
        Epic epic = allEpics.get(idEpic);
        ArrayList<Subtask> subtasks = epic.getSubtasks();
        subtasks.remove(oldSubtask);
        subtasks.add(subtask);
        epic.setSubtasks(subtasks);
        checkEpicStatus(epic);
        return subtask;
    }

    //Методы удаления задач по идентификатору
    @Override
    public Task deleteTask(Integer idTask) {
        return allTasks.remove(idTask);
    }

    @Override
    public Epic deleteEpic(Integer idEpic) {
        ArrayList<Subtask> epicSubtasks = allEpics.get(idEpic).getSubtasks();
        for (Subtask subtask : epicSubtasks) {
            allSubtasks.remove(subtask.getId());
        }
        return allEpics.remove(idEpic);
    }

    @Override
    public Subtask deleteSubtask(Integer idSubtask) {
        if (!allSubtasks.containsKey(id)) {
            return null;
        }
        Subtask subtask = allSubtasks.get(id);
        Integer idEpic = subtask.getEpicId();
        Subtask deletedSubtask = allSubtasks.remove(id);
        //Обновление списка подзадач и статуса эпика
        Epic epic = allEpics.get(idEpic);
        ArrayList<Subtask> subtasks = epic.getSubtasks();
        subtasks.remove(subtask);
        epic.setSubtasks(subtasks);
        checkEpicStatus(epic);
        return deletedSubtask;
    }


    //Дополнительный метод получения списка всех подзадач определённого эпика
    @Override
    public ArrayList<Subtask> getSubtaskByEpic(Epic epic) {
        return epic.getSubtasks();
    }

    // Проверка статуса эпика при удалении или изменении подзадачи
    void checkEpicStatus(Epic epic) {
        boolean isSubtaskStatusNew = true;
        boolean isSubtaskStatusDone = true;

        ArrayList<Status> statuses = new ArrayList<>();

        for (Integer id : allSubtasks.keySet()) {
            Subtask subtask = allSubtasks.get(id);
            if (subtask.getEpicId().equals(epic.getId())) {
                statuses.add(subtask.getStatus());
            }
        }

        for (Status status : statuses) {
            if (!status.equals(Status.NEW)) {
                isSubtaskStatusNew = false;
                break;
            }
        }

        for (Status status : statuses) {
            if (!status.equals(Status.DONE)) {
                isSubtaskStatusDone = false;
                break;
            }
        }

        if (epic.getSubtasks().isEmpty() || isSubtaskStatusNew) {
            epic.setStatus(Status.NEW);
        } else if (isSubtaskStatusDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    // Метод возвращения списка просмотренных задач
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}