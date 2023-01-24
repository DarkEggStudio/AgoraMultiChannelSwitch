//
//  UserManager.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/21.
//

import Foundation

class UserManager: NSObject {
    static var shared: UserManager = { UserManager() }()
    var me: User?
    
    func login() {
        self.me = User(id: 10000, name: "DummyUser")
    }
}
