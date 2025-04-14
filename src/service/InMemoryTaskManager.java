package service;

import enums.Status;
import model.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> allTasks = new HashMap<>();
    private final Map<Integer, Epic> allEpics = new HashMap<>();
    private final Map<Integer, Subtask> allSubtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private Integer id = 1;

    //Методы получения списков всех созданных задач, эпиков и подзадач
    @Override
    public List<Task> getAllTasks() {
        return List.copyOf(allTasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return List.copyOf(allEpics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return List.copyOf(allSubtasks.values());
    }

    //Методы для удаления всех задач, эпиков и подзадач
    @Override
    public void deleteAllTasks() {
        for (Integer key : allTasks.keySet()) {
            historyManager.remove(key);
        }
        allTasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Integer key : allSubtasks.keySet()) {
            historyManager.remove(key);
        }
        for (Integer key : allEpics.keySet()) {
            historyManager.remove(key);
        }
        allEpics.clear();
        allSubtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer key : allSubtasks.keySet()) {
            historyManager.remove(key);
        }
        for (Epic epic : allEpics.values()) {
            epic.getSubtasksId().clear();
            updateEpic(epic);
        }
        allSubtasks.clear();
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
    public void createTask(Task task) {
        task.setId(generateId());
        allTasks.put(task.getId(), task);
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(generateId());
        allEpics.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        subtask.setId(generateId());
        allSubtasks.put(subtask.getId(), subtask);

        Epic epic = allEpics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        checkEpicStatus(subtask.getEpicId());
    }

    //Методы обновления задач, эпиков и подзадач
    @Override
    public void updateTask(Task task) {
        if (task.getId() == null) {
            System.out.println("Задача не найдена!");
            return;
        }
        allTasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic.getId() == null) {
            System.out.println("Эпик не найден!");
            return;
        }

        final Epic epicSaved = allEpics.get(epic.getId());
        epicSaved.setTitle(epic.getTitle());
        epicSaved.setDescription(epic.getDescription());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask.getId() == null || subtask.getEpicId() == null) {
            System.out.println("Подзадача не найдена или не привязана к эпику!");
            return;
        }
        if (!allEpics.containsKey(subtask.getEpicId())) {
            System.out.println("Подзадача привязана к несуществующему эпику!");
            return;
        }
        allSubtasks.put(subtask.getId(), subtask);
        checkEpicStatus(subtask.getEpicId());
    }

    //Методы удаления задач по идентификатору
    @Override
    public void deleteTask(Integer idTask) {
        final Task task = allTasks.remove(idTask);
        if (task == null) {
            System.out.println("Задачи с id " + idTask + " не существует!");
            return;
        }
        historyManager.remove(idTask);
    }

    @Override
    public void deleteEpic(Integer idEpic) {
        if (idEpic == null) {
            System.out.println("Эпика с id " + idEpic + " не существует!");
            return;
        }
        final Epic epic = allEpics.get(idEpic);
        List<Integer> subtaskIds = epic.getSubtasksId();
        for (Integer subtaskId : subtaskIds) {
            historyManager.remove(subtaskId);
            allSubtasks.remove(subtaskId);
        }
        allEpics.remove(idEpic);
        historyManager.remove(idEpic);
    }

    @Override
    public void deleteSubtask(Integer idSubtask) {
        final Subtask subtask = allSubtasks.remove(idSubtask);
        if (subtask == null) {
            System.out.println("Подзадачи с id " + idSubtask + " не существует!");
            return;
        }
        int epicId = subtask.getEpicId();
        allEpics.get(epicId).deleteSubtaskById(idSubtask);
        checkEpicStatus(epicId);
        historyManager.remove(idSubtask);
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
    public void checkEpicStatus(Integer idEpic) {
        Integer countOfNew = 0;
        Integer countOfDone = 0;
        final Epic epic = allEpics.get(idEpic);
        final List<Integer> currentSubtaskList = epic.getSubtasksId();

        for (Integer id : currentSubtaskList) {
            switch (allSubtasks.get(id).getStatus()) {
                case NEW -> countOfNew++;
                case DONE -> countOfDone++;
                default -> {
                    epic.setStatus(Status.IN_PROGRESS);
                    return;
                }
            }
        }
        int subtaskListSize = currentSubtaskList.size();
        if (countOfNew == subtaskListSize) {
            epic.setStatus(Status.NEW);
        } else if (countOfDone == subtaskListSize) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private Integer generateId() {
        return this.id++;
    }
}