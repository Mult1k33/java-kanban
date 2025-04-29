import enums.*;
import service.*;
import model.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;


public class Main {
    private static final TaskManager inMemoryTaskManager = Managers.getDefault();
    private static TaskManager fileTaskManager;

    public static void main(String[] args) throws IOException {

        File file = File.createTempFile("task", ".csv");
        fileTaskManager = Managers.loadFromFile(file);

        // Создание двух задач
        Task task1 = new Task("Стать разработчиком Java", "Пройти обучение в ЯП");
        fileTaskManager.createTask(task1);
        inMemoryTaskManager.createTask(task1);

        Task task2 = new Task("Вакансия 114597", "Закрыть вакансию SA",
                LocalDateTime.now(), Duration.ofMinutes(1440));
        fileTaskManager.createTask(task2);
        inMemoryTaskManager.createTask(task2);

        // Создание эпика с тремя подзадачами
        Epic epic1 = new Epic("ФЗ спринта 6", "Написать бэкенд для трекера задач");
        fileTaskManager.createEpic(epic1);
        inMemoryTaskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Написать код программы",
                task2.getEndTime(), Duration.ofMinutes(600), epic1.getId());
        fileTaskManager.createSubtask(subtask1);
        inMemoryTaskManager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача 2", "Сделать пул-реквест", epic1.getId());
        fileTaskManager.createSubtask(subtask2);
        inMemoryTaskManager.createSubtask(subtask2);

        Subtask subtask3 = new Subtask("Подзадача 3", "Пройти код-ревью",
                subtask1.getEndTime(), Duration.ofMinutes(1440), epic1.getId());
        fileTaskManager.createSubtask(subtask3);
        inMemoryTaskManager.createSubtask(subtask3);

        // Создание эпика без подзадач
        Epic epic2 = new Epic("Б.Эккель", "Дополнительня литература");
        fileTaskManager.createEpic(epic2);
        inMemoryTaskManager.createEpic(epic2);

        // Проверка, что все задачи создались
        printAllTasks();
        System.out.println();

        // Проверка, что список приоритетных задач выводится корректно
        inMemoryTaskManager.getPrioritizedTasks();
        printPrioritizedTasks();

        //Изменение статусов задач и подзадач
        task1.setStatus(Status.IN_PROGRESS);
        task2.setStatus(Status.DONE);

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.IN_PROGRESS);
        subtask3.setStatus(Status.IN_PROGRESS);

        // Обновление статусов задач, эпиков и подзадач
        updateTaskStatuses();

        // Вывод списков задач, эпиков и подзадач с измененными статусами
        System.out.println();
        System.out.println("После обновления:");
        printAllTasks();

        //Вывод истории просмотров
        printHistoryView();

        // Проверка списка приоритетных задач после обновления
        System.out.println();
        printPrioritizedTasks();

        //Вывод истории после удаления задачи и эпика с тремя подзадачами
        System.out.println();
        inMemoryTaskManager.deleteTask(1);
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
        System.out.println();
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
        inMemoryTaskManager.getHistory().forEach(System.out::println);
        System.out.println();
    }

    public static void printHistoryAfterDelete() {
        System.out.println("История просмотров после удаления задачи и эпика с подзадачами: ");
        inMemoryTaskManager.getHistory().forEach(System.out::println);
    }

    // Метод обновления созданных задач
    public static void updateTaskStatuses() {
        Task task1 = inMemoryTaskManager.getTaskById(1);
        Task task2 = inMemoryTaskManager.getTaskById(2);

        task1.setStatus(Status.IN_PROGRESS);
        task2.setStatus(Status.DONE);

        Subtask subtask1 = inMemoryTaskManager.getSubtaskById(4);
        Subtask subtask2 = inMemoryTaskManager.getSubtaskById(5);
        Subtask subtask3 = inMemoryTaskManager.getSubtaskById(6);

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.IN_PROGRESS);
        subtask3.setStatus(Status.IN_PROGRESS);

        inMemoryTaskManager.updateTask(task1);
        inMemoryTaskManager.updateTask(task2);
        inMemoryTaskManager.updateSubtask(subtask1);
        inMemoryTaskManager.updateSubtask(subtask2);
        inMemoryTaskManager.updateSubtask(subtask3);
    }

    // Метод отображения приоритетных задач
    public static void printPrioritizedTasks() {
        System.out.println("Список приоритетных задач:");
        inMemoryTaskManager.getPrioritizedTasks().forEach(System.out::println);
        System.out.println();
    }
}