//
//  SettingVC.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/26.
//

import UIKit

class SettingVC: UIViewController {
    @IBOutlet private weak var preloadSwitch: UISwitch!
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
}

extension SettingManager {
    @IBAction private func onPreloadSwitchChanged(_ sender: UISwitch) {
        SettingManager.shared.changeSetting(key: "kPreload", toValue: sender.isOn)
    }
}
