package com.hbw.shortvideo;

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
import com.hbw.shortvideo.common.AgoraManager;
import com.hbw.shortvideo.common.HostInfo;
import com.hbw.shortvideo.common.HostVideoInfo;
import com.hbw.shortvideo.common.VideoReportLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import fr.castorflex.android.verticalviewpager.VerticalViewPager;
import io.agora.rtc2.RtcConnection;
import io.agora.rtc2.RtcEngineConfig;

public class MainActivity extends AppCompatActivity {

    private VerticalViewPager vp;
    private MyPagerAdapter myPagerAdapter;
    private AgoraPageAdapter agoraPagerAdapter;
    private int currentFlagPostion;//传递过来播放第几个
    private List<Video> list = new ArrayList<>();//播放列表
    private int mCurrentPosition;//当前正在播放第几个主播
    private int mPrevousPosition = -1;//上一个播放的主播
    //private AliPlayer aliPlayer;//当前正在播放的播放器

    private List<HostInfo> hosts = new ArrayList<>();

    private void initData()
    {
        String[] channels = new String[] {"AoVf4Hfdy9EbyDLh", "RxhMtitAzUwk1nBR", "IBbgSrJic1gNbGaG"};
        int[] images = new int[] {R.drawable.image1, R.drawable.image2, R.drawable.image3};

        for(int i=0;i<12;i++)
        {
            hosts.add(new HostInfo(channels[i%3], 900001, images[i%3]));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();

        setContentView(R.layout.activity_main);
        vp = findViewById(R.id.main_vp);
        if(!AgoraManager.getInstance().isReady()) {

            int areaCode = RtcEngineConfig.AreaCode.AREA_CODE_CN;

            AgoraManager.getInstance().createEngine(getApplicationContext(), getString(R.string.agora_app_id), areaCode);

            Log.i("DEMO", "init RTC engine");

        }
        initViewPager();
    }

    private void initViewPager() {
        currentFlagPostion = getIntent().getIntExtra("currentPostion", 0);

        agoraPagerAdapter = new AgoraPageAdapter();
        //myPagerAdapter = new MyPagerAdapter();
        //vp.setAdapter(myPagerAdapter);
        vp.setAdapter(agoraPagerAdapter);
        vp.setOffscreenPageLimit(1);
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                Log.i("PageScrolling", "onPageScrolled i:"+i+" v:"+v+" i1:"+i1);


            }

            @Override
            public void onPageSelected(int position) {

                mPrevousPosition = mCurrentPosition;

                HostVideoInfo hostVideoInfo = agoraPagerAdapter.findHostVideoInfo(mPrevousPosition);
                if(hostVideoInfo != null) {
                    Log.i("DEMO", "mute Previous Host Audio:"+mPrevousPosition);
                    AgoraManager.getInstance().unMuteAudioEx(hostVideoInfo.getConnection(), true);
                    Log.i("DEMO", "unSubscribe Channel:"+mPrevousPosition);
                    AgoraManager.getInstance().updateChannelEx(hostVideoInfo.getConnection(), false);
                    Log.i("DEMO", "Leave channel:"+mPrevousPosition);

                    AgoraManager.getInstance().leaveChannelEx(hostVideoInfo.getConnection());
                }

                mCurrentPosition = position;
                Log.i("DEMO", "onPageSelected position:"+position);

                hostVideoInfo = agoraPagerAdapter.findHostVideoInfo(mCurrentPosition);
                if(hostVideoInfo != null) {
                    Log.i("DEMO", "HostVideoInfo position:" + hostVideoInfo.getPosition());
                    Log.i("DEMO", "unMuteAudio:"+position);
//                    AgoraManager.getInstance().unMuteAudioEx(hostVideoInfo.getConnection(), false);
//                    AgoraManager.getInstance().setUidViewEx(hostVideoInfo.getConnection()
//                            , hostVideoInfo.getHost().getUserId(), hostVideoInfo.getHostVideo());

                }

            }

            @Override
            public void onPageScrollStateChanged(int i) {
                Log.i("PageScrolling", "onPageScrollStateChanged i:"+i);
            }
        });
        vp.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                if (position != 0) {
                    return;
                }
                Log.i("DEMO", "transformPage position:"+mCurrentPosition);

//

                HostVideoInfo hostVideoInfo = agoraPagerAdapter.findHostVideoInfo(mCurrentPosition);
                if(hostVideoInfo != null) {
                    AgoraManager.getInstance().updateChannelEx(hostVideoInfo.getConnection(), true);
                    AgoraManager.getInstance().unMuteAudioEx(hostVideoInfo.getConnection(), false);
                    AgoraManager.getInstance().setUidViewEx(hostVideoInfo.getConnection()
                            , hostVideoInfo.getHost().getUserId(), hostVideoInfo.getHostVideo());

                }
//
//                PlayerInfo playerInfo = myPagerAdapter.findPlayerInfo(mCurrentPosition);
//                if (playerInfo != null) {
//                    if (playerInfo.getAliPlayer() != null) {
//                        playerInfo.getAliPlayer().start();
//                        aliPlayer = playerInfo.getAliPlayer();
//                    }
//                }
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

