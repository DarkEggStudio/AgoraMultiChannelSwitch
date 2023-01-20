//
//  BaseViewController.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/11.
//

import UIKit

extension UIViewController {
    // MARK: - Common functions
    func showWarningAlert(title: String, message: String? = nil) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
        let canncelAction = UIAlertAction(title: "OK", style: .cancel) { action in
            //
        }
        alert.addAction(canncelAction)
        self.present(alert, animated: true) {
            //
        }
    }
    
    /// Show error alert
    func showErrorAlert(title: String, message: String? = nil) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
        let closeAction = UIAlertAction(title: "OK", style: .default) { action in
            //
        }
        alert.addAction(closeAction)
        self.present(alert, animated: true) {
            //
        }
    }
}
