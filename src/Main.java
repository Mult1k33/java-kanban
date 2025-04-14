import enums.Status;
import service.*;
import model.*;

import java.io.File;
import java.io.IOException;

public class Main {

    private static final TaskManager inMemoryTaskManager = Managers.getDefault();

    public static void main(String[] args) throws IOException {

        File file = File.createTempFile("task",".csv");

        TaskManager taskManager = Managers.loadFromFile(file);

        // Создание двух задач, эпика с тремя подзадачами и эпика без подзадач
        Task task1 = new Task("Стать разработчиком Java", "Пройти обучение в ЯП");
        taskManager.createTask(task1);
        inMemoryTaskManager.createTask(task1);
        Task task2 = new Task("Вакансия 114597", "Закрыть вакансию SA");
        taskManager.createTask(task2);
        inMemoryTaskManager.createTask(task2);

        Epic epic1 = new Epic("ФЗ спринта 6", "Написать бэкенд для трекера задач");
        taskManager.createEpic(epic1);
        inMemoryTaskManager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача 1", "Написать код программы", epic1.getId());
        taskManager.createSubtask(subtask1);
        inMemoryTaskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Сделать пул-реквест", epic1.getId());
        taskManager.createSubtask(subtask2);
        inMemoryTaskManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("Подзадача 3", "Пройти код-ревью", epic1.getId());
        taskManager.createSubtask(subtask3);
        inMemoryTaskManager.createSubtask(subtask3);

        Epic epic2 = new Epic("Б.Эккель", "Дополнительня литература");
        taskManager.createEpic(epic2);
        inMemoryTaskManager.createEpic(epic2);

        // Проверка, что все задачи создались
        printAllTasks();
        System.out.println();

        //Изменение статусов задач и подзадач
        task1.setStatus(Status.IN_PROGRESS);
        task2.setStatus(Status.DONE);

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.IN_PROGRESS);
        subtask3.setStatus(Status.IN_PROGRESS);

        // Обновление статусов задач, эпиков и подзадач
        taskManager.updateTask(task1);
        taskManager.updateTask(task2);

        taskManager.updateEpic(epic1);

        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        taskManager.updateSubtask(subtask3);

        // Вывод списков задач, эпиков и подзадач с измененными статусами
        System.out.println();
        System.out.println("После обновления:");
        printAllTasks();

        //Вывод истории просмотров
        System.out.println();
        printHistoryView();

        //Вывод истории после удаления задачи и эпика с тремя подзадачами
        System.out.println();
        taskManager.deleteTask(1);
        inMemoryTaskManager.deleteTask(1);
        taskManager.deleteEpic(3);
        inMemoryTaskManager.deleteEpic(3);
        printHistoryAfterDelete();
    }

    //Вывод списков задач, эпиков и подзадач
    public static void printAllTasks() {
        System.out.println("Все задачи:");
        for (Task task : inMemoryTaskManager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Все эпики:");
        for (Epic epic : inMemoryTaskManager.getAllEpics()) {
            System.out.println(epic);

            for (Task task : inMemoryTaskManager.getSubtaskByEpic(epic.getId())) {
                System.out.println("-->" + task);
            }
        }
        System.out.println("Все подзадачи:");
        for (Task subtask : inMemoryTaskManager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }

    //Метод вывода истории последних просмотренных задач (в истории не должно быть повторов)
    public static void printHistoryView() {
        inMemoryTaskManager.getTaskById(2);
        inMemoryTaskManager.getTaskById(1);
        inMemoryTaskManager.getEpicById(3);
        inMemoryTaskManager.getSubtaskById(6);
        inMemoryTaskManager.getSubtaskById(4);
        inMemoryTaskManager.getTaskById(1);
        inMemoryTaskManager.getSubtaskById(5);
        inMemoryTaskManager.getEpicById(7);
        inMemoryTaskManager.getTaskById(1);
        inMemoryTaskManager.getSubtaskById(4);

        System.out.println();
        System.out.println("История просмотров:");
        for (Task task : inMemoryTaskManager.getHistory()) {
            System.out.println(task);
        }
    }

    public static void printHistoryAfterDelete() {
        System.out.println();
        System.out.println("История просмотров после удаления задачи и эпика с подзадачами: ");
        for (Task task : inMemoryTaskManager.getHistory()) {
            System.out.println(task);
        }
    }
}