//
//  GiftQueue.swift
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/07/29.
//

import UIKit

/// the keyPath of default array
private let kGiftListKeyPath = "giftList"

/// GiftQueue
public class GiftQueue: NSObject {
    /// singleton instance
    static let `default` = {GiftQueue()}();
    
    // MARK: - properties
    @objc var giftList = NSMutableArray();
    public var targetView: UIView?;
    var isRunning = false;
    var count:Int { return self.giftList.count };
    
    override init() {
        super.init();
        self.addObserver(self, forKeyPath: kGiftListKeyPath, options: [.new, .old], context: nil);
    }
}

// MARK: - public functions
extension GiftQueue {
    /// add one gift to queue.
    ///
    /// - Parameter object: git object
    public func add(object: GiftObject) {
        self.mutableArrayValue(forKey: kGiftListKeyPath).insert(object, at: 0);
    }
    
    /// stop the queue, clear all gift object.
    public func stop() {
        self.isRunning = false;
        self.removeAll();
    }
    
    
    /// pause the queue.
    public func pause() {
        self.isRunning = false;
    }
    
    /// start play the queue.
    public func start() {
        self.isRunning = true;
        self.next();
    }
    
}

// MARK: - internal functions
extension GiftQueue {
    /// add a gift object, move with absolute path.
    ///
    /// - Parameters:
    ///   - image: image of the gift
    ///   - startPoint: start poing
    ///   - absolutePath: absolute path
    ///   - duration: duration of animation
    internal func add(image: String, at startPoint: CGPoint, absolutePath: [[CGPoint]], duration: CFTimeInterval) {
        self.mutableArrayValue(forKey: kGiftListKeyPath).insert(GiftObject(image: image, startPoint: startPoint, path: absolutePath, duration: duration, relative: false), at: 0);
    }
    
    /// add a gift object, move with relative path.
    ///
    /// - Parameters:
    ///   - image: image of the gift
    ///   - startPoint: start poing
    ///   - absolutePath: relative path
    ///   - duration: duration of animation
    internal func add(image: String, at startPoint: CGPoint, relativePath: [[CGPoint]], duration: CFTimeInterval) {
        self.mutableArrayValue(forKey: kGiftListKeyPath).insert(GiftObject(image: image, startPoint: startPoint, path: relativePath, duration: duration, relative: true), at: 0);
    }
    
    /// add a gift object
    ///
    /// - Parameter gift: GiftObject
    internal func add(_ gift: GiftObject) {
        self.mutableArrayValue(forKey: kGiftListKeyPath).insert(gift, at: 0)
    }
    
    
    /// remove the last gift in list.
    internal func removeLast() {
        self.giftList.removeLastObject();
    }
    
    /// remove the gift at index.
    ///
    /// - Parameter index: index of object
    internal func remove(at index: Int) {
        self.giftList.removeObject(at: index);
    }
    
    /// remove all gift.
    internal func removeAll() {
        self.giftList.removeAllObjects();
    }
    
    /// play next gift.
    @objc internal func next() {
        guard self.count > 0 else {
            self.isRunning = false;
            return
        }
        //print("[AnimatedGift]the count of queue is \(self.count)");
        self.isRunning = true;
        if let gift = self.giftList.lastObject as? GiftObject {
            GiftAnimation.shared.play(gift, inView: self.targetView);
            self.removeLast();
            self.perform(#selector(next), with: nil, afterDelay: 0.1);
        }
    }
}

// MARK: - KVO
extension GiftQueue {
    public override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if keyPath == kGiftListKeyPath {
            if !self.isRunning {
                self.next();
            }
        }
    }
}
