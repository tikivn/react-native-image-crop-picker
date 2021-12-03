import React, { Component } from "react";
import {
  Alert,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
  Image,
  Platform,
} from "react-native";
import ImagePicker, { CropView } from "@tikivn/react-native-image-crop-picker";
console.log("b", CropView);
const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },
  button: {
    backgroundColor: "blue",
    marginBottom: 10,
  },
  text: {
    color: "white",
    fontSize: 20,
    textAlign: "center",
  },
});

export default class App extends Component {
  constructor() {
    super();
    this.cropViewRef = React.createRef();
    this.state = {
      image: null,
      cropImage: null,
    };
  }

  pickSingle(cropit, circular = false, mediaType) {
    ImagePicker.openPicker({
      width: 500,
      height: 200,
      cropping: cropit,
      cropperCircleOverlay: circular,
      sortOrder: "none",
      compressImageMaxWidth: 1000,
      compressImageMaxHeight: 1000,
      compressImageQuality: 1,
      compressVideoPreset: "MediumQuality",
      includeExif: true,
      cropperStatusBarColor: "white",
      cropperToolbarColor: "white",
      cropperActiveWidgetColor: "white",
      cropperToolbarWidgetColor: "#3498DB",
    })
      .then((image) => {
        console.log("received image", image);
        this.setState({
          image: {
            uri: image.path,
            width: image.width,
            height: image.height,
            mime: image.mime,
          },
          images: null,
        });
      })
      .catch((e) => {
        console.log(e);
        Alert.alert(e.message ? e.message : e);
      });
  }

  crop() {
    const cropView = this.cropViewRef.current;
    if (cropView) {
      cropView.saveImage(true, 100);
    }
  }

  scaledHeight(oldW, oldH, newW) {
    return (oldH / oldW) * newW;
  }

  render() {
    return (
      <View style={styles.container}>
        {this.state.image ? (
          <CropView
            ref={this.cropViewRef}
            sourceUrl={this.state.image.uri}
            style={{ flex: 1, width: "100%" }}
            onImageCrop={(res) => {
              console.log(res);
              this.setState({
                image: null,
                cropImage: res,
              });
            }}
            keepAspectRatio
            aspectRatio={{ width: 16, height: 9 }}
          />
        ) : null}
        {this.state.cropImage ? (
          <Image
            source={this.state.cropImage}
            style={{
              width: 300,
              height: 300,
              resizeMode: "contain",
              overflow: "hidden",
            }}
          />
        ) : null}
        <TouchableOpacity
          onPress={() => this.pickSingle(false)}
          style={styles.button}
        >
          <Text style={styles.text}>Select Single Image With Camera</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={() => this.crop()} style={styles.button}>
          <Text style={styles.text}>Select Single Image With Camera</Text>
        </TouchableOpacity>
      </View>
    );
  }
}
