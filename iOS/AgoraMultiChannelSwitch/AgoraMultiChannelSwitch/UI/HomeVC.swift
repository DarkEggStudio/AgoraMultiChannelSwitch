//
//  HomeVC.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/11.
//

import UIKit
import DarkEggKit

class HomeVC: UIViewController {
    //
    @IBOutlet private weak var channelNameField: UITextField!
    @IBOutlet private weak var standardButton: UIButton!
    @IBOutlet private weak var premiumButton: UIButton!
    
    @IBOutlet private weak var localUserIdField: UITextField!
    @IBOutlet private weak var multiChannelTypeSegment: UISegmentedControl!
    @IBOutlet private weak var multiChannelButton: UIButton!

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        UserManager.shared.login()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
//        AgoraRtmManager.shared.login(uid: "testhyh") { success, message in
//            if success {
//                Logger.debug("RTM Login success.")
//            }
//            else {
//                Logger.debug(message)
//            }
//        }
    }
}

extension HomeVC {
    
    @IBAction private func onSettingButtonClicked(_ sender: UIBarButtonItem) {
        Logger.debug()
        self.performSegue(withIdentifier: "EnterSettingScene", sender: sender)
    }
    
    // MARK: -
    @IBAction private func onButtonClicked(_ sender: UIButton?) {
        // check channel name
        if checkChannelName() {
            Logger.debug()
            self.performSegue(withIdentifier: "EnterAudienceScene", sender: sender)
        }
    }
    
    /// Check channel name
    private func checkChannelName() -> Bool{
        guard let channelName = channelNameField.text, !channelName.isEmpty else {
            Logger.debug("Please enter channel name.")
            self.showErrorAlert(title: "Cannot proceed", message: "Please enter the channel name.")
            return false
        }
        return true
    }
    
    // MARK: -
//    private func enterAudienceRoom(_ sender: UIButton) {
//        self.performSegue(withIdentifier: "EnterAudienceScene", sender: sender)
//    }
    
    private func checkLocalUid() -> Bool {
        guard let localUidStr = self.localUserIdField.text, let localUid = UInt(localUidStr) else {
            return false
        }
        Logger.debug("Local Uid : \(localUid)")
        return true
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let btn = sender as? UIButton {
            switch btn {
            case standardButton:
                let vc = segue.destination as! AudienceRoomVC
                vc.channelName = channelNameField.text
                vc.type = .standard
                break
            case premiumButton:
                let vc = segue.destination as! AudienceRoomVC
                vc.channelName = channelNameField.text
                vc.type = .premium
                break
            case multiChannelButton:
                break
            default:
                break
            }
        }
    }
}

extension HomeVC {
    @IBAction private func onMultiChannelButtonClicked(_ sender: UIButton) {
        if checkLocalUid() {
            Logger.debug()
            self.performSegue(withIdentifier: "EnterMutliChannelScene", sender: sender)
        }
    }
    
    @IBAction private func onMultiTypeChanged(_ sender: UISegmentedControl) {
        switch sender.selectedSegmentIndex {
        case 0:
            SettingManager.shared.enablePreload = true
            break
        case 1:
            SettingManager.shared.enablePreload = false
            break
        default:
            SettingManager.shared.enablePreload = false
            break
        }
    }
}
