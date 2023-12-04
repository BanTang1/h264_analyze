package com.hx.analyze_h264;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {

    public static void copyRawResourceToFile(Context context, int resourceId, String destinationPath) {
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        OutputStream outputStream = null;

        try {
            File destinationFile = new File(destinationPath);
            outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            Log.d("FileUtil", "File copied successfully");
        } catch (IOException e) {
            Log.e("FileUtil", "Error copying file", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                Log.e("FileUtil", "Error closing streams", e);
            }
        }
    }

    public static String getAppPrivateDir(Context context) {
        File file = new File(context.getExternalFilesDir(null), "output.h264");
        return file.getAbsolutePath();
    }
}
