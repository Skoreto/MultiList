package cz.uhk.fim.skoreto.todolist.model;

/**
 * Trida reprezentujici seznam ukolu.
 * Created by Tomas.
 */
public class TaskList {

    private int id;

    private String name;

    public TaskList() {
    }

    public TaskList(int id, String name) {
        this.id = id;
        this.name = name;
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

    /**
     * Prepsani pro ucely zobrazeni nazvu seznamu ukolu ve spinneru detailu ukolu.
     */
    @Override
    public String toString() {
        return this.name;
    }
}
