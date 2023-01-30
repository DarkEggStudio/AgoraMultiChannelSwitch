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

class AppConfig {
    static let shared: AppConfig = {AppConfig()}()
    private let defaultBidConfig: BidConfig = BidConfig(baseUrl: "")
    lazy var bidConfig: BidConfig = {
        return defaultBidConfig
    }()
}
