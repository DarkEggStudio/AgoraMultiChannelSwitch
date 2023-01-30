//
//  AgoraPictureInPictureController.h
//  AgoraMultiChannelSwitch
//
//  Created by Yuhua Hu on 2022/10/13.
//

#ifndef AgoraPictureInPictureController_h
#define AgoraPictureInPictureController_h

#import <Foundation/Foundation.h>
#import "AgoraSampleBufferRender.h"
#import <AVKit/AVKit.h>

NS_ASSUME_NONNULL_BEGIN
@protocol AgoraPictureInPictureControllerDelegate <NSObject>

@optional

@end

//仅支持IOS15以后
@interface AgoraPictureInPictureController : NSObject

@property (nonatomic, weak) id <AgoraPictureInPictureControllerDelegate>delegate;

@property (nonatomic, strong, readonly) AVPictureInPictureController *pipController;

@property (nonatomic, strong, readonly) AgoraSampleBufferRender *displayView;

- (instancetype)initWithDisplayView:(AgoraSampleBufferRender *)displayView;
    
@end

NS_ASSUME_NONNULL_END

#endif /* AgoraPictureInPictureController_h */
