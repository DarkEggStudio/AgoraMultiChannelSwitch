//
//  SettingManager.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/25.
//

import Foundation
import DarkEggKit

class SettingManager: NSObject {
    static var shared: SettingManager = {SettingManager()}()
    var settingTableData = ["Type": [Setting(title: "AudienceType", key: "kAudienceType")],
                            "Multi Channel Switch": [Setting(title: "Preload", key: "kPreload")]]
    
    var enablePreload: Bool = true
}

extension SettingManager {
    func changeSetting(key: String, toValue value: Any? = nil) {
        guard value != nil else {
            // If value is null, change nothing
            return
        }
    }
    
    func getSetting(of key: String) {
        guard !key.isEmpty else {
            return
        }
        return
    }
}
