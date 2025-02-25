import java.util.ArrayList;
import java.util.HashMap;

class TaskManager {

    private final HashMap<Integer, Task> allTasks = new HashMap<>();
    private final HashMap<Integer, Epic> allEpics = new HashMap<>();
    private final HashMap<Integer, Subtask> allSubtasks = new HashMap<>();
    private Integer id = 0;


    //Методы получения списков всех созданных задач, эпиков и подзадач
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(allTasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(allEpics.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(allSubtasks.values());
    }


    //Методы для удаления всех задач, эпиков и подзадач
    public void deleteAllTasks() {
        allTasks.clear();
    }

    public void deleteAllEpics() {
        allEpics.clear();
        allSubtasks.clear();
    }

    public void deleteAllSubtasks() {
        allSubtasks.clear();
        for (Epic epic : allEpics.values()) {
            epic.getSubtasks().clear();
            checkEpicStatus(epic);
        }
    }


    //Методы для получения задач, эпиков, подзадач по идентификатору
    public Task getTaskById(Integer idTask) {
        return allTasks.get(idTask);
    }

    public Epic getEpicById(Integer idEpic) {
        return allEpics.get(idEpic);
    }

    public Subtask getSubtaskById(Integer idSubtask) {
        return allSubtasks.get(idSubtask);
    }


    //Методы для создания задач, эпиков и подзадач
    public void createTask(Task task) {
        allTasks.put(generateId(), task);
        task.setId(id);
    }

    public void createEpic(Epic epic) {
        allEpics.put(generateId(), epic);
        epic.setId(id);
    }

    public void createSubtask(Subtask subtask) {
        if (allEpics.containsKey(subtask.getEpicId())) {
            allSubtasks.put(generateId(), subtask);
            subtask.setId(id);
            Epic epic = allEpics.get(subtask.getEpicId());
            epic.addSubtask(subtask);
            checkEpicStatus(epic);
        }
    }


    //Методы обновления задач, эпиков и подзадач
    public void updateTask(Task task) {
        if (allTasks.containsKey(task.getId())) {
            allTasks.put(task.getId(), task);
        }
    }

    public void updateEpic(Epic epic) {
        if (allEpics.containsKey(epic.getId())) {
            allEpics.put(epic.getId(), epic);
        }
    }

    public void updateSubtask(Subtask subtask) {
        if (allSubtasks.containsKey(subtask.getId())) {
            allSubtasks.put(subtask.getId(), subtask);
            Epic epic = allEpics.get(subtask.getEpicId());
            checkEpicStatus(epic);
        }
    }


    //Методы удаления задач по идентификатору
    public void deleteTask(Integer idTask) {
        allTasks.remove(idTask);
    }

    public void deleteEpic(Integer idEpic) {
        Epic epic = allEpics.remove(idEpic);
        if (epic != null) {
            for (Integer id : epic.getSubtasks()) {
                allSubtasks.remove(id);
            }
        }
    }

    public void deleteSubtask(Integer idSubtask) {
        Subtask subtask = allSubtasks.remove(idSubtask);
        if (subtask != null) {
            Epic epic = allEpics.get(subtask.getEpicId());
            epic.getSubtasks().remove(idSubtask);
            checkEpicStatus(epic);
        }
    }


    //Дополнительный метод получения списка всех подзадач определённого эпика
    public ArrayList<Subtask> getSubtaskByEpic(Integer idEpic) {
        ArrayList<Subtask> result = new ArrayList<>();
        Epic epic = allEpics.get(idEpic);
        for (Integer id : epic.getSubtasks()) {
            result.add(allSubtasks.get(id));
        }
        return result;
    }


    // Проверка статуса эпика
    private void checkEpicStatus(Epic epic) {
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


    private Integer generateId() {
        return this.id++;
    }
}
