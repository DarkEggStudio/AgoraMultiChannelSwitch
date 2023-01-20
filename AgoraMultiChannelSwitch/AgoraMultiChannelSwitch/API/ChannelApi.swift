//
//  ChannelApi.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/12/08.
//

import Alamofire

struct ChannelApi: CommonApiProtocol {
    typealias ResponseObject = ChannelApiResponseModel
    
    var endpoint: String = "channels"
    var method: HTTPMethod = .get
    var cancelToken: String?
}

struct ChannelApiRequestModel: CommonRequestModel {
    
}

struct ChannelApiResponseModel: CommonResponseModel {
    var result: [ChannelModel] = []
}
