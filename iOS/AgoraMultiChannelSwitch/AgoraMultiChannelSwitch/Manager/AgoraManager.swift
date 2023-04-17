//
//  AgoraManager.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/08.
//

import Foundation
import AVKit
import AgoraRtcKit
import DarkEggKit

@objc protocol AgoraManagerDelegate: AnyObject {
    @objc optional func agoraManager(_ mgr: AgoraManager, userJoined uid: UInt)
    @objc optional func agoraManager(_ mgr: AgoraManager, userLeaved uid: UInt)
    @objc optional func agoraManager(_ mgr: AgoraManager, ReceivedString str: String?)
}

class AgoraManagerDelegateObject: AgoraManagerDelegate {
    var channelName: String
    
    init(channel: String) {
        self.channelName = channel
    }
}

class AgoraManager: NSObject {
    var agoraKit: AgoraRtcEngineKit!
    weak var delegate: AgoraManagerDelegate?
    
    static let shared: AgoraManager = { AgoraManager()}()
    
    private let dateFormatter = DateFormatter()
    
    var remoteHostJoined: ((UInt) -> Void) = { uid in
        //
    }
    
    override init() {
        super.init()
        let config = AgoraRtcEngineConfig()
        config.areaCode = .global
        config.appId = AppConfig.shared.rtc.appId
        agoraKit = AgoraRtcEngineKit.sharedEngine(with: config, delegate: self) //(withAppId: "", delegate: self)
        agoraKit.delegate = self
        agoraKit.setChannelProfile(.liveBroadcasting)
        agoraKit.setAudioProfile(.musicHighQualityStereo)
        agoraKit.setAudioScenario(.gameStreaming)
        agoraKit.enableAudio()
        agoraKit.enableVideo()
        agoraKit.enableLocalVideo(false)
        agoraKit.enableLocalAudio(true)
        agoraKit.muteLocalAudioStream(false)
        agoraKit.muteAllRemoteAudioStreams(false)
        
//        Logger.debug("1 \(Date.now.timeIntervalSince1970)")
//        let videoEncoderConfig = AgoraVideoEncoderConfiguration()
//        agoraKit.setVideoEncoderConfiguration(videoEncoderConfig)
//        Logger.debug("2 \(Date.now.timeIntervalSince1970)")
        
        dateFormatter.dateFormat = "yyyyMMdd hh:mm:ss.SSS"
    }
    
    func join(channel: String, type: LiveType, completion: ((Bool, String, UInt?) -> Void)? = nil) {
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.clientRoleType = .audience
        mediaOption.publishMediaPlayerAudioTrack = false
        mediaOption.publishCameraTrack = false
        mediaOption.publishMicrophoneTrack = false
        mediaOption.publishCustomAudioTrack = false
//        mediaOption.defaultVideoStreamType = AgoraVideoStreamType.high

        mediaOption.enableAudioRecordingOrPlayout = false
        mediaOption.autoSubscribeAudio = true
        mediaOption.autoSubscribeVideo = true
        mediaOption.channelProfile = .liveBroadcasting
        
        switch type {
        case .standard:
            mediaOption.audienceLatencyLevel = .lowLatency
            break
        case .premium:
            mediaOption.audienceLatencyLevel = .ultraLowLatency
            break
        }
        
        let connection = AgoraRtcConnection()
        connection.channelId = channel
        connection.localUid = 10000
        
        //self.agoraKit.setAudioFrameDelegate(self)
        
        self.agoraKit.setExternalAudioSource(true, sampleRate: 44100, channels: 1)
        let ret = self.agoraKit.joinChannel(byToken: nil, channelId: channel, info: nil, uid: 0) { channel, uid, elapsed in
            //
            Logger.debug("joinChannelEx: Join \(channel) with uid \(uid) elapsed \(elapsed)ms")
            Logger.debug("\(Date().timeIntervalSince1970)")
            guard uid > 0 else {
                // fail
                completion?(false, channel, nil)
                return
            }
            // success
            //let _ = self?.agoraKit.setEncodedVideoFrameDelegate(self)
            
            completion?(true, channel, uid)
        }
        Logger.debug(ret)
    }
    
