# AgoraMultiChannelSwitch
Agora Multi Channel Switch Sample

## Third-party library
* Using third-party library [VerticalViewPage](https://github.com/castorflex/VerticalViewPage)

## Ideas

### Initialization
Init a ViewPager implemented from VerticalViewPager

setOffscreenPageLimit 1

Init a VerticalPageAdapter implemented from PagerAdapter 

Override instantiateItem method
* Load fragment view
* Setup HostVideoInfo object to save Host info, Video(SurfaceView), Position and RTC connection
* Join Channel with autoSubscription value (false)

### PageScrolling

Sequence of a host view

1. instantiateItem
2. onPageSelected
3. transformPage
4. destroyItem

**onPageSelected**

- Mute Previous Host Channel
- Unsubscribe video and audio
- do not leave channel

**transformPage**

- This is called after the new page is fully displayed, and scrolling is done;
- subscribe host's video and audio
- unMute the current Host Channel

**destroyItem**

VerticalPageAdapter.destroyItem is called when view is offscreen and released;
So, leave Channel


### Swipe up and down
Because user may swipe up and down, there are always 3 view which are activated.
It's not a good idea to leave channel when the view is offscreen immediately.

## TODO:
Display Host surface view under Scrolling progress
Right now host surface view is displayed after view is fully displayed.

