//
//  Enum.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/11.
//

import Foundation

enum LiveType: String {
    case standard
    case premium
    
    var segueId: String {
        switch self {
        case .standard:
            return "EnterStandard"
        case .premium:
            return "EnterPremium"
        }
    }
}
