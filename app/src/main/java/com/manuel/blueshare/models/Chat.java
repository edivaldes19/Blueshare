package com.manuel.blueshare.models;

import java.util.ArrayList;

public class Chat {
    private String id, idUser1, idUser2;
    private long timestamp;
    private ArrayList<String> ids;
    private int idNotification;

    public Chat() {
    }

    public Chat(String id, String idUser1, String idUser2, long timestamp, ArrayList<String> ids, int idNotification) {
        this.id = id;
        this.idUser1 = idUser1;
        this.idUser2 = idUser2;
        this.timestamp = timestamp;
        this.ids = ids;
        this.idNotification = idNotification;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUser1() {
        return idUser1;
    }

    public void setIdUser1(String idUser1) {
        this.idUser1 = idUser1;
    }

    public String getIdUser2() {
        return idUser2;
    }

    public void setIdUser2(String idUser2) {
        this.idUser2 = idUser2;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<String> getIds() {
        return ids;
    }

    public void setIds(ArrayList<String> ids) {
        this.ids = ids;
    }

    public int getIdNotification() {
        return idNotification;
    }

    public void setIdNotification(int idNotification) {
        this.idNotification = idNotification;
    }
}