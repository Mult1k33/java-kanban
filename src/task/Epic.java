package task;

import enums.Status;

import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Subtask> subtasks = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }



    public Epic(Integer id, String title, String description, Status status) {
        super(id, title, description, status);
        this.subtasks = subtasks;
    }


    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void clearSubtasks() {
        subtasks.clear();
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    @Override
    public String toString() {
        return "Task.Epic{" +
                "subtasks=" + subtasks +
                "} " + super.toString();
    }
}