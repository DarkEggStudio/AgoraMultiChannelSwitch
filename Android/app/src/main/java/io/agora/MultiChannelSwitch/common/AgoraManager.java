package io.agora.MultiChannelSwitch.common;

import static io.agora.rtc2.Constants.RENDER_MODE_HIDDEN;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcConnection;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.video.VideoCanvas;

public class AgoraManager {
    private static final String TAG = "AgoraManager";
    private static volatile AgoraManager sInstance = null;
    private String appId;
    private int areaCode;
    private RtcEngine engine;
    private RtcEngineEx engineEx;
    private RtcConnection rtcSecondConnection;
    private InnerRtcEngineEventHandler iRtcEngineEventHandler = new InnerRtcEngineEventHandler();
    private Context context;
    private boolean joined = false;
    private boolean secondJoined = false;
    private boolean ready = false;
    private List<HostInfo> hosts;

    public List<HostInfo> getHosts() {
        return hosts;
    }

    public void setHosts(List<HostInfo> hosts) {
        this.hosts = hosts;
    }



    private final IRtcEngineEventHandler secondChannelEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {

        }

        public void onLeaveChannel(RtcStats stats) {

            Log.i("RTC", "Left the channel ");

        }

        @Override
        public void onUserJoined(int uid, int elapsed) {

            Log.i("RTC", String.format("user %d joined!", uid));

        }

