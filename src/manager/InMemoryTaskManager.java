package manager;

import enums.Status;
import task.*;

import java.util.*;
import java.util.ArrayList;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> allTasks = new HashMap<>();
    private final Map<Integer, Epic> allEpics = new HashMap<>();
    private final Map<Integer, Subtask> allSubtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private Integer id = 1;


    //Методы получения списков всех созданных задач, эпиков и подзадач
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(allTasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(allEpics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
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
            epic.clearSubtasksId();
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
        allSubtasks.put(subtask.getId(), subtask);
        Epic epic = allEpics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
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
        Epic oldEpic = allEpics.get(idEpic);
        // Если у обновленного эпика были подзадачи, удаляем их из allSubtask
        List<Integer> oldEpicSubtasksId = oldEpic.getSubtasksId();
        for (Integer subtaskId : oldEpicSubtasksId) {
            // Проверяем, используется ли подзадача в других эпиках
            boolean isSubtaskUsed = false;
            for (Epic otherEpic : allEpics.values()) {
                if (otherEpic.getSubtasksId().contains(subtaskId)) {
                    isSubtaskUsed = true;
                    break;
                }
            }
            // Если подзадача не используется в других эпиках, удаляем ее
            if (!isSubtaskUsed) {
                allSubtasks.remove(subtaskId);
            }
        }
        // Обновляем эпик
        allEpics.replace(idEpic, epic);

        // Добавляем новые подзадачи, если их еще нет в allSubtasks
        List<Integer> newEpicSubtasksId = epic.getSubtasksId();
        for (Integer subtaskId : newEpicSubtasksId) {
            if (!allSubtasks.containsKey(subtaskId)) {
                // Если подзадача не существует, пропускаем ее
                continue;
            }
            // Обновляем подзадачу в allSubtasks
            allSubtasks.put(subtaskId, allSubtasks.get(subtaskId));
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
        allSubtasks.replace(idSubtask, subtask);
        Integer idEpic = subtask.getEpicId();
        Epic epic = allEpics.get(idEpic);
        // Обновление подзадачи в списке подзадач эпика и проверяем статус эпика
        List<Integer> subtasksId = epic.getSubtasksId();
        if (!subtasksId.contains(idSubtask)) {
            subtasksId.add(idSubtask); // Добавляем ID подзадачи, если его нет в списке
        }
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
        Epic epic = allEpics.remove(idEpic);
        if (epic != null) {
            List<Integer> subtasksId = epic.getSubtasksId();
            for (Integer subtaskId : subtasksId) {
                allSubtasks.remove(subtaskId);
            }
        }
        return epic;
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
        List<Integer> subtasks = epic.getSubtasksId();
        subtasks.remove(subtask);
        epic.setSubtasksId(subtasks);
        checkEpicStatus(epic);
        return deletedSubtask;
    }


    //Дополнительный метод получения списка всех подзадач определённого эпика
    @Override
    public List<Subtask> getSubtaskByEpic(Integer idEpic) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = allEpics.get(idEpic);
        for (Integer id : epic.getSubtasksId()) {
            result.add(allSubtasks.get(id));
        }
        return result;
    }



    // Метод возвращения списка просмотренных задач
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Проверка статуса эпика при удалении или изменении подзадачи
    private void checkEpicStatus(Epic epic) {
        boolean isSubtaskStatusNew = true;
        boolean isSubtaskStatusDone = true;

        List<Status> statuses = new ArrayList<>();

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

        if (epic.getSubtasksId().isEmpty() || isSubtaskStatusNew) {
            epic.setStatus(Status.NEW);
        } else if (isSubtaskStatusDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private Integer generateId() {
        return this.id++;
    }

}