package io.agora.MultiChannelSwitch.libs;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.LinkedList;

import io.agora.MultiChannelSwitch.R;
import io.agora.MultiChannelSwitch.common.AgoraManager;
import io.agora.MultiChannelSwitch.common.HostInfo;
import io.agora.MultiChannelSwitch.common.HostVideoInfo;
import io.agora.MultiChannelSwitch.common.VideoLayout;
import io.agora.rtc2.RtcConnection;

public class VerticalPageAdapter extends PagerAdapter {
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

        HostInfo host = AgoraManager.getInstance().getHosts().get(position % 3);

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


        VideoLayout video = view.findViewById(R.id.hostVideo);

        Log.i("DEMO", "Init Host:"+host.getChannelId());

        HostVideoInfo hostInfo = new HostVideoInfo();
        hostInfo.setHost(host);
        hostInfo.setHostVideo(video);
        hostInfo.setPosition(position);

        RtcConnection connection = new RtcConnection();
        connection.channelId = host.getChannelId();
        connection.localUid = 10002;

        hostInfo.setConnection(connection);

        Log.i("DEMO", "Join Channel :"+host.getChannelId());
        AgoraManager.getInstance().joinChannelEx(connection, "");

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
