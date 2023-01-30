//
//  LiveRoom.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/18.
//

import Foundation

enum LiveRoomType: Int, Codable {
    case rtc
    case rtmp
}

struct LiveRoom: Codable {
    var name: String?
    var type: LiveRoomType = .rtc
}
