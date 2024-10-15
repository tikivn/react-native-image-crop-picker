package com.reactnative.ivpusic.imagepicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import com.abedelazizshe.lightcompressorlibrary.CompressionListener;
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor;
import com.abedelazizshe.lightcompressorlibrary.VideoQuality;
import com.abedelazizshe.lightcompressorlibrary.config.Configuration;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by ipusic on 12/27/16.
 */

class Compression {

    File resize(
            Context context,
            String originalImagePath,
            int originalWidth,
            int originalHeight,
            int maxWidth,
            int maxHeight,
            int quality
    ) throws IOException {
        Pair<Integer, Integer> targetDimensions =
                this.calculateTargetDimensions(originalWidth, originalHeight, maxWidth, maxHeight);

        int targetWidth = targetDimensions.first;
        int targetHeight = targetDimensions.second;

        Bitmap bitmap = null;
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            bitmap = BitmapFactory.decodeFile(originalImagePath);
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calculateInSampleSize(originalWidth, originalHeight, targetWidth, targetHeight);
            bitmap = BitmapFactory.decodeFile(originalImagePath, options);
        }

        // Use original image exif orientation data to preserve image orientation for the resized bitmap
        ExifInterface originalExif = new ExifInterface(originalImagePath);
        String originalOrientation = originalExif.getAttribute(ExifInterface.TAG_ORIENTATION);

        bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);

        File imageDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (!imageDirectory.exists()) {
            Log.d("image-crop-picker", "Pictures Directory is not existing. Will create this directory.");
            imageDirectory.mkdirs();
        }

        File resizeImageFile = new File(imageDirectory, UUID.randomUUID() + ".jpg");

        OutputStream os = new BufferedOutputStream(new FileOutputStream(resizeImageFile));
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);

        // Don't set unnecessary exif attribute
        if (shouldSetOrientation(originalOrientation)) {
            ExifInterface exif = new ExifInterface(resizeImageFile.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, originalOrientation);
            exif.saveAttributes();
        }

        os.close();
        bitmap.recycle();

        return resizeImageFile;
    }

    private int calculateInSampleSize(int originalWidth, int originalHeight, int requestedWidth, int requestedHeight) {
        int inSampleSize = 1;

        if (originalWidth > requestedWidth || originalHeight > requestedHeight) {
            final int halfWidth = originalWidth / 2;
            final int halfHeight = originalHeight / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfWidth / inSampleSize) >= requestedWidth
                    && (halfHeight / inSampleSize) >= requestedHeight) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private boolean shouldSetOrientation(String orientation) {
        return !orientation.equals(String.valueOf(ExifInterface.ORIENTATION_NORMAL))
                && !orientation.equals(String.valueOf(ExifInterface.ORIENTATION_UNDEFINED));
    }

    File resizeImageFile = new File(imageDirectory, UUID.randomUUID() + ".jpg");

    OutputStream os = new BufferedOutputStream(new FileOutputStream(resizeImageFile));
    resized.compress(Bitmap.CompressFormat.JPEG, quality, os);

    os.close();
    original.recycle();
    resized.recycle();

    return resizeImageFile;
  }

  int getRotationInDegreesForOrientationTag(int orientationTag) {
    switch (orientationTag) {
      case ExifInterface.ORIENTATION_ROTATE_90:
        return 90;
      case ExifInterface.ORIENTATION_ROTATE_270:
        return -90;
      case ExifInterface.ORIENTATION_ROTATE_180:
        return 180;
      default:
        return 0;
    }
  }

  File compressImage(final Context context, final ReadableMap options, final String originalImagePath, final BitmapFactory.Options bitmapOptions) throws IOException {
    Integer maxWidth = options.hasKey("compressImageMaxWidth") ? options.getInt("compressImageMaxWidth") : null;
    Integer maxHeight = options.hasKey("compressImageMaxHeight") ? options.getInt("compressImageMaxHeight") : null;
    Double quality = options.hasKey("compressImageQuality") ? options.getDouble("compressImageQuality") : null;

        if (maxWidth == null) maxWidth = bitmapOptions.outWidth;
        if (maxHeight == null) maxHeight = bitmapOptions.outHeight;

        return resize(context, originalImagePath, bitmapOptions.outWidth, bitmapOptions.outHeight, maxWidth, maxHeight, targetQuality);
    }

    private Pair<Integer, Integer> calculateTargetDimensions(int currentWidth, int currentHeight, int maxWidth, int maxHeight) {
        int width = currentWidth;
        int height = currentHeight;

        if (width > maxWidth) {
            float ratio = ((float) maxWidth / width);
            height = (int) (height * ratio);
            width = maxWidth;
        }

        if (height > maxHeight) {
            float ratio = ((float) maxHeight / height);
            width = (int) (width * ratio);
            height = maxHeight;
        }

        return Pair.create(width, height);
    }

    Log.d("image-crop-picker", "Image compression activated");

    // compression quality
    int targetQuality = quality != null ? (int) (quality * 100) : 100;
    Log.d("image-crop-picker", "Compressing image with quality " + targetQuality);

    if (maxWidth == null) {
      maxWidth = bitmapOptions.outWidth;
    } else {
      maxWidth = Math.min(maxWidth, bitmapOptions.outWidth);
    }

    if (maxHeight == null) {
      maxHeight = bitmapOptions.outHeight;
    } else {
      maxHeight = Math.min(maxHeight, bitmapOptions.outHeight);
    }

    return resize(context, originalImagePath, maxWidth, maxHeight, targetQuality);
  }

  VideoQuality getVideoQuality(String quality) {
    switch (quality) {
      case "HighestQuality":
        return VideoQuality.HIGH;
      case "LowQuality":
        return VideoQuality.LOW;
      default:
        return VideoQuality.MEDIUM;
    }
  }
  void compressVideo(final Context context, final ReadableMap options, final String originalVideo, final String compressedVideo, final Promise promise) {

    if (!options.hasKey("compressVideoPreset")) {
      promise.resolve(originalVideo);
      return;
    }

    VideoQuality quality = this.getVideoQuality(options.getString("compressVideoPreset"));
    Uri uri = Uri.parse(originalVideo);
    String srcPath = uri.getPath();
    MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
    metaRetriever.setDataSource(srcPath);

    final long currentTime = System.currentTimeMillis();
    VideoCompressor.start(
      context,
      uri,
      compressedVideo,
      new CompressionListener() {
        @Override
        public void onSuccess() {
          Log.d("mai.nguyen", String.valueOf(System.currentTimeMillis() - currentTime));
          promise.resolve(compressedVideo);
        }

        @Override
        public void onStart() {}

        @Override
        public void onProgress(float v) {
          Log.d("mai.nguyen", String.valueOf(v));
        }

        @Override
        public void onFailure(@NotNull String s) {
          Log.d("mai.nguyen onFailure",s);
          promise.reject(new Exception("Compression has failed"));
        }

        @Override
        public void onCancelled() {
          promise.reject(new Exception("Cancel compression"));
        }
      }, new Configuration(
        quality,
        null,
        false,
        null,
        false,
        false,
        null,
        null
      )
    );
  }
}