    /// Join with connection
    /// - Parameters:
    ///   - channel: channel name
    ///   - uid: uid
    ///   - type: live type (standard/premium)
    ///   - enableAudio: autoSubscribeAudio
    ///   - enableVideo: autoSubscribeVideo
    ///   - agoraDelegate: AgoraRtcEngineDelegate
    ///   - completion: completion handle block
    func joinEx(_ channel: String, uid: UInt,
                type: LiveType = .standard,
                enableAudio: Bool = true,
                enableVideo: Bool = true,
                agoraDelegate: AgoraRtcEngineDelegate,
                completion: ((Bool, String)->Void)? = nil )  {
        //
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.clientRoleType = .audience
        mediaOption.publishMediaPlayerAudioTrack = false
        mediaOption.publishCameraTrack = false
        mediaOption.publishMicrophoneTrack = false
        mediaOption.publishCustomAudioTrack = false

        mediaOption.enableAudioRecordingOrPlayout = true
        mediaOption.autoSubscribeAudio = enableAudio
        mediaOption.autoSubscribeVideo = enableVideo
        mediaOption.channelProfile = .liveBroadcasting
        
        switch type {
        case .standard:
            mediaOption.audienceLatencyLevel = .lowLatency
            break
        case .premium:
            mediaOption.audienceLatencyLevel = .ultraLowLatency
            break
        }
        
        let connection = AgoraRtcConnection()
        connection.channelId = channel
        connection.localUid = 10000
        
        let ret = self.agoraKit.joinChannelEx(byToken: nil, connection: connection, delegate: agoraDelegate, mediaOptions: mediaOption) { channel, uid, elapsed in
            Logger.debug("joinChannelEx: Join \(channel) with uid \(uid) elapsed \(elapsed)ms")
            // Logger.debug("\(Date().timeIntervalSince1970)")
            guard uid > 0 else {
                // fail
                completion?(false, channel)
                return
            }
            // success
            //let a = self.agoraKit.setEncodedVideoFrameDelegate(self)
            completion?(true, channel)
            self.agoraKit.muteAllRemoteAudioStreams(false)
        }
        Logger.debug(ret)
    }
    
    func setAudioEnable(_ enable: Bool, ofChannel channel: String, localUid: UInt, completion: (()->Void)? = nil) {
        Logger.debug("\(channel) enable audio \(enable)")
        let mediaOption = AgoraRtcChannelMediaOptions()
        mediaOption.clientRoleType = .audience
        mediaOption.publishMediaPlayerAudioTrack = false
        mediaOption.publishCameraTrack = false
        mediaOption.publishMicrophoneTrack = false
        mediaOption.publishCustomAudioTrack = false

        mediaOption.enableAudioRecordingOrPlayout = true
        mediaOption.autoSubscribeAudio = false
        mediaOption.autoSubscribeVideo = true
        mediaOption.channelProfile = .liveBroadcasting
        
        mediaOption.autoSubscribeAudio = true
        mediaOption.enableAudioRecordingOrPlayout = true
        mediaOption.autoSubscribeAudio = true
        
        let connection = AgoraRtcConnection()
        connection.channelId = channel
        connection.localUid = localUid
        
        let a = agoraKit.updateChannelEx(with: mediaOption, connection: connection)
        agoraKit.muteAllRemoteAudioStreams(!enable)
        Logger.debug("\(channel) enable audio \(a)")
    }
    
    func leave(completion: (()->Void)? = nil) {
        self.agoraKit.leaveChannel { stats in
            completion?()
        }
    }
    
    func leaveEx(channel: String, localUid: UInt, completion: (()->Void)? = nil) {
        let connection = AgoraRtcConnection()
        connection.channelId = channel
        connection.localUid = localUid
        self.agoraKit.leaveChannelEx(connection) { stats in
            Logger.debug()
            completion?()
        }
    }
    
    func sendData() {
       // agoraKit.pushExternalEncodedVideoFrame(<#T##frame: Data##Data#>, info: <#T##AgoraEncodedVideoFrameInfo#>)
    }
}

extension AgoraManager {
    func setHostVideoView(_ view: UIView, forUser uid: UInt) {
        Logger.debug("Set host \(uid) video view.")
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = uid
        videoCanvas.view = view
        videoCanvas.renderMode = .hidden
//        self.remoteHostJoined(uid)
        self.agoraKit.setupRemoteVideo(videoCanvas)
    }
    
    func setHostVideoViewEx(_ view: UIView, forChannel channel: String, hostUid: UInt, localUid: UInt) {
        Logger.debug("Set host \(hostUid) video view.(Ex)")
        
        let videoCanvas = AgoraRtcVideoCanvas()
        videoCanvas.uid = hostUid
        videoCanvas.view = view
        videoCanvas.renderMode = .hidden
        
        let connection = AgoraRtcConnection()
        connection.channelId = channel
        connection.localUid = localUid
        self.agoraKit.setupRemoteVideoEx(videoCanvas, connection: connection)
        
//        let pipController = AgoraPictureInPictureController.init(displayView: view)
//        pipController?.pipController.delegate = self
    }
    
