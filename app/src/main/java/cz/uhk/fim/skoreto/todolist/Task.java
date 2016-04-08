package cz.uhk.fim.skoreto.todolist;

/**
 * Created by Tomas on 8.4.2016.
 */
public class Task {

    private int id;

    private String name;

    private String description;

    private int listId;

    private int completed;

    public Task(){

    }

    public Task(int id, String name, String description, int listId, int completed) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.listId = listId;
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }
}
