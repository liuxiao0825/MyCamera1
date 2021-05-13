package com.example.mycamera1;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUESTCOOD_CAMERA = 1;
    private CameraManager mCameraManager;
    private SurfaceHolder mSurfaceHolder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 切换摄像头点击事件
         */
        ImageView cameraSwitch = findViewById(R.id.cameraswitch);
        cameraSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: ");
                mCameraManager.switchCamera();
            }
        });


        /**
         * 拍照点击事件
         */
        findViewById(R.id.capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = mCameraManager.takePicture();

                Toast.makeText(MainActivity.this, "保存照片成功", Toast.LENGTH_SHORT).show();

                ImageFilterView imageFilterView = (ImageFilterView)findViewById(R.id.thumbnails);
                imageFilterView.setImageBitmap(bitmap);

            }
        });

        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        mSurfaceHolder= surfaceView.getHolder();//得到SurfaceView的控制器
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback2() {
            @Override
            public void surfaceRedrawNeeded(SurfaceHolder holder) {

            }

            /**
             * 创建surface
             * @param holder
             */
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i(TAG, "surfaceCreated: ");
                mCameraManager.setSurfaceHolder(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            /**
             * 销毁surface
             * @param holder
             */
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i(TAG, "surfaceDestroyed: ");
            }
        });


        mCameraManager = new CameraManager(getApplicationContext());
        mCameraManager.init();
        //权限判断
        if (
                PackageManager.PERMISSION_DENIED == checkCallingOrSelfPermission(Manifest.permission.CAMERA)
                        || PackageManager.PERMISSION_GRANTED == checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        || PackageManager.PERMISSION_GRANTED == checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                ) {
            //请求获取权限，执行后回调onRequestPermissionsResult(。。。)函数
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},REQUESTCOOD_CAMERA);
        }else {
            mCameraManager.openCamera();
        }
    }

    /**
     * 请求权限的结果
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUESTCOOD_CAMERA){     //请求权限成功
            //权限判断
            if (checkCallingOrSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                mCameraManager.openCamera();//打开摄像头
            }else {
                Toast.makeText(this,"没有授权使用相机",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStop() { //停止预览
        mCameraManager.stopPreview();
        super.onStop();
    }

    @Override
    protected void onDestroy() { //销毁资源
        mCameraManager.release();
        super.onDestroy();
    }
}