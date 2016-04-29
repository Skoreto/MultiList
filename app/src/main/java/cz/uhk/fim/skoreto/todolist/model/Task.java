package cz.uhk.fim.skoreto.todolist.model;

import java.util.Date;

/**
 * Created by Tomas.
 */
public class Task {

    private int id;

    private String name;

    private String description;

    private int listId;

    private int completed;

    private String photoName;

    private String recordingName;

    private Date dueDate;

    public Task(){
    }

    public Task(int id, String name, String description, int listId, int completed, String photoName, String recordingName, Date dueDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.listId = listId;
        this.completed = completed;
        this.photoName = photoName;
        this.recordingName = recordingName;
        this.dueDate = dueDate;
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

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getRecordingName() {
        return recordingName;
    }

    public void setRecordingName(String recordingName) {
        this.recordingName = recordingName;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
}
