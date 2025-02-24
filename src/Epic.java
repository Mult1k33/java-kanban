import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Integer> subtasks;


    public Epic(String title, String description) {
        super(title, description);
        this.subtasks = new ArrayList<>();
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask.getId());
    }

    public ArrayList<Integer> getSubtasks() {
        return subtasks;
    }


    @Override
    public String toString() {
        return "Task.Epic{" +
                "subtasks=" + subtasks +
                "} " + super.toString();
    }
}
