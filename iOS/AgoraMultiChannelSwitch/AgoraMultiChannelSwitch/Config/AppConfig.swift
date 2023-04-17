//
//  AppConfig.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/12/08.
//

import Foundation

struct BidConfig {
    var aopId: String = ""
    var baseUrl: String = ""
}

//
let defaultAgoraRtcAppId = <#Your Agora AppId#>
struct AgoraRtcConfig {
    var appId: String = ""
}

class AppConfig {
    static let shared: AppConfig = {AppConfig()}()
    
    private let defaultBidConfig: BidConfig = BidConfig(baseUrl: "")
    lazy var bidConfig: BidConfig = {
        return defaultBidConfig
    }()
    
    private let defaultAgoraRtcConfig: AgoraRtcConfig = AgoraRtcConfig(appId: defaultAgoraRtcAppId)
    lazy var rtc: AgoraRtcConfig = {
        return defaultAgoraRtcConfig
    }()
}
