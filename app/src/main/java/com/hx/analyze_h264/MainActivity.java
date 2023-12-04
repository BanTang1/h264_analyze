package com.hx.analyze_h264;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.hx.analyze_h264.databinding.ActivityMainBinding;

import java.io.File;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("analyze_h264");
    }

    private final String TAG = "liudehua";

    private ActivityMainBinding binding;
    private H264Player h264Player;

    private final ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        String permission = entry.getKey();
                        Boolean isGranted = entry.getValue();
//                        Log.i(TAG, "onActivityResult: permission = " + permission);
//                        Log.i(TAG, "onActivityResult: isGranted = " + isGranted);
                        if (isGranted) {
                            // Permission is granted. Do your work here.
                        } else {
                            // Permission is denied. Handle the error here.
                        }
                    }
                }
            });

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // target api = 33
        String[] permissions = {
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_IMAGES
        };
        if (!checkPermissions(permissions)) {
            requestMultiplePermissionsLauncher.launch(permissions);
        }
        FileUtil.copyRawResourceToFile(this, R.raw.output, FileUtil.getAppPrivateDir(this));
        initSurface();
    }

    /**
     * 当Surface View创建时，开始播放视频
     */
    private void initSurface() {

        final SurfaceHolder surfaceHolder = binding.surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                File file = new File(getExternalFilesDir(null), "output.h264");
                Log.i(TAG, "surfaceCreated: file = " + file.getAbsolutePath());
                if (file.exists()) {
                    h264Player = new H264Player(file.getAbsolutePath(), holder.getSurface());
                    h264Player.play();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                Log.i(TAG, "surfaceChanged: ");
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                Log.i(TAG, "surfaceDestroyed: ");
                if (h264Player != null) {
                    h264Player.destroyed();
                }
            }
        });
    }

    private boolean checkPermissions(String[] permissions) {
        for (String p : permissions) {
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}