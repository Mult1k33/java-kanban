package service;

import enums.*;
import model.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        if (file == null) {
            throw new ManagerReadException("Файл не инициализирован");
        }
        this.file = file;
    }

    //Методы для создания задач, эпиков и подзадач
    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }


    //Методы обновления задач, эпиков и подзадач
    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    //Методы для удаления всех задач, эпиков и подзадач
    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    //Методы удаления задач по идентификатору
    @Override
    public void deleteTask(Integer idTask) {
        super.deleteTask(idTask);
        save();
    }

    @Override
    public void deleteEpic(Integer idEpic) {
        super.deleteEpic(idEpic);
        save();
    }

    @Override
    public void deleteSubtask(Integer idSubtask) {
        super.deleteSubtask(idSubtask);
        save();
    }

    @Override
    public void checkEpicStatus(Integer idEpic) {
        super.checkEpicStatus(idEpic);
        save();
    }

    // Метод, который восстанавливает состояние менеджера из файла
    public static FileBackedTaskManager loadFromFile(File file) throws ManagerReadException {
        if (file == null || !file.exists()) {
            throw new ManagerReadException("Файл не существует");
        }

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            bufferedReader.readLine();

            List<Task> tasks = new ArrayList<>();

            while ((line = bufferedReader.readLine()) != null) {
                Task task = fromString(line);
                if (task != null) {
                    tasks.add(task);
                }
            }
            for (Task task : tasks) {
                switch (task.getTaskType()) {
                    case TASK:
                        manager.createTask(task);
                        break;
                    case EPIC:
                        manager.createEpic((Epic) task);
                        break;
                    case SUBTASK:
                        manager.createSubtask((Subtask) task);
                        break;
                }
            }
        } catch (IOException e) {
            throw new ManagerReadException("Ошибка при чтении данных из файла: " + e.getMessage());
        }
        return manager;
    }

    // Метод, который сохраняет текущее состояние менеджера в указанный файл
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("id,type,title,description,status,epic,startTime,duration\n");

            for (Task task : getAllTasks()) {
                writer.write(task.toString() + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(epic.toString() + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(subtask.toString() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи данных в файл: " + e.getMessage());
        }
    }

    // Метод создания задачи из строки
    private static Task fromString(String value) {
        String[] words = value.split(",", -1);  // -1 сохраняет пустые поля

        try {
            int id = Integer.parseInt(words[0]);
            TaskType type = TaskType.valueOf(words[1]);
            String title = words[2];
            String description = words[3];
            Status status = Status.valueOf(words[4]);
            String epicIdStr = words.length > 5 ? words[5] : "";
            String startTimeStr = words.length > 6 ? words[6] : "";
            String durationStr = words.length > 7 ? words[7] : "0"; // По умолчанию 0

            LocalDateTime startTime = !startTimeStr.isEmpty()
                    ? LocalDateTime.parse(startTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : null;
            Duration duration = Duration.ofMinutes(Long.parseLong(durationStr));

            switch (type) {
                case TASK:
                    Task task = new Task(id, title, description);
                    task.setStatus(status);
                    task.setStartTime(startTime);
                    task.setDuration(duration);
                    return task;
                case EPIC:
                    Epic epic = new Epic(id, title, description);
                    epic.setStatus(status);
                    epic.setStartTime(startTime);
                    epic.setDuration(duration);
                    return epic;
                case SUBTASK:
                    if (epicIdStr.isEmpty()) {
                        throw new IllegalArgumentException("У подзадачи не указан epicId");
                    }
                    Subtask subtask = new Subtask(id, title, description, Integer.parseInt(epicIdStr));
                    subtask.setStatus(status);
                    subtask.setStartTime(startTime);
                    subtask.setDuration(duration);
                    return subtask;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка парсинга строки: " + value, e);
        }
        return null;
    }
}