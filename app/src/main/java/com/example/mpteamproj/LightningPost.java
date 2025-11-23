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
}
