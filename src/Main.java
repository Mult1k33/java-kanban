import enums.Status;
import service.*;
import model.*;

public class Main {

    private static final TaskManager inMemoryTaskManager = Managers.getDefault();

    public static void main(String[] args) {

        // Создание двух задач, эпика с тремя подзадачами и эпика без подзадач
        Task task1 = new Task("Стать разработчиком Java", "Пройти обучение в ЯП");
        inMemoryTaskManager.createTask(task1);
        Task task2 = new Task("Вакансия 114597", "Закрыть вакансию SA");
        inMemoryTaskManager.createTask(task2);

        Epic epic1 = new Epic("ФЗ спринта 6", "Написать бэкенд для трекера задач");
        inMemoryTaskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Написать код программы", epic1.getId());
        inMemoryTaskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Сделать пул-реквест", epic1.getId());
        inMemoryTaskManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("Подзадача 3", "Пройти код-ревью", epic1.getId());
        inMemoryTaskManager.createSubtask(subtask3);

        Epic epic2 = new Epic("Б.Эккель", "Дополнительня литература");
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
        inMemoryTaskManager.updateTask(task1);
        inMemoryTaskManager.updateTask(task2);

        inMemoryTaskManager.updateEpic(epic1);

        inMemoryTaskManager.updateSubtask(subtask1);
        inMemoryTaskManager.updateSubtask(subtask2);
        inMemoryTaskManager.updateSubtask(subtask3);

        // Вывод списков задач, эпиков и подзадач с измененными статусами
        System.out.println();
        System.out.println("После обновления:");
        printAllTasks();

        //Вывод истории просмотров
        System.out.println();
        printHistoryView();

        //Вывод истории после удаления задачи и эпика с тремя подзадачами
        System.out.println();
        inMemoryTaskManager.deleteTask(1);
        inMemoryTaskManager.deleteEpic(3);
        printHistoryView();
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
}