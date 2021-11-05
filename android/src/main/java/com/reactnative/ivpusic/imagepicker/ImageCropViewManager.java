package com.reactnative.ivpusic.imagepicker;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.canhub.cropper.CropImageView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

public class ImageCropViewManager extends SimpleViewManager<CropImageView> {
    private final static String REACT_CLASS = "RCTCropView";
    private final static String ON_IMAGE_SAVED = "onImageSaved";
    private final static String SOURCE_URL_PROP = "sourceUrl";
    private final static String KEEP_ASPECT_RATIO_PROP = "keepAspectRatio";
    private final static String ASPECT_RATIO_PROP = "cropAspectRatio";
    private final static int SAVE_IMAGE_COMMAND = 1;
    private final static int ROTATE_IMAGE_COMMAND = 2;
    private final static String SAVE_IMAGE_COMMAND_NAME = "saveImage";
    private final static String ROTATE_IMAGE_COMMAND_NAME = "rotateImage";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected CropImageView createViewInstance(final ThemedReactContext reactContext) {
        final CropImageView view = new CropImageView(reactContext);
        view.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
            @Override
            public void onCropImageComplete(CropImageView cropImageView, CropImageView.CropResult result) {
                if (result.isSuccessful()) {
                    WritableMap map = Arguments.createMap();
                    try {
                        map.putString("uri", "file://" + RealPathUtil.getRealPathFromURI(reactContext, result.getUriContent()));
                    } catch (IOException e) {
                        map.putString("uri", "file://" + result.getUriFilePath(reactContext, true));
                    }
                    map.putInt("width", result.getCropRect().width());
                    map.putInt("height", result.getCropRect().height());
                    reactContext.getJSModule(RCTEventEmitter.class).receiveEvent(
                            view.getId(),
                            ON_IMAGE_SAVED,
                            map
                    );
                }
            }
        });
        return view;
    }

    @Override
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(ON_IMAGE_SAVED, MapBuilder.of("registrationName", ON_IMAGE_SAVED));
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                SAVE_IMAGE_COMMAND_NAME, SAVE_IMAGE_COMMAND,
                ROTATE_IMAGE_COMMAND_NAME, ROTATE_IMAGE_COMMAND
        );
    }

    @Override
    public void receiveCommand(@NonNull CropImageView root, int commandId, @Nullable ReadableArray args) {
        super.receiveCommand(root, commandId, args);
        if (args == null) {
            return;
        }

        switch (commandId) {
            case SAVE_IMAGE_COMMAND: {
                if (args != null) {
                    boolean preserveTransparency = args.getBoolean(0);
                    String extension = ".jpg";
                    Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
                    if (preserveTransparency && root.getCroppedImage().hasAlpha()) {
                        extension = ".png";
                        format = Bitmap.CompressFormat.PNG;
                    }
                    String tmpDirPath = root.getContext().getCacheDir() + "/react-native-image-crop-picker";
                    File tmpDir = new File(tmpDirPath);
                    if (!tmpDir.exists()) {
                        tmpDir.mkdir();
                    }
                    String path = new File(tmpDirPath, UUID.randomUUID().toString() + extension).toURI().toString();
                    int quality = args.getInt(1);
                    root.saveCroppedImageAsync(Uri.parse(path), format, quality, root.getCropRect().width(), root.getCropRect().height());
                }
                break;
            }
            case ROTATE_IMAGE_COMMAND: {
                boolean clockwise = args.getBoolean(0);
                int angle = clockwise ? 90 : -90;
                root.rotateImage(angle);
            }
        }
    }

    @ReactProp(name = SOURCE_URL_PROP)
    public void setSourceUrl(CropImageView view, @Nullable String url) {
        if (url != null) {
            view.setImageUriAsync(Uri.parse(url));
        }
    }

    @ReactProp(name = KEEP_ASPECT_RATIO_PROP)
    public void setFixedAspectRatio(CropImageView view, Boolean fixed) {
        view.setFixedAspectRatio(fixed);
    }

    @ReactProp(name = ASPECT_RATIO_PROP)
    public void setAspectRatio(CropImageView view, @Nullable ReadableMap aspectRatio) {
        if (aspectRatio != null) {
            view.setAspectRatio(aspectRatio.getInt("width"), aspectRatio.getInt("height"));
        } else {
            view.clearAspectRatio();
        }
    }
}
