
public class Subtask extends Task {

    private Integer EPIC_ID;

    public Subtask(String title, String description, Integer epicId) {
        super(title, description);
        this.EPIC_ID = epicId;
    }


    public Integer getEpicId() {
        return EPIC_ID;
    }

    @Override
    public String toString() {
        return "Task.Subtask{" +
                "epicId=" + EPIC_ID +
                "} " + super.toString();
    }
}