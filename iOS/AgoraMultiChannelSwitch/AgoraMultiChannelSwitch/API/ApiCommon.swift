//
//  ApiCommon.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/12/08.
//

import UIKit
import PromiseKit

enum HttpCode: Int {
    case notFound = 404
    case needUpdate = 426
    case serverError = 500
}

// MARK: - Common Api Protocol
protocol CommonApiProtocol {
    // MARK: - required
    associatedtype ResponseObject: Codable
    var endpoint: String { get }
    var method: HTTPMethod { get }
    // MARK: - option
    var parameter: [String: Any]? { get set }
    var cancelToken: String? { get set }
    
    // for dummy data
    var dummyFile: String { get }
    
    init()
    func request(serverUrl: String) -> Promise<ResponseObject>
}

extension CommonApiProtocol {
    init(_ parameter: [String: Any]?) {
        self.init()
        self.parameter = parameter
    }
    
    var parameter: [String: Any]? {
        get { return [:] }
        set {}
    }
    
    mutating func setCancelToken(_ cancelToken: String) {
        self.cancelToken = cancelToken
    }
    
    var dummyFile: String {
        get { return "" }
    }
    
    func request(serverUrl: String = AppConfig.shared.bidConfig.baseUrl) -> Promise<ResponseObject> {
        return ApiCaller.shared.callApi(api: self, apiUrl: serverUrl)
    }
    
    func dummyData() -> Promise<ResponseObject> {
        return Promise { seal in
            do {
                // check file path
                guard let jsonPath = Bundle.main.path(forResource: self.dummyFile, ofType: nil) else {
                    throw APIError.noData
                }
                // load dummy data
                let decoder = JSONDecoder()
                let data = try Data(contentsOf: URL(fileURLWithPath: jsonPath))
                let res = try decoder.decode(ResponseObject.self, from: data)
                seal.fulfill(res)
            } catch {
                seal.reject(error)
            }
        }
    }
}

// MARK: - Common Request Model
protocol CommonRequestModel: Codable {
    var dictionary: [String: Any]? { get }
}

extension CommonRequestModel {
    var dictionary: [String: Any]? {
      guard let data = try? JSONEncoder().encode(self) else { return nil }
      return (try? JSONSerialization.jsonObject(with: data, options: .allowFragments)).flatMap { $0 as? [String: Any] }
    }
}

// MARK: - Common Response Model
protocol CommonResponseModel: Codable {
    var errors: [ApiErrorModel]? { get set }
    var errorHandler: ((Int, [ApiErrorModel])->Any)? { get set }
}

extension CommonResponseModel {
    var errors: [ApiErrorModel]? {
        get { return [] }
        set {}
    }
    
    var errorHandler: ((Int, [ApiErrorModel])->Any)? {
        get { return nil }
        set {}
    }
}

// MARK: - Api Error Model
enum AuthError: Error {
    case noError
    case noSavedToken
    case settingError
    case idServerError
    case conflictError
    case unknown
}

// MARK: -
enum APIError: Error {
    case serverError(ApiErrorModel)
    case noData
    case forceUpdate
    case unknown
}


class ApiErrorModel: Codable {
    var errorCode: Int?
    var message: String?
    var errors: [ApiErrorModel]?
}

//protocol ApiErrorModel: Codable {
//    var errorCode: Int? { get set }
//    var message: String? { get set }
//    var errors: [ApiErrorModel]? { get set }
//}
//
//extension ApiErrorModel {
//    var errorCode: Int? {
//        get { return 999 }
//        set { }
//    }
//
//    var message: String? {
//        get { return "Unknown error." }
//        set {}
//    }
//
//    var errors: [ApiErrorModel]? {
//        get { return nil }
//        set {}
//    }
//}

class VoidModel: ApiErrorModel {
}
