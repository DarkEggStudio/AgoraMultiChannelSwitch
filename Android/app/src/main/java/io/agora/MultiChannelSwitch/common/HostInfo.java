package io.agora.MultiChannelSwitch.common;

/**
 * Host Model
 * including channel Id
 */
public class HostInfo {


    private String channelName;
    private String channelId;
    private int userId;


    private int imageId;
    private String imageUrl;

    public HostInfo() {

    }

    public HostInfo(String channelId, int userId, int imageId) {
        this.channelId = channelId;
        this.userId = userId;
        this.imageId = imageId;
    }

    public HostInfo(String channelId, int userId, String imageUrl) {
        this.channelId = channelId;
        this.userId = userId;
        this.imageUrl = imageUrl;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}
