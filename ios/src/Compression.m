//
//  Compression.m
//  imageCropPicker
//
//  Created by Ivan Pusic on 12/24/16.
//  Copyright © 2016 Ivan Pusic. All rights reserved.
//

#import "Compression.h"
@import Photos;

@implementation Compression

- (instancetype)init {
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] initWithDictionary:@{
                                                                                 @"640x480": AVAssetExportPreset640x480,
                                                                                 @"960x540": AVAssetExportPreset960x540,
                                                                                 @"1280x720": AVAssetExportPreset1280x720,
                                                                                 @"1920x1080": AVAssetExportPreset1920x1080,
                                                                                 @"LowQuality": AVAssetExportPresetLowQuality,
                                                                                 @"MediumQuality": AVAssetExportPresetMediumQuality,
                                                                                 @"HighestQuality": AVAssetExportPresetHighestQuality,
                                                                                 @"Passthrough": AVAssetExportPresetPassthrough,
                                                                                 }];
    
    if (@available(iOS 9.0, *)) {
        [dic addEntriesFromDictionary:@{@"3840x2160": AVAssetExportPreset3840x2160}];
    } else {
        // Fallback on earlier versions
    }
    
    self.exportPresets = dic;
    
    return self;
}

- (ImageResult*) compressImageDimensions:(UIImage*)image
                   compressImageMaxWidth:(CGFloat)maxWidth
                  compressImageMaxHeight:(CGFloat)maxHeight
                              intoResult:(ImageResult*)result {
    
    CGFloat oldWidth = image.size.width;
    CGFloat oldHeight = image.size.height;
    
    int newWidth = 0;
    int newHeight = 0;
    
    if (maxWidth < maxHeight) {
        newWidth = maxWidth;
        newHeight = (oldHeight / oldWidth) * newWidth;
    } else {
        newHeight = maxHeight;
        newWidth = (oldWidth / oldHeight) * newHeight;
    }
    CGSize newSize = CGSizeMake(newWidth, newHeight);
    
    UIGraphicsImageRenderer *renderer = [[UIGraphicsImageRenderer alloc] initWithSize:newSize];
    UIImage *resizedImage = [renderer imageWithActions:^(UIGraphicsImageRendererContext * _Nonnull rendererContext) {
        [image drawInRect:CGRectMake(0, 0, newSize.width, newSize.height)];
    }];
    
    result.width = [NSNumber numberWithFloat:newWidth];
    result.height = [NSNumber numberWithFloat:newHeight];
    result.image = resizedImage;
    return result;
}

- (ImageResult*) compressImage:(UIImage*)image
                   withOptions:(NSDictionary*)options {
    
    ImageResult *result = [[ImageResult alloc] init];
    result.width = @(image.size.width);
    result.height = @(image.size.height);
    result.image = image;
    result.mime = @"image/jpeg";
    
    NSNumber *compressImageMaxWidth = [options valueForKey:@"compressImageMaxWidth"];
    NSNumber *compressImageMaxHeight = [options valueForKey:@"compressImageMaxHeight"];
    
    // determine if it is necessary to resize image
    BOOL shouldResizeWidth = (compressImageMaxWidth != nil && [compressImageMaxWidth floatValue] < image.size.width);
    BOOL shouldResizeHeight = (compressImageMaxHeight != nil && [compressImageMaxHeight floatValue] < image.size.height);
    
    if (shouldResizeWidth || shouldResizeHeight) {
        CGFloat maxWidth = compressImageMaxWidth != nil ? [compressImageMaxWidth floatValue] : image.size.width;
        CGFloat maxHeight = compressImageMaxHeight != nil ? [compressImageMaxHeight floatValue] : image.size.height;
        
        [self compressImageDimensions:image
                compressImageMaxWidth:maxWidth
               compressImageMaxHeight:maxHeight
                           intoResult:result];
    }
    
    // parse desired image quality
    NSNumber *compressQuality = [options valueForKey:@"compressImageQuality"];
    if (compressQuality == nil) {
        compressQuality = [NSNumber numberWithFloat:0.8];
    }
    
    // convert image to jpeg representation
    result.data = UIImageJPEGRepresentation(result.image, [compressQuality floatValue]);
    
    return result;
}

- (void)compressVideo:(NSURL*)inputURL
            outputURL:(NSURL*)outputURL
          withOptions:(NSDictionary*)options
              handler:(void (^)(AVAssetExportSession*))handler {
    NSString *presetKey = [options valueForKey:@"compressVideoPreset"];
    if (presetKey == nil) {
        presetKey = @"MediumQuality";
    }
    
    NSString *preset = [self.exportPresets valueForKey:presetKey];
    if (preset == nil) {
        preset = AVAssetExportPresetMediumQuality;
    }
    
    [[NSFileManager defaultManager] removeItemAtURL:outputURL error:nil];
    AVURLAsset *asset = [AVURLAsset URLAssetWithURL:inputURL options:nil];
    AVAssetExportSession *exportSession = [[AVAssetExportSession alloc] initWithAsset:asset presetName:preset];
    exportSession.shouldOptimizeForNetworkUse = YES;
    exportSession.outputURL = outputURL;
    exportSession.outputFileType = AVFileTypeMPEG4;
    
    [exportSession exportAsynchronouslyWithCompletionHandler:^(void) {
        handler(exportSession);
    }];
}

- (void) convertStringToURL:(NSString*)source handler:(void (^)(NSURL*))handler {
    if([source containsString:@"ph://"]){
        NSString *assetIdentifier = [source stringByReplacingOccurrencesOfString: @"ph://" withString: @""];
        PHFetchResult *assetResult = [PHAsset fetchAssetsWithLocalIdentifiers: @[assetIdentifier] options:nil];
        PHAsset *phAsset = [assetResult firstObject];
        if(phAsset == nil || phAsset.mediaType != PHAssetMediaTypeVideo){
            [self exception:@"Failed to request asset."];
        }
        PHImageManager *manager = [PHImageManager defaultManager];
        PHVideoRequestOptions *options = [[PHVideoRequestOptions alloc] init];
        options.version = PHVideoRequestOptionsVersionOriginal;
        options.networkAccessAllowed = YES;
        options.deliveryMode = PHVideoRequestOptionsDeliveryModeHighQualityFormat;
        
        [manager
         requestAVAssetForVideo:phAsset
         options:options
         resultHandler:^(AVAsset * asset, AVAudioMix * audioMix,
                         NSDictionary *info) {
            handler([(AVURLAsset *)asset URL]);
        }];
    }else if ([source containsString:@"assets-library"]){
        handler([NSURL fileURLWithPath:source]);
    } else {
        NSURL *bundleURL = [[NSBundle mainBundle] bundleURL];
        handler([NSURL URLWithString:source relativeToURL: bundleURL]);
    }
}

- (void) exception:(NSString*)error {
    @throw [NSException exceptionWithName:@"" reason: error userInfo:nil];
}
@end
