//
//  AuctionApi.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/12/08.
//

import Alamofire

// MARK: -
struct AuctionApi: CommonApiProtocol {
    typealias ResponseObject = AuctionApiResponseModel
    
    var method: HTTPMethod = .get
    var cancelToken: String?
    
    init() {
        fatalError("must set uid.")
    }
    
    let channelId: String
    init(channelId: String) {
        self.channelId = channelId
    }
    var endpoint: String {
        "auction?channelid=\(self.channelId)"
    }
}

// MARK: -
struct AuctionApiRequestModel: CommonRequestModel {
    var channelId: String?
}

// MARK: -
struct AuctionApiResponseModel: CommonResponseModel {
    var result: [AuctionModel] = []
}
