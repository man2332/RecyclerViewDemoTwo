package com.example.man2332.recyclerviewdemotwo;

public class Item {
    private String topic;
    private String totalTime;


    public Item(String topic, String totalTime) {
        this.topic = topic;
        this.totalTime = totalTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }
}