        @Override
        public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {

        }
    };

    private ArrayList<Integer> users = new ArrayList<Integer>();

    public static AgoraManager getInstance() {
        if (sInstance == null) {
            synchronized (AgoraManager.class) {
                if (sInstance == null) {
                    sInstance = new AgoraManager();
                }
            }
        }
        return sInstance;
    }

    private AgoraManager() {

    }


    public boolean createEngine(Context context, String appId, int areaCode)
    {
        try{
            /**Creates an RtcEngine instance.
             * @param context The context of Android Activity
             * @param appId The App ID issued to you by Agora. See <a href="https://docs.agora.io/en/Agora%20Platform/token#get-an-app-id">
             *              How to get the App ID</a>
             * @param handler IRtcEngineEventHandler is an abstract class providing default implementation.
             *                The SDK uses this class to report to the app on SDK runtime events.*/
            //String appId = getString(R.string.agora_app_id);
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = context.getApplicationContext();
            config.mAppId = appId;
            config.mEventHandler = iRtcEngineEventHandler;
            config.mAreaCode = areaCode;
            engineEx = (RtcEngineEx) RtcEngine.create(config);

            this.ready = true;
            this.appId = appId;
            this.areaCode = areaCode;
            this.context = context;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public ArrayList<Integer> getAllUsers()
    {
        return users;
    }

    public void addRtcEngineEventHandler(IRtcEngineEventHandler handler)
    {
        if (engineEx == null) {
            Log.e(TAG, "RTC engine has not been initialized");
        } else {
            engineEx.addHandler(handler);
        }
    }

    public void removeRtcEngineEventHandler(IRtcEngineEventHandler handler)
    {
        if (engineEx == null) {
            Log.e(TAG, "RTC engine has not been initialized");
        } else {
            engineEx.removeHandler(handler);
        }
    }



    public boolean isReady()
    {
        return ready;
    }

    public boolean isJoined()
    {
        return joined;
    }

    public void joinChannelEx(RtcConnection connection, String token, IRtcEngineEventHandler handler)
    {
        if (engineEx == null) {
            Log.e(TAG, "RTC engine has not been initialized");
            return;
        }
            ChannelMediaOptions mediaOptions = new ChannelMediaOptions();


                mediaOptions.autoSubscribeAudio = false;
                mediaOptions.autoSubscribeVideo = false;
                mediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;

            engineEx.joinChannelEx(token, connection, mediaOptions, handler);

            joined = true;

    }

    public void updateChannelEx(RtcConnection connection, boolean subscribed)
    {
        if (engineEx == null) {
            Log.e(TAG, "RTC engine has not been initialized");
            return;
        }
        ChannelMediaOptions mediaOptions = new ChannelMediaOptions();

        mediaOptions.autoSubscribeAudio = subscribed;
        mediaOptions.autoSubscribeVideo = subscribed;
        mediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;

        engineEx.updateChannelMediaOptionsEx(mediaOptions, connection);
    }

    public void unMuteAudioEx(RtcConnection connection, boolean muted)
    {
        if (engineEx == null) {
            Log.e(TAG, "RTC engine has not been initialized");
            return;
        }
        engineEx.muteAllRemoteAudioStreamsEx(muted, connection);
    }

    public void leaveChannelEx(RtcConnection connection)
    {
        if (engineEx == null) {
            Log.e(TAG, "RTC engine has not been initialized");
            return;
        }
        engineEx.leaveChannelEx(connection);
        joined = false;
    }

    public void setupRemoteVideoEx(RtcConnection connection, VideoCanvas view)
    {
        if (engineEx == null) {
            Log.e(TAG, "RTC engine has not been initialized");
            return;
        }
        engineEx.setupRemoteVideoEx(view, connection);
    }

    public void setUidViewEx(RtcConnection connection, int uid, SurfaceView view)
    {
        // Setup remote video to render
        view.setZOrderOnTop(true);
        setupRemoteVideoEx(connection, new VideoCanvas(view, RENDER_MODE_HIDDEN, uid));
    }

    public void setUidViewEx(RtcConnection connection, int uid, VideoLayout view)
    {
        /**Display remote video stream*/
        SurfaceView surfaceView = null;
        // Create render view by RtcEngine
        surfaceView = new SurfaceView(this.context);
        surfaceView.setZOrderMediaOverlay(true);
        //surfaceView.setZOrderOnTop(true);
        view.setReportUid(uid);

        // Add to the remote container
        view.addView(surfaceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // Setup remote video to render
        //engine.setupRemoteVideo(new VideoCanvas(surfaceView, RENDER_MODE_HIDDEN, uid));
        setupRemoteVideoEx(connection, new VideoCanvas(surfaceView, RENDER_MODE_HIDDEN, uid));
    }

    public void destroy()
    {
        engineEx = null;
    }

    public void addUser(Integer uid) {

        if(!users.contains(uid)) {
            users.add(uid);
        }
    }

    public void removeUser(Integer uid)
    {
        if(users.contains(uid))
            users.remove(uid);
    }

    public void setAppId(String appId)
    {
        this.appId = appId;
    }

    public void setAreaCode(int areaCode)
    {
        this.areaCode = areaCode;
    }

    /**
     * IRtcEngineEventHandler is an abstract class providing default implementation.
     * The SDK uses this class to report to the app on SDK runtime events.
     */
    private class InnerRtcEngineEventHandler extends IRtcEngineEventHandler {
        /**
         * Reports a warning during SDK runtime.
         * Warning code: https://docs.agora.io/en/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_warn_code.html
         */
        @Override
        public void onWarning(int warn) {
            Log.w(TAG, String.format("onWarning code %d message %s", warn, RtcEngine.getErrorDescription(warn)));
        }

        @Override
        public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
            Log.e(TAG, String.format("onFirstRemoteVideoFrame %d ", uid));
        }

        /**
         * Reports an error during SDK runtime.
         * Error code: https://docs.agora.io/en/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_error_code.html
         */
        @Override
        public void onError(int err) {
            Log.e(TAG, String.format("onError code %d message %s", err, RtcEngine.getErrorDescription(err)));
        }

        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) joins the channel.
         *
         * @param uid     ID of the user whose audio state changes.
         * @param elapsed Time delay (ms) from the local user calling joinChannel/setClientRole
         *                until this callback is triggered.
         */
        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            Log.i(TAG, "onUserJoined->" + uid);
            //showLongToast(String.format("user %d joined!", uid));
            addUser(uid);

        }

        /**
         * Occurs when a remote user (Communication)/host (Live Broadcast) leaves the channel.
         *
         * @param uid    ID of the user whose audio state changes.
         * @param reason Reason why the user goes offline:
         *               USER_OFFLINE_QUIT(0): The user left the current channel.
         *               USER_OFFLINE_DROPPED(1): The SDK timed out and the user dropped offline because no data
         *               packet was received within a certain period of time. If a user quits the
         *               call and the message is not passed to the SDK (due to an unreliable channel),
         *               the SDK assumes the user dropped offline.
         *               USER_OFFLINE_BECOME_AUDIENCE(2): (Live broadcast only.) The client role switched from
         *               the host to the audience.
         */
        @Override
        public void onUserOffline(int uid, int reason) {
            Log.i(TAG, String.format("user %d offline! reason:%d", uid, reason));
            removeUser(uid);
        }

    }
}
