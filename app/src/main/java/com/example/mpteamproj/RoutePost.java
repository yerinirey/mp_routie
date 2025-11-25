package com.example.mpteamproj;

public class RoutePost {

    private String id;
    private String title;
    private String startPlace;
    private String endPlace;
    private String startLabel;
    private String endLabel;
    private String memo;
    private String hostUid;
    private Long createdAt;

    public RoutePost() {
        // Firestore용 기본 생성자
    }

    // 필요하다면 생성자 하나 정도
    public RoutePost(String title, String startPlace, String endPlace,
                     String memo, String hostUid, Long createdAt) {
        this.title = title;
        this.startPlace = startPlace;
        this.endPlace = endPlace;
        this.memo = memo;
        this.hostUid = hostUid;
        this.createdAt = createdAt;
    }

    // --- getter / setter ---

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) { this.title = title; }

    public String getStartPlace() {
        return startPlace;
    }

    public void setStartPlace(String startPlace) { this.startPlace = startPlace; }

    public String getEndPlace() {
        return endPlace;
    }

    public void setEndPlace(String endPlace) { this.endPlace = endPlace; }

    public String getMemo() {
        return memo;
    }

    public String getStartLabel() { return startLabel != null ? startLabel : ""; }
    public void setStartLabel(String startLabel) { this.startLabel = startLabel; }

    public String getEndLabel() { return endLabel != null ? endLabel : ""; }
    public void setEndLabel(String endLabel) { this.endLabel = endLabel; }

    public void setMemo(String memo) { this.memo = memo; }

    public String getHostUid() {
        return hostUid;
    }

    public void setHostUid(String hostUid) { this.hostUid = hostUid; }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}
