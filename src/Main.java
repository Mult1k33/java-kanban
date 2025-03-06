import enums.Status;
import manager.*;
import task.*;

/*Доброго времени, Владимир!
Код претерпел некоторые изменения в сравнении с прошлым спринтом.
Создал пакеты для удобства работы и более четкой структуры проекта.
Изменил логику нескольких методов, т.к. смущали некоторые моменты.
Надеюсь, что данные решения не испортили картины.

Насчет тестов есть сомнения.
Если честно, совсем не могу понять, как написать эти 2 теста:
1) Проверить, что объект Epic нельзя добавить в самого себя в виде подзадачи;
2) Проверить, что объект Subtask нельзя сделать своим же эпиком.
 */

public class Main {

    private static final TaskManager inMemoryTaskManager = Managers.getDefault();

    public static void main(String[] args) {

        //Создние двух задач, эпика с двумя подзадачами и эпика с одной подзадачей
        Task task1 = new Task("Стать разработчиком Java", "Пройти обучение в ЯП");
        inMemoryTaskManager.createTask(task1);
        Task task2 = new Task("Вакансия 114597", "Закрыть вакансию SA");
        inMemoryTaskManager.createTask(task2);

        Epic epic1 = new Epic("ФЗ спринта 4", "Написать бэкенд для трекера задач");
        inMemoryTaskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Написать код программы", epic1.getId());
        inMemoryTaskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Пройти ревью", epic1.getId());
        inMemoryTaskManager.createSubtask(subtask2);

        Epic epic2 = new Epic("Б.Эккель", "Дополнительня литература");
        inMemoryTaskManager.createEpic(epic2);

        Subtask subtask3 = new Subtask("Философия Java", "Прочитать 2 главы", epic2.getId());
        inMemoryTaskManager.createSubtask(subtask3);

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
        inMemoryTaskManager.updateEpic(epic2);

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

            for (Task task : inMemoryTaskManager.getSubtaskByEpic(epic)) {
                System.out.println("-->" + task);
            }
        }
        System.out.println("Все подзадачи:");
        for (Task subtask : inMemoryTaskManager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }

    //Метод вывода истории последних просмотренных 10 задач (добавлены 11 для проверки)
    public static void printHistoryView() {
        inMemoryTaskManager.getTaskById(2);
        inMemoryTaskManager.getTaskById(1);
        inMemoryTaskManager.getEpicById(3);
        inMemoryTaskManager.getSubtaskById(7);
        inMemoryTaskManager.getSubtaskById(4);
        inMemoryTaskManager.getTaskById(2);
        inMemoryTaskManager.getSubtaskById(5);
        inMemoryTaskManager.getEpicById(6);
        inMemoryTaskManager.getTaskById(2);
        inMemoryTaskManager.getSubtaskById(4);
        inMemoryTaskManager.getSubtaskById(7);

        System.out.println();
        System.out.println("История:");
        for (Task task : inMemoryTaskManager.getHistory()) {
            System.out.println(task);
        }
    }
}