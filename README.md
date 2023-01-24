# AgoraMultiChannelSwitch_iOS
Agora Multi Channel Switch

## Some comment 2023.01.24

* Switch multi channel use scroll view
* Use your agora app id in the ```AgoraManager.swift```
* The channel names are hard coded, must be HuTest01/HuTest02/HuTest03 to fix the cover images

    * You can change these part use channel list that get from RESTful API
    
* There are 2 type

    * **Enable Preload Video** will join 3 channel at same time and subscribe 3 video, but only the audio the center channel will be subscribed.
    * **Use Cover Image** will join only 1 channel if not scrolling. When scrolling, it will join next channel for preload, so it will be at most 2 channel in same time.
