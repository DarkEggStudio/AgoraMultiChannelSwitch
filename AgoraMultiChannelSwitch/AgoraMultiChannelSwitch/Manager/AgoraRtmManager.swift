//
//  AgoraRtmManager.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/12/09.
//

import Foundation
import AgoraRtmKit

import DarkEggKit

class AgoraRtmManager: NSObject {
    let rtmAppId = ""
    
    var rtmKit: AgoraRtmKit!
    
    static let shared: AgoraRtmManager = { AgoraRtmManager() }()
    
    override init() {
        super.init()
        self.rtmKit = AgoraRtmKit(appId: self.rtmAppId, delegate: self)
    }
}

extension AgoraRtmManager {
    func login(uid: String = "", completion: ((Bool, String)->Void)? = nil) {
        // get tokoen
        let api = RtmTokenApi(uid: uid)
        api.request(serverUrl: "").done { res in
            Logger.debug(res.result ?? "")
            
            guard let token = res.result else {
                Logger.error("Can not get token.")
                completion?(false, "Can not get token.")
                return
            }
            
            self.rtmKit.login(byToken: token, user: uid) { code in
                Logger.debug(code.rawValue)
                completion?(true, "\(code)")
            }
        }.catch { err in
            Logger.error(err.localizedDescription)
            completion?(false, err.localizedDescription)
        }.finally {
            // nothing
        }
    }
    
    func logout() {
        self.rtmKit.logout { code in
            Logger.debug(code)
        }
    }
    
    func join(channelName: String, username: String) {
        self.rtmKit.createChannel(withId: channelName, delegate: self)
    }
    
    func leave() {
        //
    }
}

extension AgoraRtmManager: AgoraRtmDelegate {
    func rtmKit(_ kit: AgoraRtmKit, userMetadataUpdated userId: String, metadata data: AgoraRtmMetadata) {
        Logger.debug("\(userId) send data \(data)")
    }
}

extension AgoraRtmManager: AgoraRtmChannelDelegate {
    func channel(_ channel: AgoraRtmChannel, messageReceived message: AgoraRtmMessage, from member: AgoraRtmMember) {
        Logger.debug(message.text)
    }
}