//            if (!TextUtils.isEmpty(host.getImageUrl())) {
//                coverPicture.setVisibility(View.VISIBLE);
//                Glide.with(MainActivity.this).load(host.getImageUrl()).into(coverPicture);
//            }
            coverPicture.setVisibility(View.VISIBLE);
            coverPicture.setImageResource(host.getImageUrl());

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
            //destroyHostInfo(position);
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

        protected void destroyHostInfo(int position) {
            //while (true) {
                HostVideoInfo hostInfo = findHostVideoInfo(position);
                if (hostInfo == null)
                    return;

                AgoraManager.getInstance().leaveChannelEx(hostInfo.getConnection());
            Log.i("DEMO", "Leave channel :"+position);
                hostInfo.setJoined(false);
            //}
        }

    }


    class MyPagerAdapter extends PagerAdapter {

        ArrayList<PlayerInfo> playerInfoList = new ArrayList<>();
        private LinkedList<View> mViewCache = new LinkedList<>();

        protected PlayerInfo instantiatePlayerInfo(int position) {
            AliPlayer aliyunVodPlayer = AliPlayerFactory.createAliPlayer(getApplicationContext());
            PlayerInfo playerInfo = new PlayerInfo();
            playerInfo.setPlayURL(list.get(position).getVideoUrl());
            playerInfo.setAliPlayer(aliyunVodPlayer);
            playerInfo.setPosition(position);
            playerInfoList.add(playerInfo);
            return playerInfo;
        }

        public PlayerInfo findPlayerInfo(int position) {
            for (int i = 0; i < playerInfoList.size(); i++) {
                PlayerInfo playerInfo = playerInfoList.get(i);
                if (playerInfo.getPosition() == position) {
                    return playerInfo;
                }
            }
            return null;
        }

        public void mOnDestroy() {
            for (PlayerInfo playerInfo : playerInfoList) {
                if (playerInfo.getAliPlayer() != null) {
                    playerInfo.getAliPlayer().release();
                }
            }
            playerInfoList.clear();
        }

        protected void destroyPlayerInfo(int position) {
            while (true) {
                PlayerInfo playerInfo = findPlayerInfo(position);
                if (playerInfo == null)
                    break;
                if (playerInfo.getAliPlayer() == null)
                    break;
                playerInfo.getAliPlayer().release();
                playerInfoList.remove(playerInfo);
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            View view;
            if (mViewCache.size() == 0) {
                view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_main_viewpage, null, false);
            } else {
                view = mViewCache.removeFirst();
            }
            view.setId(position);

            TextView videoTitle = view.findViewById(R.id.item_main_video_title);
            final ImageView coverPicture = view.findViewById(R.id.item_main_cover_picture);
            SurfaceView surfaceView = view.findViewById(R.id.item_main_surface_view);

            surfaceView.setZOrderOnTop(true);

            if (!TextUtils.isEmpty(list.get(position).getImageUrl())) {
                coverPicture.setVisibility(View.VISIBLE);
                Glide.with(MainActivity.this).load(list.get(position).getImageUrl()).into(coverPicture);
            }
            videoTitle.setText("<  " + list.get(position).getTitle());
            videoTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnDestroy();
                    finish();
                }
            });

            final PlayerInfo playerInfo = instantiatePlayerInfo(position);
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    playerInfo.getAliPlayer().setDisplay(holder);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    playerInfo.getAliPlayer().redraw();
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    playerInfo.getAliPlayer().setDisplay(null);
                }
            });
            UrlSource urlSource = new UrlSource();
            urlSource.setUri(list.get(position).getVideoUrl());
            //设置播放源
            playerInfo.getAliPlayer().setDataSource(urlSource);
            //准备播放
            playerInfo.getAliPlayer().prepare();

            //开启缓存
            CacheConfig cacheConfig = new CacheConfig();
            //开启缓存功能
            cacheConfig.mEnable = true;
            //能够缓存的单个文件最大时长。超过此长度则不缓存
            cacheConfig.mMaxDurationS = 300;
            //缓存目录的位置
            cacheConfig.mDir = "hbw";
            //缓存目录的最大大小。超过此大小，将会删除最旧的缓存文件
            cacheConfig.mMaxSizeMB = 200;
            //设置缓存配置给到播放器
            playerInfo.getAliPlayer().setCacheConfig(cacheConfig);

            playerInfo.getAliPlayer().setLoop(true);
            playerInfo.getAliPlayer().setOnPreparedListener(new IPlayer.OnPreparedListener() {
                @Override
                public void onPrepared() {
                    // 视频准备成功之后影响封面图
                    if (!TextUtils.isEmpty(list.get(position).getImageUrl())) {
                        coverPicture.setVisibility(LinearLayout.GONE);
                    }
                }
            });
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            destroyPlayerInfo(position);
            View contentView = (View) object;
            container.removeView(contentView);
            mViewCache.add(contentView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (aliPlayer != null) {
//            aliPlayer.release();
//        }
        if (myPagerAdapter != null) {
            myPagerAdapter.mOnDestroy();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (aliPlayer != null) {
//            aliPlayer.pause();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (aliPlayer != null) {
//            aliPlayer.start();
//        }
    }
}
