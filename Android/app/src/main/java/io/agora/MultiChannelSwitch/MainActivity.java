package io.agora.MultiChannelSwitch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.player.IPlayer;
import com.aliyun.player.nativeclass.CacheConfig;
import com.aliyun.player.source.UrlSource;
import com.bumptech.glide.Glide;
import com.hbw.shortvideo.R;

import io.agora.MultiChannelSwitch.common.AgoraManager;
import io.agora.MultiChannelSwitch.common.HostInfo;
import io.agora.MultiChannelSwitch.common.HostVideoInfo;
import io.agora.MultiChannelSwitch.common.VideoReportLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;
import io.agora.rtc2.RtcConnection;
import io.agora.rtc2.RtcEngineConfig;

public class MainActivity extends AppCompatActivity {

    private VerticalViewPager vp;

    private AgoraPageAdapter agoraPagerAdapter;
    private int currentFlagPostion;//传递过来播放第几个

    private int mCurrentPosition;//当前正在播放第几个主播
    private int mPrevousPosition = -1;//上一个播放的主播

    private List<HostInfo> hosts = new ArrayList<>();

    private void initSampleHosts()
    {
        int hostCount = 3; // set number to 12 for loop

        String[] channels = new String[] {"AoVf4Hfdy9EbyDLh", "RxhMtitAzUwk1nBR", "IBbgSrJic1gNbGaG"};
        int[] images = new int[] {R.drawable.image1, R.drawable.image2, R.drawable.image3};

        for(int i=0;i<hostCount;i++)
        {
            hosts.add(new HostInfo(channels[i%3], 900001, images[i%3]));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        currentFlagPostion = getIntent().getIntExtra("currentPostion", 0);

        agoraPagerAdapter = new AgoraPageAdapter();

        vp.setAdapter(agoraPagerAdapter);
        vp.setOffscreenPageLimit(1);
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
//                Log.i("PageScrolling", "onPageScrolled i:"+i+" v:"+v+" i1:"+i1);
            }

            @Override
            public void onPageSelected(int position) {

                mPrevousPosition = mCurrentPosition;

                /**
                 * mute prevous channel
                 * unsubscribe any hosts
                 * leave channel
                 */
                HostVideoInfo hostVideoInfo = agoraPagerAdapter.findHostVideoInfo(mPrevousPosition);
                if(hostVideoInfo != null && hostVideoInfo.isJoined()) {
                    Log.i("DEMO", "mute Previous Host Audio:"+mPrevousPosition);
                    AgoraManager.getInstance().unMuteAudioEx(hostVideoInfo.getConnection(), true);
                    Log.i("DEMO", "unSubscribe Channel:"+mPrevousPosition);
                    AgoraManager.getInstance().updateChannelEx(hostVideoInfo.getConnection(), false);
                    Log.i("DEMO", "Leave channel:"+mPrevousPosition);

                    // Here, call agora leave channel function is not a good idea,
                    // because if user swipe back to previous one
                    AgoraManager.getInstance().leaveChannelEx(hostVideoInfo.getConnection());
                    hostVideoInfo.setJoined(false);
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
                    return;
                }
                Log.i("DEMO", "transformPage position:"+mCurrentPosition);

                /**
                 * Here is the place when NEW page is fully displayed.
                 * Scrolling page is finished.
                 * So, subscribe channel host and unMute audio
                 * Setup display UI
                 */

                HostVideoInfo hostVideoInfo = agoraPagerAdapter.findHostVideoInfo(mCurrentPosition);
                if(hostVideoInfo != null) {
                    AgoraManager.getInstance().updateChannelEx(hostVideoInfo.getConnection(), true);
                    AgoraManager.getInstance().unMuteAudioEx(hostVideoInfo.getConnection(), false);
                    AgoraManager.getInstance().setUidViewEx(hostVideoInfo.getConnection()
                            , hostVideoInfo.getHost().getUserId(), hostVideoInfo.getHostVideo());

                }

            }

        });
        vp.setCurrentItem(currentFlagPostion);
    }

    class AgoraPageAdapter extends PagerAdapter {
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

            HostInfo host = hosts.get(position % 3);

            Log.i("instantiateItem", "instantiateItem position:"+position);
            Log.i("DEMO", "Join Channel:"+position);

            TextView videoTitle = view.findViewById(R.id.hostTitle);
            final ImageView coverPicture = view.findViewById(R.id.hostImage);
            videoTitle.setText("<  " + host.getChannelName());

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


            VideoReportLayout video = view.findViewById(R.id.hostVideo);

            HostVideoInfo hostInfo = new HostVideoInfo();
            hostInfo.setHost(host);
            hostInfo.setHostVideo(video);
            hostInfo.setPosition(position);

            RtcConnection connection = new RtcConnection();
            connection.channelId = host.getChannelName();
            connection.localUid = 10002;

            hostInfo.setConnection(connection);

            AgoraManager.getInstance().joinChannelEx(connection, "");

            hostInfo.setJoined(true);

            hostVideoList.add(hostInfo);

            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return hosts.size();
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            /**
             * Perhaps, it's a good idea that
             * Here is the good place to call leave channel method
             */
            //leaveHostChannel(position);

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



    @Override
    protected void onDestroy() {
        AgoraManager.getInstance().destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
