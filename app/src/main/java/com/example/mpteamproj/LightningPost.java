package com.example.mpteamproj;

public class LightningPost {
    private String id;
    private String title;
    private String description;
    private String hostUid;
    private Long createdAt;
    private String routeId;
    private String routeTitle;
    private String routeStart;
    private String routeEnd;

    private int participantCount;
    private Long eventTime;
    private boolean joined;   // 현재 로그인 유저가 참가중인지

    public LightningPost() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHostUid() { return hostUid; }
    public void setHostUid(String hostUid) { this.hostUid = hostUid; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getRouteTitle() { return routeTitle; }
    public void setRouteTitle(String routeTitle) { this.routeTitle = routeTitle; }

    public String getRouteStart() { return routeStart; }
    public void setRouteStart(String routeStart) { this.routeStart = routeStart; }

    public String getRouteEnd() { return routeEnd; }
    public void setRouteEnd(String routeEnd) { this.routeEnd = routeEnd; }

    // Participants
    public int getParticipantCount() {return participantCount;}

    public void setParticipantCount(int participantCount) { this.participantCount = participantCount;}

    public boolean isJoined() {return joined;}

    public void setJoined(boolean joined) {this.joined = joined;}

    public Long getEventTime() { return eventTime;};

    public void setEventTime(Long eventTime) { this.eventTime = eventTime;};
}
