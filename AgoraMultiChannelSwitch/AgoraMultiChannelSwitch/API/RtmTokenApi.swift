//
//  RtmTokenApi.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/12/08.
//

import Alamofire

struct RtmTokenApi: CommonApiProtocol {
    typealias ResponseObject = RtmTokenResponseModel
    
    var method: HTTPMethod = .get
    var cancelToken: String? = nil
    
    init() {
        fatalError("must set uid.")
    }
    
    let uid: String
    init(uid: String) {
        self.uid = uid
    }
    
    var endpoint: String {
        "rtmtoken?uid=\(self.uid)"
    }
}

// MARK: - Request model
struct RtmTokenRequestModel: CommonRequestModel {
    var uid: String
}

// MARK: - Response model
struct RtmTokenResponseModel: CommonResponseModel {
    var result: String?
}
