package service;

import enums.*;
import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static enums.TaskType.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) throws IOException {
        if (file == null) {
            throw new ManagerReadException("Файл не инициализирован");
        }
        if (!file.exists()) {
            Files.createFile(file.toPath());
        }
        this.file = file;
        restoreFromFile();
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

    // Метод, который сохраняет текущее состояние менеджера в указанный файл
    private void save() {
        if (file == null) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write("id,type,name,status,description,epic\n");

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
        final String[] words = value.split(",", -1);

        try {
            final TaskType taskType = TaskType.valueOf(words[1]);
            switch (taskType) {
                case TASK:
                    Task task = new Task(Integer.parseInt(words[0]), words[2], words[4]);
                    task.setStatus(Status.valueOf(words[3]));
                    return task;
                case EPIC:
                    Epic epic = new Epic(Integer.parseInt(words[0]), words[2], words[4]);
                    epic.setStatus(Status.valueOf(words[3]));
                    return epic;
                case SUBTASK:
                    Subtask subtask = new Subtask(
                            Integer.parseInt(words[0]), words[2], words[4], Integer.parseInt(words[5]));
                    subtask.setStatus(Status.valueOf(words[3]));
                    return subtask;
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Некорректные данные в строке: " + value, e);
        }
        return null;
    }

    //
    private void restoreFromFile() {
        if (file == null || !file.exists()) {
            throw new ManagerReadException("Файл не существует");
        }

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
                            createTask((Task) task);
                            break;
                        case EPIC:
                            createEpic((Epic) task);
                            break;
                        case SUBTASK:
                            createSubtask((Subtask) task);
                            break;
                    }
                }
            } catch (IOException e) {
            throw new ManagerReadException("Ошибка при чтении данных из файла: " + e.getMessage());
        }
    }
}
