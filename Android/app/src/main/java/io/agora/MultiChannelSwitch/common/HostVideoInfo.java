package io.agora.MultiChannelSwitch.common;

import android.view.SurfaceView;

import io.agora.rtc2.RtcConnection;

public class HostVideoInfo {
    private HostInfo host;
    private VideoLayout hostVideo;
    private boolean isJoined = false;
    private RtcConnection connection;
    private int position;
    private SurfaceView surfaceView;
    private boolean isVisible = false;

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

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

    public HostVideoInfo(HostInfo host, VideoLayout hostVideo, int position) {
        this.host = host;
        this.hostVideo = hostVideo;
        this.position = position;
    }

    public HostVideoInfo(HostInfo host, SurfaceView hostVideo, int position) {
        this.host = host;
        this.surfaceView = hostVideo;
        this.position = position;
    }

    public HostInfo getHost() {
        return host;
    }

    public void setHost(HostInfo host) {
        this.host = host;
    }

    public VideoLayout getHostVideo() {
        return hostVideo;
    }

    public void setHostVideo(VideoLayout hostVideo) {
        this.hostVideo = hostVideo;
    }


    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

}
