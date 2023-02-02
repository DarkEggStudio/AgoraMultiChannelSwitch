package io.agora.MultiChannelSwitch.common;

import io.agora.rtc2.RtcConnection;

public class HostVideoInfo {
    private HostInfo host;
    private VideoReportLayout hostVideo;
    private boolean isJoined = false;
    private RtcConnection connection;
    private int position;


    public boolean isJoined() {
        return isJoined;
    }

    public void setJoined(boolean joined) {
        isJoined = joined;
    }



    public RtcConnection getConnection() {
        return connection;
    }

    public void setConnection(RtcConnection connection) {
        this.connection = connection;
    }


    public HostVideoInfo() {

    }

    public HostVideoInfo(HostInfo host, VideoReportLayout hostVideo, int position) {
        this.host = host;
        this.hostVideo = hostVideo;
        this.position = position;
    }

    public HostInfo getHost() {
        return host;
    }

    public void setHost(HostInfo host) {
        this.host = host;
    }

    public VideoReportLayout getHostVideo() {
        return hostVideo;
    }

    public void setHostVideo(VideoReportLayout hostVideo) {
        this.hostVideo = hostVideo;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    }
