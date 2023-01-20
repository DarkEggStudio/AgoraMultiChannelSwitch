//
//  Settiing.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/25.
//

import Foundation

class Setting: Codable {
    var title: String
    var key: String
    //var value: Codable?
    
    init(title: String, key: String) {
        self.title = title
        self.key = key
    }
}
