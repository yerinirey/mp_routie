package com.example.mpteamproj;

public class LightningPost {

    private String id;       // Firestore 문서 ID
    private String title;
    private String place;
    private String timeText;
    private String hostUid;
    private long createdAt;

    public LightningPost() {
        // Firestore에서 객체로 변환할 때 필요
    }

    public LightningPost(String title, String place, String timeText, String hostUid, long createdAt) {
        this.title = title;
        this.place = place;
        this.timeText = timeText;
        this.hostUid = hostUid;
        this.createdAt = createdAt;
    }

    // getter / setter

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public String getTitle() {
        return title;
    }

    public String getPlace() {
        return place;
    }

    public String getTimeText() {
        return timeText;
    }

    public String getHostUid() {
        return hostUid;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }

    public void setHostUid(String hostUid) {
        this.hostUid = hostUid;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
