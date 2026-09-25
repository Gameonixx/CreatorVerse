package com.creatorverse.notification.dto;

public class NotificationUnreadCountResponse {
    private long count;

    public NotificationUnreadCountResponse() {}

    public NotificationUnreadCountResponse(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
