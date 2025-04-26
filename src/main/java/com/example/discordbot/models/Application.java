package com.example.discordbot.models;

import org.mongojack.Id;
import org.bson.types.ObjectId;

public class Application {
    @Id
    private ObjectId id;

    private String userId;
    private String name;
    private String skills;
    private String languages;
    private String showcase;
    private String info;
    private String channelId;
    private Boolean reviewed = true;
    private long timestamp;

    public Application() {
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getShowcase() {
        return showcase;
    }

    public void setShowcase(String showcase) {
        this.showcase = showcase;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getChannelId() { return this.channelId; }

    public void setChannelId(String id) { this.channelId = id; }

    public Boolean isReviewed() {
        return this.reviewed;
    }
    
    public void setReviewed(Boolean reviewed) {
        this.reviewed = reviewed;
    }
}
