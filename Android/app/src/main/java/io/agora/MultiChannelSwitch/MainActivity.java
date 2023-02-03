package io.agora.MultiChannelSwitch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.agora.MultiChannelSwitch.common.AgoraManager;
import io.agora.MultiChannelSwitch.common.HostInfo;
import io.agora.MultiChannelSwitch.common.HostVideoInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;

import io.agora.MultiChannelSwitch.common.VideoLayout;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcConnection;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;

public class MainActivity extends AppCompatActivity {
    protected Handler handler;
    private VerticalViewPager vp;
    private HostVideoInfo currentHostVideoInfo;
    private VerticalPageAdapter verticalPagerAdapter;
    private int currentFlagPosition;//传递过来播放第几个

    private int mCurrentPosition;//当前正在播放第几个主播
    private int mPreviousPosition = -1;//上一个播放的主播
    private float lastValue;
    private boolean isScrolling = false;  //viewpager是否滑动
    private boolean isBottomToTop = false;  //从下向上滑动
    private boolean isTopToBottom = false;  //从上向下滑动

    private void initSampleHosts()
    {
        List<HostInfo> hosts = new ArrayList<>();
        int hostCount = 3; // set number to 12 for loop

        String[] channels = new String[] {"AoVf4Hfdy9EbyDLh", "RxhMtitAzUwk1nBR", "IBbgSrJic1gNbGaG"};
        int[] images = new int[] {R.drawable.image1, R.drawable.image2, R.drawable.image3};

        for(int i=0;i<hostCount;i++)
        {
            hosts.add(new HostInfo(channels[i], 900001, images[i]));
        }

        AgoraManager.getInstance().setHosts(hosts);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());
        /**
         * Load sample hosts
         */
        initSampleHosts();

        setContentView(R.layout.activity_main);
        vp = findViewById(R.id.main_vp);

        /**
         * Init Agora Service Manager
         */
        if(!AgoraManager.getInstance().isReady()) {

            int areaCode = RtcEngineConfig.AreaCode.AREA_CODE_CN;

            AgoraManager.getInstance().createEngine(getApplicationContext(), getString(R.string.agora_app_id), areaCode);

            Log.i("DEMO", "init RTC engine");

        }

        /**
         * Init ViewPager
         */
        initViewPager();
    }

    private void initViewPager() {
        currentFlagPosition = getIntent().getIntExtra("currentPosition", 0);

        verticalPagerAdapter = new VerticalPageAdapter();

        vp.setAdapter(verticalPagerAdapter);
        vp.setOffscreenPageLimit(1);
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Log.i("PageScrolling", "onPageScrolled i:"+i+" v:"+v+" i1:"+i1);
                // TODO: Join Channel based on Scrolling position

                if (positionOffset == 0.0){
                    return;
                }
                if (lastValue > positionOffset) {
                    // 递减，从上向下滑动
                    isBottomToTop = false;
                    isTopToBottom = true;

                    Log.i("PageScrolling", "TopToBottom");
                } else if (lastValue < positionOffset) {
                    // 递增，从下向上滑动
                    isBottomToTop = true;
                    isTopToBottom = false;
                    Log.i("PageScrolling", "BottomToTop");
                } else if (lastValue == positionOffset) {
                    isTopToBottom = isBottomToTop = false;
                }
                lastValue = positionOffset;
                isScrolling = true;

            }

            @Override
            public void onPageSelected(int position) {

                mPreviousPosition = mCurrentPosition;

                /**
                 * mute previous channel
                 * unsubscribe any hosts
                 * leave channel
                 */
                HostVideoInfo hostVideoInfo = verticalPagerAdapter.findHostVideoInfo(mPreviousPosition);
                if(hostVideoInfo != null) {
//                    Log.i("DEMO", "mute Previous Host Audio:"+mPreviousPosition);
                    AgoraManager.getInstance().unMuteAudioEx(hostVideoInfo.getConnection(), true);
//                    Log.i("DEMO", "unSubscribe Channel:"+mPreviousPosition);
                    AgoraManager.getInstance().updateChannelEx(hostVideoInfo.getConnection(), false);


                    // Here, call agora leave channel function is not a good idea,
                    // because if user swipe back to previous one

                    // AgoraManager.getInstance().leaveChannelEx(hostVideoInfo.getConnection());
                    // hostVideoInfo.setJoined(false);
                    // Log.i("DEMO", "Leave channel:"+mPreviousPosition);
                }

                mCurrentPosition = position;
                Log.i("DEMO", "onPageSelected position:"+position);



            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //Log.i("PageScrolling", "onPageScrollStateChanged i:"+i);
            }
        });
        vp.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {

                if (position != 0) {
//                    Log.i("POSITION", "transformPage position:"+position);
                    return;
                }
                Log.i("DEMO", "transformPage position:"+mCurrentPosition);

                /**
                 * Here is the place when NEW page is fully displayed.
                 * Scrolling page is finished.
                 * So, subscribe channel host and unMute audio
                 * Setup display UI
                 */
                HostVideoInfo hostVideoInfo = verticalPagerAdapter.findHostVideoInfo(mCurrentPosition);
                if(hostVideoInfo != null) {

                    //Log.i("DEMO", "subscribe Channel:"+mCurrentPosition + " " +hostVideoInfo.getHost().getChannelId());
                    AgoraManager.getInstance().updateChannelEx(hostVideoInfo.getConnection(), true);
                    //Log.i("DEMO", "unmute Host Audio:"+mCurrentPosition);
                    AgoraManager.getInstance().unMuteAudioEx(hostVideoInfo.getConnection(), false);


                    currentHostVideoInfo = hostVideoInfo;
                }



            }

        });
        vp.setCurrentItem(currentFlagPosition);
    }

    private class VerticalPageAdapter extends PagerAdapter {
        private LinkedList<View> mViewCache = new LinkedList<>();
        ArrayList<HostVideoInfo> hostVideoList = new ArrayList<>();


        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            View view;
//            if (mViewCache.size() == 0) {
            view = LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_single_host, null, false);
