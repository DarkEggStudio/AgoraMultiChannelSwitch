//
//  AudienceRoomVC.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/11.
//

import UIKit
import DarkEggKit

class AudienceRoomVC: UIViewController {
    //
    @IBOutlet private weak var videoView: UIView!
    @IBOutlet private weak var closeButton: UIButton!
    @IBOutlet private weak var heartButton: UIButton!
    
    @IBOutlet private weak var debugLabel: UILabel!
    
    var type: LiveType = .standard
    var channelName: String?
    
    let agoraMgr = AgoraManager.shared
    private let dateFormatter = DateFormatter()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        dateFormatter.dateFormat = "yyyyMMdd hh:mm:ss.SSS"
    }
    
    override func viewDidAppear(_ animated: Bool) {
        // display title
        guard let cname = channelName else {
            return
        }
        self.title = "\(self.channelName ?? "") - \(type.rawValue)"
        // join channel
        agoraMgr.delegate = self
        agoraMgr.join(channel: cname, type: type) { [weak self] success, channelName, uid in
            //
            guard success else {
                self?.showErrorAlert(title: "Error", message: "Can not join channel \(channelName)")
                return
            }
        }
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        agoraMgr.leave()
    }
}

extension AudienceRoomVC {
    @IBAction private func onCloseButtonClicked(_ sender: UIButton) {
        self.dismiss(animated: true)
    }
    
    @IBAction private func onHeartButtonClicked(_ sender: UIButton?) {
        // ChatManager.shared.sendHeart(to: channelName, count: 1)
        
        let startPoint = self.heartButton.center//CGPoint(x: self.view.frame.maxX-60, y:self.view.frame.maxY-60)
        let points = [[CGPoint(x: -40, y: -200), CGPoint(x: -120, y: -100)],
                      [CGPoint(x: 10, y: -400), CGPoint(x: 30, y: -300)]]
        DZAnimatedGift.addGift(image: "heart_pink", at: startPoint, relativePath: points, duration: drand48()+2.0)
    }
}

extension AudienceRoomVC: UIScrollViewDelegate {
    func scrollViewDidScroll(_ scrollView: UIScrollView) {
        //
    }
}

extension AudienceRoomVC: AgoraManagerDelegate {
    func agoraManager(_ mgr: AgoraManager, userJoined uid: UInt) {
        //
        Logger.debug("User \(uid) joined.")
//        self.videoView.backgroundColor = .red
        self.agoraMgr.setHostVideoView(self.videoView, forUser: uid)
    }
    
    func agoraManager(_ mgr: AgoraManager, userLeaved uid: UInt) {
        //
        Logger.debug("User \(uid) leaved.")
    }
    
    func agoraManager(_ mgr: AgoraManager, ReceivedString str: String?) {
        Logger.debug("3: \(dateFormatter.string(from: Date()))")
        DispatchQueue.main.async {
            self.debugLabel.text = str
            Logger.debug("4: \(self.dateFormatter.string(from: Date()))")
        }
    }
}

