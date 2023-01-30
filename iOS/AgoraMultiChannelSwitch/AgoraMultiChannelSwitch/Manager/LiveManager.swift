//
//  LiveManager.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/18.
//

import PromiseKit

enum IndexStepping: Int {
    case back = -1
    case forward = 1
}

class LiveManager: NSObject {
    var currentIndex: Int = 0
    private var liveList: [LiveRoom] = []
    static var shared: LiveManager = {LiveManager()}()
}

extension LiveManager {
    func getLiveList(pageIndex: Int = 0, pageSize: Int = 10) -> Promise<[LiveRoom]> {
        return Promise { seal in
            let list = self.makeDummyRoomList()
            seal.fulfill(list)
        }
    }
}

extension LiveManager {
    private func makeDummyRoomList() -> [LiveRoom] {
        let room1 = LiveRoom(name: "HuTest01", type: .rtc) // you can change to you own channel id here,
        let room2 = LiveRoom(name: "HuTest02", type: .rtc)
        let room3 = LiveRoom(name: "HuTest03", type: .rtc)
        let list: [LiveRoom] = [room1, room2, room3]
        self.liveList = list
        return list
    }
    
    func updateIndex(_ direction: IndexStepping) {
        currentIndex = self.currentIndex + direction.rawValue
        if currentIndex >= self.liveList.count {
            currentIndex = 0
        }
        else if currentIndex < 0 {
            currentIndex = self.liveList.count - 1
        }
    }
    
    /// next item
    var next: LiveRoom {
        var idx = self.currentIndex + 1
        if idx >= self.liveList.count {
            idx = 0
        }
        return self.liveList[idx]
    }
    
    /// prev item
    var prev: LiveRoom {
        var idx = self.currentIndex - 1
        if idx < 0 {
            idx = self.liveList.count - 1
        }
        return self.liveList[idx]
    }
}
