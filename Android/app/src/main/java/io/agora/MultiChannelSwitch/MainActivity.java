package io.agora.MultiChannelSwitch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import io.agora.MultiChannelSwitch.common.AgoraManager;
import io.agora.MultiChannelSwitch.common.HostInfo;
import io.agora.MultiChannelSwitch.common.HostVideoInfo;

import java.util.ArrayList;
import java.util.List;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;

import io.agora.MultiChannelSwitch.libs.VerticalPageAdapter;
import io.agora.rtc2.RtcEngineConfig;

public class MainActivity extends AppCompatActivity {

    private VerticalViewPager vp;

    private VerticalPageAdapter verticalPagerAdapter;
    private int currentFlagPosition;//传递过来播放第几个

    private int mCurrentPosition;//当前正在播放第几个主播
    private int mPreviousPosition = -1;//上一个播放的主播

    private void initSampleHosts()
    {
        List<HostInfo> hosts = new ArrayList<>();
        int hostCount = 3; // set number to 12 for loop

        String[] channels = new String[] {"AoVf4Hfdy9EbyDLh", "RxhMtitAzUwk1nBR", "IBbgSrJic1gNbGaG"};
        int[] images = new int[] {R.drawable.image1, R.drawable.image2, R.drawable.image3};

        for(int i=0;i<hostCount;i++)
        {
            hosts.add(new HostInfo(channels[i%3], 900001, images[i%3]));
        }

        AgoraManager.getInstance().setHosts(hosts);
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
        currentFlagPosition = getIntent().getIntExtra("currentPosition", 0);

        verticalPagerAdapter = new VerticalPageAdapter();

        vp.setAdapter(verticalPagerAdapter);
        vp.setOffscreenPageLimit(1);
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
//                Log.i("PageScrolling", "onPageScrolled i:"+i+" v:"+v+" i1:"+i1);
                // TODO: Join Channel based on Scrolling position


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
                    Log.i("DEMO", "mute Previous Host Audio:"+mPreviousPosition);
                    AgoraManager.getInstance().unMuteAudioEx(hostVideoInfo.getConnection(), true);
                    Log.i("DEMO", "unSubscribe Channel:"+mPreviousPosition);
                    AgoraManager.getInstance().updateChannelEx(hostVideoInfo.getConnection(), false);


                    // Here, call agora leave channel function is not a good idea,
                    // because if user swipe back to previous one
                    // AgoraManager.getInstance().leaveChannelEx(hostVideoInfo.getConnection());
                    // hostVideoInfo.setJoined(false);
                    // Log.i("DEMO", "Leave channel:"+mPreviousPosition);
                }

                mCurrentPosition = position;
                //Log.i("DEMO", "onPageSelected position:"+position);

            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //Log.i("PageScrolling", "onPageScrollStateChanged i:"+i);
            }
        });
        vp.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
//                if (position < -1) {
//                    HostVideoInfo nextHostInfo = agoraPagerAdapter.findHostVideoInfo(mCurrentPosition + 1);
//
//                    if(nextHostInfo != null && !nextHostInfo.isJoined()) {
//                        Log.i("DEMO", "Next Host state:"+nextHostInfo.isJoined());
//                        AgoraManager.getInstance().joinChannelEx(nextHostInfo.getConnection(), "");
//                        nextHostInfo.setJoined(true);
//                    }
//                }
//
//                if (position > 1 ) {
//                    HostVideoInfo prevHostInfo = agoraPagerAdapter.findHostVideoInfo(mCurrentPosition - 1);
//
//                    if(prevHostInfo != null && !prevHostInfo.isJoined()) {
//                        Log.i("DEMO", "Prev Host state:"+prevHostInfo.isJoined());
//                        AgoraManager.getInstance().joinChannelEx(prevHostInfo.getConnection(), "");
//                        prevHostInfo.setJoined(true);
//                    }
//                }
                if (position != 0) {
//                    Log.i("POSITION", "transformPage position:"+position);
                    return;
                }
//                Log.i("DEMO", "transformPage position:"+mCurrentPosition);

                /**
                 * Here is the place when NEW page is fully displayed.
                 * Scrolling page is finished.
                 * So, subscribe channel host and unMute audio
                 * Setup display UI
                 */
                HostVideoInfo hostVideoInfo = verticalPagerAdapter.findHostVideoInfo(mCurrentPosition);
                if(hostVideoInfo != null) {

                    Log.i("DEMO", "subscribe Channel:"+mCurrentPosition + " " +hostVideoInfo.getHost().getChannelId());
                    AgoraManager.getInstance().updateChannelEx(hostVideoInfo.getConnection(), true);
                    Log.i("DEMO", "unmute Host Audio:"+mCurrentPosition);
                    AgoraManager.getInstance().unMuteAudioEx(hostVideoInfo.getConnection(), false);



                }

            }

        });
        vp.setCurrentItem(currentFlagPosition);
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
