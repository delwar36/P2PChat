package com.example.wifidirectchat.model;

import android.arch.persistence.room.ColumnInfo;

import java.util.Date;

public class ChatHistoryEntity {
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "date")
    private Date startDate;

    @ColumnInfo(name = "name")
    private String name;
//
//    public String getNickname() {
//        return nickname;
//    }
//
//    public void setNickname(String nickname) {
//        this.nickname = nickname;
//    }

//    @ColumnInfo(name = "nickname")
//    private String nickname;

    @ColumnInfo(name = "messageCount")
    private int messageCount;

    public ChatHistoryEntity(String name, Date startDate, int messageCount) {
        this.startDate = startDate;
        this.name = name;
        this.messageCount = messageCount;
        //this.nickname = name;
    }


    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
