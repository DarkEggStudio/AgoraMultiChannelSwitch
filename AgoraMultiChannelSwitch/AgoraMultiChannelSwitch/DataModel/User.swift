//
//  User.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/21.
//

import Foundation

class User: Codable { 
    var id: UInt?
    var name: String?
    
    init(id: UInt, name: String) {
        self.id = id
        self.name = name
    }
}
