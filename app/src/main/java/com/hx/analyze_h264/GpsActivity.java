package com.hx.analyze_h264;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Map;

public class GpsActivity extends AppCompatActivity {

    private final String TAG = "liudehua";
    private LocationManager locationManager;
    private TextView text1,text2;


    private ActivityResultLauncher<String[]> mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {

        }
    });
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            Log.i(TAG, "onLocationChanged: lng = " + lng);
            Log.i(TAG, "onLocationChanged: lat = " + lat);
            text1.setText(String.valueOf(lng));
            text2.setText(String.valueOf(lat));
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(TAG, "onStatusChanged: ");
        }

        public void onProviderEnabled(String provider) {
            Log.i(TAG, "onProviderEnabled: ");
        }

        public void onProviderDisabled(String provider) {
            Log.i(TAG, "onProviderDisabled: ");
        }
    };

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Log.i(TAG, "onLocationResult: onLocationResult");
            if (locationResult == null) {
                Log.i(TAG, "onLocationResult: locationResult is null");
                return;
            }
            for (Location location : locationResult.getLocations()) {
                // 更新位置信息
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                Log.i(TAG, "get current location: longitude = " + longitude);
                Log.i(TAG, "get current location: latitude = " + latitude);
                text1.setText(String.valueOf(longitude));
                text2.setText(String.valueOf(latitude));
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        text1 = new TextView(this);
        text2 = new TextView(this);
        linearLayout.addView(text1);
        linearLayout.addView(text2);
        setContentView(linearLayout);

        String[] permission = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mActivityResultLauncher.launch(permission);
        }


        PackageManager packageManager = getPackageManager();
        boolean hasGPS = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);

        if (hasGPS) {
            Log.i(TAG, "onCreate: 支持GPS");
        } else {
            Log.i(TAG, "onCreate: 不支持GPS");
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // 请求位置更新
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);




        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // 处理获取到的位置信息
                    if (location != null) {
                        double longitude = location.getLongitude();
                        double latitude = location.getLatitude();
                        Log.i(TAG, "get last location: longitude = " + longitude);
                        Log.i(TAG, "get last location: latitude = " + latitude);
                        text1.setText(String.valueOf(longitude));
                        text2.setText(String.valueOf(latitude));
                        // 使用获取到的经纬度信息
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.i(TAG, "onCreate: 位置获取失败");
                });


//        LocationRequest locationRequest = LocationRequest.create();
//        locationRequest.setInterval(5000); // 设置获取位置的间隔，单位为毫秒
//        locationRequest.setFastestInterval(0);  // 最快获取位置的间隔时间
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // 设置优先级
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }
}
