////
////  AgoraPictureInPictureController.swift
////  AgoraMultiChannelSwitch
////
////  Created by Yuhua Hu on 2022/10/12.
////
//
//import UIKit
//import Foundation
//import AVKit
//import AgoraRtcKit
//
////
//class AgoraSampleBufferRender: UIView {
//    var displayLayer: AVSampleBufferDisplayLayer = AVSampleBufferDisplayLayer()
//    var _videoWidth: CGFloat = 0
//    var _videoHeight: CGFloat = 0
//    
//    required init?(coder: NSCoder) {
//        super.init(coder: coder)
//        self.layer.addSublayer(self.displayLayer)
//    }
//    
////    override class func awakeFromNib() {
////        super.awakeFromNib()
////        self.layer.addSublayer(self.displayLayer)
////    }
//    
//    override func layoutSubviews() {
//        super.layoutSubviews()
//        self.clipsToBounds = true
//        self.layoutDisplayLayer()
//    }
//    
//    private func layoutDisplayLayer() {
//        if (_videoWidth == 0 || _videoHeight == 0 || self.frame.size.equalTo(CGSize.zero)) {
//            return;
//        }
//        
//        let viewWidth = self.frame.size.width;
//        let viewHeight = self.frame.size.height;
//        let videoRatio = _videoWidth/_videoHeight;
//        let viewRatio = viewWidth/viewHeight;
//        
//        var videoSize: CGSize;
//        if (videoRatio >= viewRatio) {
//            videoSize.height = viewHeight;
//            videoSize.width = videoSize.height * videoRatio;
//        }else {
//            videoSize.width = viewWidth;
//            videoSize.height = videoSize.width / videoRatio;
//        }
//        
//        let renderRect: CGRect = CGRect(x: 0.5 * (viewWidth - videoSize.width), y: 0.5 * (viewHeight - videoSize.height), width: videoSize.width, height: videoSize.height)
//        //CGRectMake(0.5 * (viewWidth - videoSize.width), 0.5 * (viewHeight - videoSize.height), videoSize.width, videoSize.height);
//
//        if (!renderRect.equalTo(self.displayLayer.frame)) {
//            self.displayLayer.frame = renderRect;
//        }
//    }
//    
//    // MARK: -
//    func reset() {
//        self.displayLayer.flushAndRemoveImage()
//    }
//    
//    func render(videoData: AgoraOutputVideoFrame?) {
//        //
//        guard let _videoData = videoData else {
//            return
//        }
//        
//        DispatchQueue.main.async {
//            self._videoWidth = CGFloat(_videoData.width)
//            self._videoHeight = CGFloat(_videoData.height)
//            self.layoutDisplayLayer()
//        }
//        
//        let width: size_t = Int(_videoData.width)
//        let height: size_t = Int(_videoData.height)
//        let yStride: size_t = Int(_videoData.yStride)
//        let uStride: size_t = Int(_videoData.uStride)
//        let vStride: size_t = Int(_videoData.vStride)
//        
//        let yBuffer = _videoData.yBuffer
//        let uBuffer = _videoData.uBuffer
//        let vBuffer = _videoData.vBuffer
//        
//        let pixelBuffer: CVPixelBuffer? = nil
//        
//        let pixelAttributes: [String(kCVPixelBufferIOSurfacePropertiesKey): AnyObject] = [:]
//    }
//    
//    func render(videoPixelBuffer: AgoraOutputVideoFrame) {
//        
//    }
//}
//
//protocol AgoraPictureInPictureControllerDelegate: AnyObject {
//    func agoraPinP(_ controller: AgoraPictureInPictureController, didShow: Bool)
//}
//
//class AgoraPictureInPictureController: NSObject {
//    internal weak var delegate: AgoraPictureInPictureControllerDelegate?
//    internal var pipController: AVPictureInPictureController
//    internal var displayView: AgoraSampleBufferRender?
//    
//    init(_displayView: AgoraSampleBufferRender) {
//        if #available(iOS 15.0, *) {
//            if AVPictureInPictureController.isPictureInPictureSupported() {
//                displayView = _displayView
//                super.init()
//                AVPictureInPictureControllerContentSource(sampleBufferDisplayLayer: displayView?.displayLayer, playbackDelegate: self)
//            }
//        }
//        
//        return nil
//    }
//    
//}
