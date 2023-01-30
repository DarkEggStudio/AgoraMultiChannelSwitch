//
//  BitApi.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/12/08.
//

import Alamofire

// MARK: - 
struct BidApi: CommonApiProtocol {
    typealias ResponseObject = BidApiResponseModel
    var method: HTTPMethod = .post
    var cancelToken: String?
    
    var endpoint: String = "auction/bid"
}

// MARK: -
struct BidApiRequestModel: CommonRequestModel {
    var auctionId: Int
    var uid: String
    var amount: Int = 0
}

// MARK: -
struct BidApiResponseModel: CommonResponseModel {
    var result: Bool = false
    var reason: String?
}