    func test() {
        Logger.debug()
        //AVPlayer
    }

    func pullAudioRawData() {
        var data: Data = Data()
        agoraKit.pullPlaybackAudioFrameRawData(&data, lengthInByte: 65535)
        Logger.debug("data: \(data)")
    }
}

extension AgoraManager: AgoraRtcEngineDelegate {
    func setHostPosition(_ uid: UInt, position: [NSNumber]) {
        Logger.debug("setHostPosition")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didMicrophoneEnabled enabled: Bool) {
        Logger.debug("didMicrophoneEnabled: \(enabled)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        Logger.debug("Host user \(uid) join")
        if uid > 1000 {
            Logger.debug("\(uid) is data host")
            let opt = AgoraVideoSubscriptionOptions()
            opt.encodedFrameOnly = true
        }
        else {
            Logger.debug("\(uid) is video host")
            let opt = AgoraVideoSubscriptionOptions()
            opt.encodedFrameOnly = false
            self.delegate?.agoraManager?(self, userJoined: uid)
        }
    }
    
    //
    func rtcEngine(_ engine: AgoraRtcEngineKit, firstRemoteVideoDecodedOfUid uid: UInt, size: CGSize, elapsed: Int) {
        Logger.debug("\(Date().timeIntervalSince1970)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, localVideoStateChangedOf state: AgoraVideoLocalState, error: AgoraLocalVideoStreamError, sourceType: AgoraVideoSourceType) {
        Logger.debug()
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOfflineOfUid uid: UInt, reason: AgoraUserOfflineReason) {
        Logger.debug("user \(uid) leave")
        self.delegate?.agoraManager?(self, userLeaved: uid)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinChannel channel: String, withUid uid: UInt, elapsed: Int) {
        Logger.debug("Join \(channel) with uid \(uid) elapsed \(elapsed)ms")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurError errorCode: AgoraErrorCode) {
        Logger.debug("ErrorCode \(errorCode.rawValue)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didOccurWarning warningCode: AgoraWarningCode) {
        Logger.debug("WarningCode \(warningCode.rawValue)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, didUserInfoUpdatedWithUserId uid: UInt, userInfo: AgoraUserInfo) {
        Logger.debug("UserId:\(uid), userInfo: \(userInfo)")
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, receiveStreamMessageFromUid uid: UInt, streamId: Int, data: Data) {
        Logger.debug(String(decoding: data, as: UTF8.self))
    }
}

extension AgoraManager: AgoraEncodedVideoFrameDelegate {
    func onEncodedVideoFrameReceived(_ videoData: Data, length: Int, info videoFrameInfo: AgoraEncodedVideoFrameInfo) -> Bool {
        Logger.debug("Received data: \(videoData), length: \(length)")
//        let str = String(data: videoData, encoding: .utf8)
        
        Logger.debug("1: \(dateFormatter.string(from: Date()))")
        if let dict = try? JSONDecoder().decode([String: String].self, from: videoData) {
            let dateStr = dict["DateTime"]
//            Logger.debug(dateStr)
            self.delegate?.agoraManager?(self, ReceivedString: dateStr)
            Logger.debug("2: \(dateFormatter.string(from: Date()))")
        }
        return false
    }
}

extension AgoraManager: AgoraAudioFrameDelegate {
    func onRecordAudioFrame(_ frame: AgoraAudioFrame, channelId: String) -> Bool {
        Logger.debug()
        return false
    }
    
    func onPlaybackAudioFrame(_ frame: AgoraAudioFrame, channelId: String) -> Bool {
        Logger.debug()
        return false
    }
    
    func onMixedAudioFrame(_ frame: AgoraAudioFrame, channelId: String) -> Bool {
        Logger.debug()
        return false
    }
    
    func onEarMonitoringAudioFrame(_ frame: AgoraAudioFrame) -> Bool {
        Logger.debug()
        return false
    }
    
    func onPlaybackAudioFrame(beforeMixing frame: AgoraAudioFrame, channelId: String, uid: UInt) -> Bool {
        Logger.debug()
        return false
    }
    
    func getObservedAudioFramePosition() -> AgoraAudioFramePosition {
        return AgoraAudioFramePosition()
    }
    
    func getMixedAudioParams() -> AgoraAudioParams {
        return AgoraAudioParams()
    }
    
    func getRecordAudioParams() -> AgoraAudioParams {
        return AgoraAudioParams()
    }
    
    func getPlaybackAudioParams() -> AgoraAudioParams {
        return AgoraAudioParams()
    }
    
    func getEarMonitoringAudioParams() -> AgoraAudioParams {
        return AgoraAudioParams()
    }
}
