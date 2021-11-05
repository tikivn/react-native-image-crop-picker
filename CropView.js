import React, { createRef } from "react";
import {
  findNodeHandle,
  NativeSyntheticEvent,
  requireNativeComponent,
  StyleProp,
  UIManager,
  ViewStyle,
} from "react-native";

const RCTCropView = requireNativeComponent("CropView");

class CropView extends React.PureComponent {
  static defaultProps = {
    keepAspectRatio: false,
  };

  viewRef = createRef();

  saveImage = (preserveTransparency = true, quality = 90) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.viewRef.current),
      UIManager.getViewManagerConfig("CropView").Commands.saveImage,
      [preserveTransparency, quality]
    );
  };

  rotateImage = (clockwise = true) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this.viewRef.current),
      UIManager.getViewManagerConfig("CropView").Commands.rotateImage,
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
        cropAspectRatio={1}
      />
    );
  }
}

export default CropView;
