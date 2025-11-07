package com.example.cam_app;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final String TAG = "CustomCameraApp";

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private boolean isCameraOpened = false;
    private boolean isPreviewRunning = false;

    private Button btnOpenCamera, btnStartPreview, btnStopPreview, btnCloseCamera, btnCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CAMERA_PERMISSION);
            }
        }

        // UI initialization
        surfaceView = findViewById(R.id.surfaceView);
        btnOpenCamera = findViewById(R.id.btnOpenCamera);
        btnStartPreview = findViewById(R.id.btnStartPreview);
        btnStopPreview = findViewById(R.id.btnStopPreview);
        btnCloseCamera = findViewById(R.id.btnCloseCamera);
        btnCapture = findViewById(R.id.btnCapture); // ✅ Capture button

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Button listeners
        btnOpenCamera.setOnClickListener(v -> openCamera());
        btnStartPreview.setOnClickListener(v -> startCameraPreview());
        btnStopPreview.setOnClickListener(v -> stopCameraPreview());
        btnCloseCamera.setOnClickListener(v -> closeCamera());
        btnCapture.setOnClickListener(v -> captureImage()); // ✅ Capture
    }

    // Custom open method
    protected Camera open(int startId, int cameraNum, @Nullable Integer width, @Nullable Integer height, int is360View) {
        try {
            Class<Camera> cls = Camera.class;
            Method open = cls.getMethod("open", int.class, int.class, int.class, int.class, int.class);
            return (Camera) open.invoke(null, startId, cameraNum, width, height, is360View);
        } catch (Exception e) {
            Log.e(TAG, "Custom open() failed: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void openCamera() {
        if (!isCameraOpened) {
            try {
                camera = open(16, 1, 2592, 1944, 0);
                if (camera != null) {
                    isCameraOpened = true;
                    Toast.makeText(this, "Camera opened", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Camera open returned null", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Failed to open camera", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "openCamera: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Camera already opened", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCameraPreview() {
        if (camera != null && !isPreviewRunning) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                isPreviewRunning = true;
                Toast.makeText(this, "Preview started", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Failed to start preview", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "startPreview: " + e.getMessage());
            }
        }
    }

    private void stopCameraPreview() {
        if (camera != null && isPreviewRunning) {
            camera.stopPreview();
            isPreviewRunning = false;
            Toast.makeText(this, "Preview stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private void closeCamera() {
        if (camera != null) {
            if (isPreviewRunning) {
                camera.stopPreview();
                isPreviewRunning = false;
            }
            camera.release();
            camera = null;
            isCameraOpened = false;
            Toast.makeText(this, "Camera closed", Toast.LENGTH_SHORT).show();
        }
    }

    // Capture and save image
    private void captureImage() {
        if (camera != null && isPreviewRunning) {
            try {
                camera.takePicture(null, null, (data, cam) -> {
                    saveImageToStorage(data);
                    Toast.makeText(this, "Picture Captured & Saved!", Toast.LENGTH_SHORT).show();
                    cam.startPreview();
                });
            } catch (Exception e) {
                Toast.makeText(this, "Capture failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "captureImage: " + e.getMessage());
            }
        } else {
            Toast.makeText(this, "Start preview first!", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveImageToStorage(byte[] data) {
        OutputStream outputStream = null;
        try {
            String fileName = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CustomCameraApp");
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    outputStream = getContentResolver().openOutputStream(uri);
                }
            } else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CustomCameraApp");
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, fileName);
                outputStream = new FileOutputStream(file);
                sendBroadcast(new android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }

            if (outputStream != null) {
                outputStream.write(data);
                outputStream.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "saveImageToStorage: " + e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
