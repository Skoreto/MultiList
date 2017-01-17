package cz.uhk.fim.skoreto.todolist.model;

import java.util.Date;

/**
 * Trida reprezentujici zadany ukol.
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

    private Date notificationDate;

    private int taskPlaceId;

    public Task(){
    }

    public Task(int id, String name, String description, int listId, int completed,
                String photoName, String recordingName, Date dueDate, Date notificationDate,
                int taskPlaceId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.listId = listId;
        this.completed = completed;
        this.photoName = photoName;
        this.recordingName = recordingName;
        this.dueDate = dueDate;
        this.notificationDate = notificationDate;
        this.taskPlaceId = taskPlaceId;
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

    public Date getNotificationDate() {
        return notificationDate;
    }

    public void setNotificationDate(Date notificationDate) {
        this.notificationDate = notificationDate;
    }

    public int getTaskPlaceId() {
        return taskPlaceId;
    }

    public void setTaskPlaceId(int taskPlaceId) {
        this.taskPlaceId = taskPlaceId;
    }
}
