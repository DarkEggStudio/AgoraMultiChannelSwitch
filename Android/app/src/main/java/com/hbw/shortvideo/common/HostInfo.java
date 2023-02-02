package com.hbw.shortvideo.common;

public class HostInfo {
    private String channelName;
    private int userId;
    private int imageUrl;

    public HostInfo() {

    }

    public HostInfo(String channelName, int userId, int imageUrl) {
        this.channelName = channelName;
        this.userId = userId;
        this.imageUrl = imageUrl;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(int imageUrl) {
        this.imageUrl = imageUrl;
    }
}
