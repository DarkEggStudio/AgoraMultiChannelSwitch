//
//  APICaller.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/12/08.
//


import UIKit
import DarkEggKit
import PromiseKit

// MARK: - keys and default values
private let kContentTypeKey     = "Content-Type"
private let kContentType        = "application/json"
private let kUserAgentKey       = "User-Agent"
private let kAcceptLanguageKey  = "Accept-Language"
private let kAuthorizationKey   = "Authorization"
private let kAuthorization      = "Bearer"
private let kDevicePlatformKey  = "devicePlatform"
private let kDevicePlatform     = "iOS"
private let kDeviceIdKey        = "deviceId"

struct HttpResponseStatusCode {
    static let success: Int    = 200
    static let needsUpate: Int = 426
}

class ApiCaller: NSObject {
    // MARK: - Singleton
    static let shared: ApiCaller = {ApiCaller()}();
    
    internal var authToken: String?
    
    private var kUserAgent: String = {
        let osVersion = UIDevice.current.systemVersion
        let deviceName = UIDevice.modelName
        let appVersionBuild = Bundle.main.infoDictionary?[kCFBundleVersionKey as String] ?? ""
        let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] ?? ""
        let userAgentStr = "Device:\(deviceName),OS:iOS,OSVersion:\(osVersion),App:\(appVersion)"
        return userAgentStr
    }()
    
    var requestQueue: [String: DataRequest] = [:]
    
    lazy var SystemLanguage: String = {
        let preferredLang = Bundle.main.preferredLocalizations.first ?? "en"
        Logger.debug("current system language: \(preferredLang)")
        switch preferredLang {
        case "en-US", "en":
            return preferredLang
        case "ja-JP", "ja":
            return preferredLang
        default:
            return "en"
        }
    }()
}

extension ApiCaller {
    internal func callApi<API: CommonApiProtocol, T: Codable>(api: API, apiUrl: String = AppConfig.shared.bidConfig.baseUrl) -> Promise<T> {
        
        var header = [kContentTypeKey: kContentType, kUserAgentKey: kUserAgent, kAcceptLanguageKey: SystemLanguage]
        
        if let token = self.authToken {
            header[kAuthorizationKey] = "\(kAuthorization) \(token)"
        }

        
        /*
        if let deviceId = DeviceManager.shared.deviceId {
            header[kDeviceIdKey] = deviceId
        }
         */
        
        let q = DispatchQueue.global()
        var param = api.parameter
        if param != nil {
            param![kDevicePlatformKey] = kDevicePlatform
        }
        else {
            param = [kAuthorizationKey: kDevicePlatform]
        }
        
        var encoding: ParameterEncoding = JSONEncoding.default
        if api.method == .get {
            //encoding = URLEncoding.default
            encoding = URLEncoding(boolEncoding: .literal)
        }
        if api.method == .delete {
            param = nil
        }
        let url = "\(apiUrl)/\(api.endpoint)"
        let request = Alamofire.request(url, method: api.method, parameters: param, encoding: encoding, headers: header)
        if let cancelToken = api.cancelToken {
            self.requestQueue[cancelToken] = request
        }
        
        Logger.debug("request url: \(url)")
        
        //return request.responseDecodable(T.self, queue: q)
        return Promise { seal in
            request.validate().response(queue: q) { (response) in
                // request end, remove from cancel queue
                if let cancelToken = api.cancelToken {
                    self.requestQueue[cancelToken] = nil
                    self.requestQueue.removeValue(forKey: cancelToken)
                }
                
                let decoder = JSONDecoder()
                Logger.error("response.response?.statusCode: \(response.response?.statusCode ?? 999)")
                Logger.error("response.response?.: \(response.response?.debugDescription ?? "--")")
                
//                guard response.response?.statusCode == 200 || response.response?.statusCode == 204 else {
                guard (response.response?.statusCode ?? 999) <= 226 else {
                    if response.response?.statusCode == 426 {
                        // NotificationCenter.default.post(name: NSNotification.Name.Api.ForceUpdate, object: nil)
                        seal.reject(APIError.forceUpdate)
                        return
                    }
                    else if response.response?.statusCode == 404 {
                        // 404
                        seal.reject(APIError.noData)
                        return
                    }
                    else if response.response?.statusCode == 409 {
                        seal.reject(AuthError.conflictError)
                        return
                    }
                    if let a = response.data {
                        if let e = try? decoder.decode(ApiErrorModel.self, from: a) {
                            seal.reject(APIError.serverError(e))
                            return
                        }
                        if let t = try? decoder.decode(T.self, from: a) {
                            if let e = t as? ApiErrorModel {
                                seal.reject(APIError.serverError(e))
                            }
                            return
                        }
                    }
                    seal.reject(APIError.unknown)
                    return
                }
                // special for VoidObject
                if T.self == VoidModel.self {
                    let tempData = "{}".data(using: .utf8)!
                    if let voidObj = try? decoder.decode(T.self, from: tempData) {
                        seal.fulfill(voidObj)
                    }
                    else {
                        seal.reject(APIError.noData)
                    }
                    return
                }
                // else
                if let data = response.data {
                    if let t = try? decoder.decode(T.self, from: data) {
                        seal.fulfill(t)
                    }
                    else {
                        seal.reject(APIError.noData)
                    }
                }
            }
        }
    }
    
    internal func cancelApi(token cancelToken: String?) {
        guard let key = cancelToken else {
            return
        }
        if let request = self.requestQueue[key] {
            request.cancel()
            self.requestQueue[key] = nil
            self.requestQueue.removeValue(forKey: key)
        }
    }
}

