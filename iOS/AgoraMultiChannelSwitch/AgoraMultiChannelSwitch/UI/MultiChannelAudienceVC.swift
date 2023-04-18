//
//  MultiChannelAudienceVC.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/11.
//

import UIKit
import AgoraRtcKit
import DarkEggKit
import DZAnimatedGift

class MultiChannelAudienceVC: UIViewController {
    //
    @IBOutlet private weak var roomScrollView: UIScrollView!
    
    //
    private var liveRooms: [LiveRoom] = []
    private var views: [ChannelVideoView] = []
    private var channelId: String? // current displayed channel Id
    private var tempNames: [String] = ["A", "B", "C"]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.roomScrollView.contentInsetAdjustmentBehavior = .never
        self.roomScrollView.delegate = self
        self.navigationController?.navigationBar.isHidden = true
        
        // make views array
        self.makeViewsArray()
        
        // get the room list for scroll
        LiveManager.shared.getLiveList().done { [weak self] list in
            //
            Logger.debug(list)
            self?.liveRooms = list
            let index = LiveManager.shared.currentIndex
            // main
            if let channelNaem = self?.liveRooms[index].name {
                if let cview = self?.views[1] {
                    self?.changeVideoView(view: cview, to: channelNaem)
                    cview.joinChannel(channelNaem, enableAudio: true)
                }
            }
            // sub
            if let channelNamePrev = LiveManager.shared.prev.name {
                if let cview = self?.views[0] {
                    self?.changeVideoView(view: cview, to: channelNamePrev)
                }
            }
            if let channelNameNext = LiveManager.shared.next.name {
                if let cview = self?.views[2] {
                    self?.changeVideoView(view: cview, to: channelNameNext)
                }
            }
        }.catch { error in
            Logger.debug("GetLiveList error.")
            Logger.debug(error.localizedDescription)
        }.finally {
            //
            Logger.debug("GetLiveList finally.")
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        // start join channel
    }
}

// MARK: - Actions
extension MultiChannelAudienceVC {
    @IBAction private func onCloseButtonClicked(_ sender: UIButton) {
        self.navigationController?.popViewController(animated: true)
        for cview in self.views {
            cview.leaveChannel()
        }
    }
}

// MARK: - Private functions
extension MultiChannelAudienceVC {
    /// make view array
    private func makeViewsArray() {
        for i in 0..<3 {
            let frame = CGRect(x: 0.0,
                               y: (CGFloat(i) - 0.0) * UIScreen.main.bounds.height,
                               width: UIScreen.main.bounds.width,
                               height: UIScreen.main.bounds.height)
            Logger.debug(frame)
            if let cview = Bundle.main.loadNibNamed("ChannelVideoView", owner: self, options: nil)?.first as? ChannelVideoView {
                cview.frame = frame
                Logger.debug("view\(i): \(cview)")
                self.views.append(cview)
                self.roomScrollView.addSubview(cview)
                self.roomScrollView.contentSize = CGSize(width: UIScreen.main.bounds.width,
                                                         height: UIScreen.main.bounds.height * 3)
                self.roomScrollView.contentOffset = CGPoint(x: 0, y: UIScreen.main.bounds.height)
            }
        }
    }
}

// MARK: - AgoraManagerDelegate
extension MultiChannelAudienceVC: AgoraManagerDelegate {
    func agoraManager(_ mgr: AgoraManager, userJoined uid: UInt) {
        Logger.debug("user \(uid) joined.")
    }
    
    func agoraManager(_ mgr: AgoraManager, userLeaved uid: UInt) {
        Logger.debug("user \(uid) leaved.")
    }
}

// MARK: - UIScrollViewDelegate
extension MultiChannelAudienceVC: UIScrollViewDelegate {
    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        //Logger.debug(scrollView.contentOffset.y)
        let screenHeight = UIScreen.main.bounds.height
        let yOffset = scrollView.contentOffset.y
        var r: LiveRoom?
        
        if yOffset - screenHeight > screenHeight/2 {
//            self.views[0].enableAudio(flag: false)
//            self.views[1].enableAudio(flag: false)
//            self.views[2].enableAudio(flag: true)
        }
        else if yOffset - screenHeight < (-1 * screenHeight/2) {
//            self.views[0].enableAudio(flag: true)
//            self.views[1].enableAudio(flag: false)
//            self.views[2].enableAudio(flag: false)
        }
        else {
//            self.views[0].enableAudio(flag: false)
//            self.views[1].enableAudio(flag: true)
//            self.views[2].enableAudio(flag: false)
        }
        
