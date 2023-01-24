//
//  ChannelVideoView.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/20.
//

import UIKit
import AgoraRtcKit
import DarkEggKit

class ChannelVideoView: UIView {
    @IBOutlet private weak var messageLabel: UILabel!
    @IBOutlet private weak var channelLabel: UILabel!
    @IBOutlet private weak var elapsedLabel: UILabel!
    @IBOutlet private weak var videoView: UIView!
    @IBOutlet private weak var coverImageView: UIImageView!
    
    let agoraMgr = AgoraManager.shared
    
    var channelId: String?
    var coverImageUrl: String? {
        didSet {
            if let image = coverImageUrl {
                self.coverImageView.image = UIImage(named: image)
                self.coverImageView.isHidden = false
                self.coverImageView.alpha = 1.0
            }
            else {
                self.coverImageView.image = nil
            }
        }
    }
    
    var time: TimeInterval = 0
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }
}

// MARK: - Channel functions
extension ChannelVideoView {
    /// set cover image
    func setCoverImage(named image: String) {
        self.coverImageView.image = UIImage(named: image)
        self.coverImageView.isHidden = false
        self.coverImageView.alpha = 1.0
    }
    
    /// Join channel
    /// - Parameter channel: channel name
    func joinChannel(_ channel: String, enableAudio: Bool = false) {
        Logger.debug("Join channel \(channel) with audio \(enableAudio)")
        // set time
        self.elapsedLabel.text = "elapsed N/A ms"
        self.time = Date.now.timeIntervalSince1970
        Logger.debug(self.time)
        self.channelId = channel
        self.agoraMgr.joinEx(channel, uid: 10000, enableAudio: enableAudio, agoraDelegate: self) { [weak self] success, channel in
            self?.channelLabel.text = channel
            guard success else {
                self?.messageLabel.isHidden = false
                self?.messageLabel.text = "Join channel error."
                return
            }
            self?.messageLabel.text = ""
            self?.messageLabel.isHidden = true
        }
    }
    
    /// Leave channel
    func leaveChannel() {
        Logger.debug()
        if let cname = self.channelId {
            self.agoraMgr.leaveEx(channel: cname, localUid: 10000)
            self.channelId = nil
            if !SettingManager.shared.enablePreload {
                self.coverImageView.image = UIImage(named: cname)
                self.coverImageView.isHidden = false
                self.coverImageView.alpha = 1.0
            }
        }
    }
    
    func enableAudio(flag: Bool) {
        if let cname = self.channelId {
            self.agoraMgr.setAudioEnable(flag, ofChannel: cname, localUid: 10000) {
                //
            }
        }
    }
    
    private func fadeOutCoverImage() {
        DispatchQueue.main.async {
            UIView.animate(withDuration: 0.4, delay: 0.0) {
                self.coverImageView.alpha = 0.0
            }
        }
    }
}

// MARK: - AgoraRtcEngineDelegate
extension ChannelVideoView: AgoraRtcEngineDelegate {
    func rtcEngine(_ engine: AgoraRtcEngineKit, didJoinedOfUid uid: UInt, elapsed: Int) {
        Logger.debug("Host \(uid) joined.")
        AgoraManager.shared.setHostVideoViewEx(self.videoView, forChannel: self.channelId!, hostUid: uid, localUid: 10000)
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, firstRemoteVideoFrameOfUid uid: UInt, size: CGSize, elapsed: Int) {
//        self.coverImageView.isHidden = true
        // updata time
        let t = Date.now.timeIntervalSince1970 - self.time
        Logger.debug(t)
        self.elapsedLabel.text = "elapsed \(Int(t*1000)) ms"
        self.time = 0
        self.fadeOutCoverImage()
    }
}
