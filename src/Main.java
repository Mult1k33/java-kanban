public class Main {

    public static void main(String[] args) {

        System.out.println("Поехали!");

        TaskManager taskManager = new TaskManager();

        //Создние двух задач, эпика с двумя подзадачами и эпика с одной подзадачей
        Task task1 = new Task("Стать разработчиком Java", "Пройти обучение в ЯП");
        taskManager.createTask(task1);
        Task task2 = new Task("Вакансия 114597", "Закрыть вакансию SA");
        taskManager.createTask(task2);

        Epic epic1 = new Epic("ФЗ спринта 4", "Написать бэкенд для трекера задач");
        taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Написать код программы", epic1.getId());
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Пройти ревью", epic1.getId());
        taskManager.createSubtask(subtask2);

        Epic epic2 = new Epic("Б.Эккель", "Дополнительня литература");
        taskManager.createEpic(epic2);

        Subtask subtask3 = new Subtask("Философия Java", "Прочитать 2 главы", epic2.getId());
        taskManager.createSubtask(subtask3);


        //Вывод списков задач, эпиков и подзадач
        System.out.println("Все задачи:");
        System.out.println(taskManager.getAllTasks());
        System.out.println("Все эпики:");
        System.out.println(taskManager.getAllEpics());
        System.out.println("Все подзадачи:");
        System.out.println(taskManager.getAllSubtasks());


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
        taskManager.updateEpic(epic2);

        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        taskManager.updateSubtask(subtask3);

        // Вывод списков задач, эпиков и подзадач с измененными статусами
        System.out.println("Все задачи:");
        System.out.println(taskManager.getAllTasks());
        System.out.println("Все эпики:");
        System.out.println(taskManager.getAllEpics());
        System.out.println("Все подзадачи:");
        System.out.println(taskManager.getAllSubtasks());


        //Получение и вывод задач, эпиков и подзадач по id
        System.out.println("Задача:");
        System.out.println(taskManager.getTaskById(2));
        System.out.println("Эпик:");
        System.out.println(taskManager.getEpicById(3));
        System.out.println("Подзадача:");
        System.out.println(taskManager.getSubtaskById(4));

        //Получение и вывод всех подзадач определенного эпика
        System.out.println("Подзадача определенного эпика:");
        System.out.println(taskManager.getSubtaskByEpic(3));


        //Удаление одной задачи и одного эпика по id и
        taskManager.deleteTask(2);
        taskManager.deleteEpic(6);
        taskManager.deleteSubtask(4);

        // Вывод списков задач, эпиков и подзадач после удалений
        System.out.println("Все задачи:");
        System.out.println(taskManager.getAllTasks());
        System.out.println("Все эпики:");
        System.out.println(taskManager.getAllEpics());
        System.out.println("Все подзадачи:");
        System.out.println(taskManager.getAllSubtasks());


        //Удаление всех задач, эпиков и подзадач
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
        taskManager.deleteAllSubtasks();

        // Вывод списков задач, эпиков и подзадач после всех удалений
        System.out.println("Все задачи:");
        System.out.println(taskManager.getAllTasks());
        System.out.println("Все эпики:");
        System.out.println(taskManager.getAllEpics());
        System.out.println("Все подзадачи:");
        System.out.println(taskManager.getAllSubtasks());

    }
}
