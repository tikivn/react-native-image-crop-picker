import React, { createRef } from "react";
import {
  findNodeHandle,
  requireNativeComponent,
  UIManager,
} from "react-native";

const RCTCropView = requireNativeComponent("RCTCropView");

class CropView extends React.PureComponent {
  static defaultProps = {
    keepAspectRatio: false,
  };

  viewRef = createRef();

  saveImage = (preserveTransparency = true, quality = 90) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.viewRef.current),
      UIManager.getViewManagerConfig("RCTCropView").Commands.saveImage,
      [preserveTransparency, quality]
    );
  };

  rotateImage = (clockwise = true) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.viewRef.current),
      UIManager.getViewManagerConfig("RCTCropView").Commands.rotateImage,
      [clockwise]
    );
  };

  render() {
    const { sourceUrl, style, onImageCrop, keepAspectRatio, aspectRatio } =
      this.props;

    return (
      <RCTCropView
        ref={this.viewRef}
        sourceUrl={sourceUrl}
        style={style}
        onImageSaved={(event) => {
          onImageCrop(event.nativeEvent);
        }}
        keepAspectRatio={keepAspectRatio}
        cropAspectRatio={aspectRatio}
      />
    );
  }
}

export default CropView;