        if SettingManager.shared.enablePreload {
            // already loaded
        }
        else {
            if yOffset - screenHeight > 32 {
                // load next one
                r = LiveManager.shared.next
                let cview = views[2]
                if cview.channelId == nil, let cname = r?.name {
                    Logger.debug("load next one")
                    cview.joinChannel(cname)
                    cview.enableAudio(flag: false)
                }
            }
            else if yOffset - screenHeight < -32 {
                // load prev one
                r = LiveManager.shared.prev
                // load prev one
                let cview = views[0]
                if cview.channelId == nil, let cname = r?.name {
                    Logger.debug("load prev one")
                    cview.joinChannel(cname)
                    cview.enableAudio(flag: false)
                }
            }
        }
    }
    
    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        let yOffset = scrollView.contentOffset.y
        let screenHeight = UIScreen.main.bounds.height
        let viewIndex = Int(yOffset/screenHeight)
        
        guard let step = IndexStepping(rawValue: viewIndex - 1) else {
            Logger.debug("Not move, do nothing.")
            if !SettingManager.shared.enablePreload {
                self.views[0].leaveChannel()
                self.views[2].leaveChannel()
            }
            return
        }
        
        //if let step = IndexStepping(rawValue: viewIndex - 1) {
            Logger.debug("step \(step.rawValue)")
            LiveManager.shared.updateIndex(step)
        //}

        // swap
        // set main view to center
        //swap(&views, 1, viewIndex)
        let direction: ReorderDirection = ((step == .forward) ? .headToTail : .tailToHead)
        reorder(&views, direction: direction)
        // debug
        //reorder(&tempNames, direction: direction)
        //Logger.debug("tempNames: \(tempNames)")
        
        // 20230417 update
        // if scroll up(a == 1), do not change any thing in view[0]
        // if scroll down(a == -1). do not change any thin in view[2]
        // if scroll none(a == 0). do not change any thin in view[0] and view[2]
        for (idx, v) in self.views.enumerated() {
            let frame = CGRect(x: 0.0,
                               y: (CGFloat(idx) - 0.0) * UIScreen.main.bounds.height,
                               width: UIScreen.main.bounds.width,
                               height: UIScreen.main.bounds.height)
            v.frame = frame
            v.enableAudio(flag: (idx == 1))
        }
        
        // view 0 // preload
        // view 1 // display video <- at this time, user can only see this view
        // view 2 // preload
        
        switch direction {
        case .headToTail:
            let oldView = self.views[0]
            if !SettingManager.shared.enablePreload {
                oldView.leaveChannel()
            }
            if let channelNameNext = LiveManager.shared.next.name {
                let cview = self.views[2]
                cview.leaveChannel()
                self.changeVideoView(view: cview, to: channelNameNext)
            }
            break
        case .tailToHead:
            let oldView = self.views[2]
            if !SettingManager.shared.enablePreload {
                oldView.leaveChannel()
            }
            if let channelNamePrev = LiveManager.shared.prev.name {
                let cview = self.views[0]
                cview.leaveChannel()
                self.changeVideoView(view: cview, to: channelNamePrev)
            }
            break
        }
        
        DispatchQueue.main.async {
            // reset the offset
            self.roomScrollView.contentOffset = CGPoint(x: 0, y: UIScreen.main.bounds.height)
        }
    }
}

extension MultiChannelAudienceVC {
    private func changeVideoView(view cview: ChannelVideoView, to channel: String) {
        cview.channelId = channel
        cview.coverImageUrl = channel
        if SettingManager.shared.enablePreload {
            // preload
            cview.joinChannel(channel, enableAudio: false)
        }
        else {
            // not preload
            cview.leaveChannel()
        }
    }
    
    private func swap<T>(_ nums: inout[T], _ a: Int, _ b: Int) {
        let count = nums.count
        if a == b || a < 0 || a > count - 1 || b < 0 || b > count - 1 {
            return
        }
        (nums[a],nums[b]) = (nums[b],nums[a])
    }
    
    enum ReorderDirection {
        case headToTail
        case tailToHead
    }
    
    private func reorder<T>(_ arr: inout[T], direction: ReorderDirection) {
        //let count = arr.count
        switch direction {
        case .headToTail:
            let temp = arr[0]
            arr.remove(at: 0)
            arr.append(temp)
            break
        case .tailToHead:
            if let temp = arr.popLast() {
                arr.insert(temp, at: 0)
            }
            break
        }
    }
    
    private func updateUI(room: LiveRoom) {
        // TODO:
    }
}