//            } else {
//                view = mViewCache.removeFirst();
//            }
            view.setId(position);

            HostInfo host = AgoraManager.getInstance().getHosts().get(position);

            Log.i("instantiateItem", "instantiateItem position:"+position);
            Log.i("DEMO", "Init View:"+position);

            TextView videoTitle = view.findViewById(R.id.hostTitle);
            final ImageView coverPicture = view.findViewById(R.id.hostImage);
            videoTitle.setText("<  " + host.getChannelId());

            /**
             * This is for Internet Images
             */
//            if (!TextUtils.isEmpty(host.getImageUrl())) {
//                coverPicture.setVisibility(View.VISIBLE);
//                Glide.with(MainActivity.this).load(host.getImageUrl()).into(coverPicture);
//            }

            /**
             * Local images
             */
            coverPicture.setVisibility(View.VISIBLE);
            coverPicture.setImageResource(host.getImageId());


            final VideoLayout video = view.findViewById(R.id.hostVideo);

            Log.i("DEMO", "Init Host:"+host.getChannelId());

            HostVideoInfo hostInfo = findHostVideoInfo(position);

            if(hostInfo == null) hostInfo = new HostVideoInfo();
            hostInfo.setHost(host);
            hostInfo.setHostVideo(video);
            hostInfo.setPosition(position);

            RtcConnection connection = new RtcConnection();
            connection.channelId = host.getChannelId();
            connection.localUid = 10002;

            hostInfo.setConnection(connection);

            HostVideoHandler channelEventHandler = new HostVideoHandler();
            channelEventHandler.setHostVideo(hostInfo);

            Log.i("DEMO", "Join Channel :"+host.getChannelId());
            AgoraManager.getInstance().joinChannelEx(connection, "", channelEventHandler);

            AgoraManager.getInstance().setUidViewEx(connection, host.getUserId(), video);

            hostInfo.setJoined(true);

            hostVideoList.add(hostInfo);

            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return AgoraManager.getInstance().getHosts().size();
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            /**
             * Perhaps, it's a good idea that
             * Here is the good place to call leave channel method
             */
            leaveHostChannel(position);

            View contentView = (View) object;
            container.removeView(contentView);
            //mViewCache.add(contentView);
            Log.i("DEMO", "Destroy item :"+position);

        }

        public HostVideoInfo findHostVideoInfo(int position) {
            if( position < 0 ) return null;
            for (int i = 0; i < hostVideoList.size(); i++) {
                HostVideoInfo hostInfo = hostVideoList.get(i);
                if (hostInfo.getPosition() == position) {
                    return hostInfo;
                }
            }
            return null;
        }

        protected void leaveHostChannel(int position) {

            HostVideoInfo hostInfo = findHostVideoInfo(position);
            if (hostInfo == null)
                return;

            AgoraManager.getInstance().leaveChannelEx(hostInfo.getConnection());
            Log.i("DEMO", "Leave channel :" + position);
            hostInfo.setJoined(false);

        }
    }

    private class HostVideoHandler extends IRtcEngineEventHandler {
        private final static String TAG = "DEMO";
        private HostVideoInfo hostVideoInfo;
        private int hostId;

        public void setHostVideo(HostVideoInfo hostVideoInfo) {
            this.hostVideoInfo = hostVideoInfo;
            hostId = hostVideoInfo.getHost().getUserId();
        }

        @Override
        public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
            Log.e(TAG, String.format("onFirstRemoteVideoFrame %d ", uid));

        }

        /**
         * Reports a warning during SDK runtime.
         * Warning code: https://docs.agora.io/en/Voice/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_i_rtc_engine_event_handler_1_1_warn_code.html
         */
        @Override
        public void onWarning(int warn) {
            Log.w(TAG, String.format("onWarning code %d message %s", warn, RtcEngine.getErrorDescription(warn)));
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
            if (hostId != uid) return;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    hostVideoInfo.getHostVideo().setVisibility(View.VISIBLE);
                }
            });
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
            if (hostId != uid) return;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    hostVideoInfo.getHostVideo().setVisibility(View.VISIBLE);
                }
            });
        }

    }


    @Override
    protected void onDestroy() {
        AgoraManager.getInstance().destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(AgoraManager.getInstance().isReady()) {
            HostVideoInfo hostVideoInfo = verticalPagerAdapter.findHostVideoInfo(mCurrentPosition);
            if(hostVideoInfo != null)AgoraManager.getInstance().unMuteAudioEx(hostVideoInfo.getConnection(), true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(AgoraManager.getInstance().isReady()) {
            HostVideoInfo hostVideoInfo = verticalPagerAdapter.findHostVideoInfo(mCurrentPosition);
            if(hostVideoInfo != null)AgoraManager.getInstance().unMuteAudioEx(hostVideoInfo.getConnection(), false);
        }
    }
}
