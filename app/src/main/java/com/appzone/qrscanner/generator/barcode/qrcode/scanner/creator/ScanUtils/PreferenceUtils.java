package com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.ScanUtils;

import android.content.Context;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.camera.core.CameraSelector;

import com.google.common.base.Preconditions;

public class PreferenceUtils {

    public static boolean isCameraLiveViewportEnabled(Context context) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        String prefKey = context.getString(R.string.pref_key_camera_live_viewport);
//        return sharedPreferences.getBoolean(prefKey, true);
        return true;
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    public static android.util.Size getCameraXTargetResolution(Context context, int lensfacing) {
        Preconditions.checkArgument(
                lensfacing == CameraSelector.LENS_FACING_BACK
                        || lensfacing == CameraSelector.LENS_FACING_FRONT);
        String prefKey =
                lensfacing == CameraSelector.LENS_FACING_BACK
//                        ? context.getString(R.string.pref_key_camerax_rear_camera_target_resolution)
                        ? "crctas"
//                        : context.getString(R.string.pref_key_camerax_front_camera_target_resolution);
                        : "cfctas";
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return null;
//        try {
//            return android.util.Size.parseSize(sharedPreferences.getString(prefKey, null));
//        } catch (Exception e) {
//            return null;
//        }
    }
}
