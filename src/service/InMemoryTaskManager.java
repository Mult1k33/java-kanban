package service;

import enums.*;
import model.*;
import java.util.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> allTasks = new HashMap<>();
    private final Map<Integer, Epic> allEpics = new HashMap<>();
    private final Map<Integer, Subtask> allSubtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private Integer id = 1;
    private final Comparator<Task> taskComparator = Comparator.comparing(Task :: getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder()));
    private final Set<Task> prioritizedTasks = new TreeSet<>(taskComparator);

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
        allTasks.keySet().forEach(id -> {
            historyManager.remove(id);
            prioritizedTasks.removeIf(task -> task.getId().equals(id));
        });
        allTasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        allEpics.keySet().forEach(historyManager::remove);
        allSubtasks.keySet().forEach(id -> {
            historyManager.remove(id);
            prioritizedTasks.removeIf(task -> task.getId().equals(id));
        });
        allSubtasks.clear();
        allEpics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        allSubtasks.keySet().forEach(id -> {
            historyManager.remove(id);
            prioritizedTasks.removeIf(task -> task.getId().equals(id));
        });
        allEpics.values().forEach(epic -> {
            epic.getSubtasksId().clear();
            checkEpicDuration(epic);
            checkEpicStatus(epic.getId());
        });
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
        if (task == null) {
            throw new IllegalArgumentException("Задача не должна быть null");
        }

        // Проверяем пересечение времени (если у задачи задано время)
        if (task.getStartTime() != null && checkIntersection(task)) {
            throw new IllegalStateException("Задача пересекается по времени с уже добавленными");
        }

        task.setId(generateId());
        allTasks.put(task.getId(), task);

        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic == null) {
            throw new IllegalArgumentException("Эпик не должен быть null");
        }

        epic.setId(generateId());
        allEpics.put(epic.getId(), epic);
        checkEpicDuration(epic);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (subtask == null) {
            throw new IllegalArgumentException("Подзадача не должна быть null");
        }

        if (!allEpics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Не найден эпик с id=" + subtask.getEpicId());
        }

        // Проверка пересечения времени
        if (subtask.getStartTime() != null && checkIntersection(subtask)) {
            throw new IllegalStateException("Подзадача пересекается по времени с уже добавленными");
        }

        subtask.setId(generateId());
        allSubtasks.put(subtask.getId(), subtask);

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        Epic epic = allEpics.get(subtask.getEpicId());
        epic.addSubtask(subtask.getId());
        checkEpicDuration(epic);
        checkEpicStatus(epic.getId());
    }


    //Методы обновления задач, эпиков и подзадач
    @Override
    public void updateTask(Task task) {
        if (task == null || !allTasks.containsKey(task.getId())) {
            System.out.println("Задача не найдена!");
            return;
        }

        Task existing = allTasks.get(task.getId());
        prioritizedTasks.remove(existing);

        if (task.getStartTime() != null && checkIntersection(task)) {
            System.out.println("Задача пересекается по времени с уже добавленными");
            prioritizedTasks.add(existing);
            return;
        }

        allTasks.put(task.getId(), task);

        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null || !allEpics.containsKey(epic.getId())) {
            System.out.println("Эпик не найден!");
            return;
        }

        Epic existing = allEpics.get(epic.getId());
        existing.setTitle(epic.getTitle());
        existing.setDescription(epic.getDescription());
        checkEpicDuration(existing);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null || subtask.getEpicId() == null) {
            System.out.println("Подзадача не найдена или не привязана к эпику!");
            return;
        }
        if (!allEpics.containsKey(subtask.getEpicId())) {
            System.out.println("Подзадача привязана к несуществующему эпику!");
            return;
        }

        Subtask existing = allSubtasks.get(subtask.getId());
        prioritizedTasks.remove(existing);

        if (subtask.getStartTime() != null && checkIntersection(subtask)) {
            System.out.println("Подзадача пересекается по времени с уже добавленными");
            return;
        }

        allSubtasks.put(subtask.getId(), subtask);

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        Epic epic = allEpics.get(subtask.getEpicId());
        checkEpicDuration(epic);
        checkEpicStatus(epic.getId());
    }

    //Методы удаления задач по идентификатору
    @Override
    public void deleteTask(Integer idTask) {
        final Task task = allTasks.remove(idTask);

        if (task == null) {
            System.out.println("Задачи с id " + idTask + " не существует!");
            return;
        }

        prioritizedTasks.remove(task);
        historyManager.remove(idTask);
    }

    @Override
    public void deleteEpic(Integer idEpic) {

        if (idEpic == null || !allEpics.containsKey(idEpic)) {
            System.out.println("Эпика с id " + idEpic + " не существует!");
            return;
        }

        final Epic epic = allEpics.remove(idEpic);

        epic.getSubtasksId().forEach(subtaskId -> {
            prioritizedTasks.removeIf(task -> task.getId().equals(subtaskId));
            allSubtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        });

        historyManager.remove(idEpic);
    }

    @Override
    public void deleteSubtask(Integer idSubtask) {
        final Subtask subtask = allSubtasks.remove(idSubtask);

        if (subtask == null) {
            System.out.println("Подзадачи с id " + idSubtask + " не существует!");
            return;
        }

        prioritizedTasks.remove(subtask);
        int epicId = subtask.getEpicId();
        allEpics.get(epicId).deleteSubtaskById(idSubtask);
        checkEpicDuration(getEpicById(subtask.getEpicId()));
        checkEpicStatus(epicId);
        historyManager.remove(idSubtask);
    }

    //Дополнительный метод получения списка всех подзадач определённого эпика
    @Override
    public List<Subtask> getSubtaskByEpic(Integer idEpic) {
        Epic epic = allEpics.get(idEpic);

        if (epic == null) {
            System.out.println("Эпика с id " + idEpic + " не существует");
            return null;
        }
        return epic.getSubtasksId().stream().map(allSubtasks::get).toList();
    }

    // Метод возвращения списка просмотренных задач
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    //Метод возвращает список отсортированных задач по приоритету
    @Override
    public List<Task> getPrioritizedTasks() {
        return List.copyOf(prioritizedTasks);
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

    private void checkEpicDuration(Epic epic) {
        List<Subtask> activeSubtasks = getSubtaskByEpic(epic.getId()).stream()
                .filter(subtask -> subtask.getStartTime() != null)
                .toList();

        if (activeSubtasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(Duration.ZERO);
            return;
        }

        // set start
        LocalDateTime startTime = activeSubtasks.stream()
                .map(Subtask::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        epic.setStartTime(startTime);

        // set end
        LocalDateTime endTime = activeSubtasks.stream()
                .map(Subtask::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        epic.setEndTime(endTime);

        // set duration
        Duration duration = Duration.between(startTime, endTime);
        epic.setDuration(duration);
    }

    private boolean checkIntersection(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return false;
        }

        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newTask.getEndTime();

        return prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null && task.getDuration() != null)
                .anyMatch(task -> {
                    LocalDateTime taskStart = task.getStartTime();
                    LocalDateTime taskEnd = task.getEndTime();
                    return newStart.isBefore(taskEnd) && newEnd.isAfter(taskStart);
                });
    }

    private Integer generateId() {
        return this.id++;
    }
}