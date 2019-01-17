package com.example.man2332.recyclerviewdemotwo;

public class Topic {
    private String topicName;
    private String totalTime;


    public Topic(String topic, String totalTime) {
        this.topicName = topic;
        this.totalTime = totalTime;
    }

    public String getTopic() {
        return topicName;
    }

    public void setTopic(String topic) {
        this.topicName = topic;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }
}
