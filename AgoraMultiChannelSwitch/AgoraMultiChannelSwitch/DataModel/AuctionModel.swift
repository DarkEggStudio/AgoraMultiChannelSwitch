//
//  AuctionModel.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/12/08.
//

import Foundation

struct AuctionModel: Codable {
    var id: Int?            // The primary key of auction table
    var name: String?       // Auction name
    var cover: String?      // Image url, along with baseUrl
    var channelid: String?  // channel name(id) in RTM
    var status: Int?        // Auction status, 1: Online, 2: Offline, 0:Pending
    var owner: String?      // The current owner of the Auction
    
    var last_bid_at: Date?  // Last valid bid datetime
    var last_bid: Int?      // Last valid bid primry key
    var start_at: Date?     // Auction start time
    var end_at: Date?       // Auction end time
}
