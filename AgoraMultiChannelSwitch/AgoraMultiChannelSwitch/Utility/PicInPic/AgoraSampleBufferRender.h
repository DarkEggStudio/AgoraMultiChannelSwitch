//
//  AgoraSampleBufferRender.h
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/10/13.
//

#ifndef AgoraSampleBufferRender_h
#define AgoraSampleBufferRender_h

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import <AgoraRtcKit/AgoraRtcEngineKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface AgoraSampleBufferRender : UIView

@property (nonatomic, readonly) AVSampleBufferDisplayLayer *displayLayer;

- (void)reset;

- (void)renderVideoData:(AgoraOutputVideoFrame *_Nonnull)videoData;

- (void)renderVideoPixelBuffer:(AgoraOutputVideoFrame *_Nonnull)videoData;

@end

NS_ASSUME_NONNULL_END

#endif /* AgoraSampleBufferRender_h */
