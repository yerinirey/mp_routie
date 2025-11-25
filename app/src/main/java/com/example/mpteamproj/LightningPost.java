package com.example.mpteamproj;

public class LightningPost {

    private String id;

    // Firestore 문서값들
    private String hostUid;        // 진짜 UID (쿼리용)
    private String hostNickname;   // 문서에 저장된 당시 닉네임

    private String title;
    private String description;
    private Long createdAt;
    private Long eventTime;
    private String routeId;
    private String routeTitle;
    private String locationDesc;

    // 참가 관련
    private int maxParticipants;
    private int participantCount;
    private boolean joined;

    public LightningPost() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // UID (쿼리용)
    public String getHostUidRaw() {
        return hostUid != null ? hostUid : "";
    }

    public void setHostUid(String hostUid) {
        this.hostUid = hostUid;
    }

    // 표시용 닉네임 (users 컬렉션에서 가져온 값 저장)
    public String getHostNickname() {
        return hostNickname != null ? hostNickname : "";
    }

    public void setHostNickname(String hostNickname) {
        this.hostNickname = hostNickname;
    }

    public String getHostUid() {
        if (hostNickname != null && !hostNickname.isEmpty()) {
            return hostNickname;  // 닉네임 우선
        }
        if (hostUid != null && !hostUid.isEmpty()) {
            return hostUid;       // 닉네임이 없으면 UID fallback
        }
        return "";
    }

    // 나머지 getter/setter 들 (필요한 것만 적을게)

    public String getTitle() { return title != null ? title : ""; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getEventTime() { return eventTime; }
    public void setEventTime(Long eventTime) { this.eventTime = eventTime; }

    public String getRouteId() { return routeId != null ? routeId : ""; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getRouteTitle() { return routeTitle != null ? routeTitle : ""; }
    public void setRouteTitle(String routeTitle) { this.routeTitle = routeTitle; }

    public String getLocationDesc() { return locationDesc != null ? locationDesc : ""; }
    public void setLocationDesc(String locationDesc) { this.locationDesc = locationDesc; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }

    public boolean isJoined() { return joined; }
    public void setJoined(boolean joined) { this.joined = joined; }
}
