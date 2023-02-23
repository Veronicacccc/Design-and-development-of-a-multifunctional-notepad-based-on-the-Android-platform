package com.example.daily.tasks;

public class TaskItem {
    private int id;
    private String type;
    private String content;
    private String info;
    private int status;

    public TaskItem(int id, String type, String content, String info, int status){
        this.id = id;
        this.type = type;
        this.content = content;
        this.status = status;
        this.info = info;
    }
    public TaskItem(String type , String content, String info, int status){
        this.type = type;
        this.content = content;
        this.status = status;
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
