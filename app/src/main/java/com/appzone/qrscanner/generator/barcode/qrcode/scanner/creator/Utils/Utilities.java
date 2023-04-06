package com.appzone.qrscanner.generator.barcode.qrcode.scanner.creator.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore;

public class Utilities {
    public static String HISTORY_LIST = "historylist";

    // Settings
    public static String Vib_setting = "Vib_setting";
    public static String Sound_setting = "Sound_setting";
    public static String Clip_setting = "Clip_setting";
    public static String Web_setting = "Web_setting";
    public static String Save_his_setting = "Save_his_setting";





    public static void addImageToGallery(final String filePath, final Context context) {
        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }


}
