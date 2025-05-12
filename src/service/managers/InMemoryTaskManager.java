package service.managers;

import enums.*;
import model.*;
import service.exeptions.NotFoundException;
import service.exeptions.TimeOverlapException;

import java.util.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> allTasks = new HashMap<>();
    private final Map<Integer, Epic> allEpics = new HashMap<>();
    private final Map<Integer, Subtask> allSubtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final Comparator<Task> taskComparator = Comparator.comparing(Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder()));
    private final Set<Task> prioritizedTasks = new TreeSet<>(taskComparator);
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
        } else {
            throw new NotFoundException("Задача с ID " + idTask + " не найдена");
        }
        return task;
    }

    @Override
    public Epic getEpicById(Integer idEpic) {
        Epic epic = allEpics.get(idEpic);
        if (epic != null) {
            historyManager.add(epic);
        } else {
            throw new NotFoundException("Эпик с ID " + idEpic + " не найден");
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(Integer idSubtask) {
        Subtask subtask = allSubtasks.get(idSubtask);
        if (subtask != null) {
            historyManager.add(subtask);
        } else {
            throw new NotFoundException("Подзадача с ID " + idSubtask + " не найдена");
        }
        return subtask;
    }

    //Методы для создания задач, эпиков и подзадач
    @Override
    public void createTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Задача не должна быть null");
        }

        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("Название задачи не может быть пустым");
        }
        if (task.getDescription() == null) {
            throw new IllegalArgumentException("Описание задачи не может быть пустым");
        }

        // Проверяем пересечение времени (если у задачи задано время)
        if (task.getStartTime() != null && checkIntersection(task)) {
            throw new TimeOverlapException("Задача пересекается по времени с уже добавленными");
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

        if (epic.getTitle() == null || epic.getTitle().isBlank()) {
            throw new IllegalArgumentException("Название эпика не может быть пустым");
        }
        if (epic.getDescription() == null) {
            throw new IllegalArgumentException("Описание эпика не может быть пустым");
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

        if (subtask.getTitle() == null || subtask.getTitle().isBlank()) {
            throw new IllegalArgumentException("Название подзадачи не может быть пустым");
        }

        if (subtask.getDescription() == null) {
            throw new IllegalArgumentException("Описание подзадачи не может быть пустым");
        }

        if (!allEpics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Не найден эпик с id=" + subtask.getEpicId());
        }

        // Проверка пересечения времени
        if (subtask.getStartTime() != null && checkIntersection(subtask)) {
            throw new TimeOverlapException("Подзадача пересекается по времени с уже добавленными");
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
        // Проверка на null
        if (subtask == null) {
            System.out.println("Подзадача не может быть null");
            return;
        }

        // Проверка существования подзадачи
        if (!allSubtasks.containsKey(subtask.getId())) {
            System.out.println("Подзадача с id=" + subtask.getId() + " не найдена");
            return;
        }

        // Проверка эпика
        if (subtask.getEpicId() == null || !allEpics.containsKey(subtask.getEpicId())) {
            System.out.println("Неверный epicId: " + subtask.getEpicId());
            return;
        }

        // Проверка пересечения времени (кроме самой себя)
        if (subtask.getStartTime() != null) {
            Subtask existing = allSubtasks.get(subtask.getId());
            prioritizedTasks.remove(existing);

            if (checkIntersection(subtask)) {
                prioritizedTasks.add(existing); // Возвращаем обратно при ошибке
                System.out.println("Обнаружено пересечение по времени");
                return;
            }
        }

        // Обновление подзадачи
        allSubtasks.put(subtask.getId(), subtask);

        // Обновление prioritizedTasks
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }

        // Обновление эпика
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
        // Проверка существования эпика
        if (!allEpics.containsKey(idEpic)) {
            System.out.println("Эпик с id=" + idEpic + " не найден");
            return;
        }

        Epic epic = allEpics.get(idEpic);
        List<Integer> subtaskIds = epic.getSubtasksId();

        // Обработка случая, когда нет подзадач
        if (subtaskIds.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        int countNew = 0;
        int countDone = 0;

        // Проверка статусов подзадач
        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = allSubtasks.get(subtaskId);

            // Пропускаем несуществующие подзадачи
            if (subtask == null) {
                System.out.println("Подзадача с id=" + subtaskId + " не найдена");
                continue;
            }

            switch (subtask.getStatus()) {
                case NEW -> countNew++;
                case DONE -> countDone++;
                // Для IN_PROGRESS сразу выставляем статус и выходим
                default -> {
                    epic.setStatus(Status.IN_PROGRESS);
                    return;
                }
            }
        }

        // Определение итогового статуса
        if (countNew == subtaskIds.size()) {
            epic.setStatus(Status.NEW);
        } else if (countDone == subtaskIds.size()) {
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