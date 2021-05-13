package com.example.mycamera1;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraManager {

    private static final int MSG_OPEN = 1;
    private static final int MSG_STARTPREVIEW = 2;
    private static final int MSG_STOPPREVIEW = 3;
    private static final int MSG_RELEASE = 4;
    private static final String TAG = "CameraManager";
    private Context mContext;
    private Handler mCameraHandler;
    private SurfaceHolder mSurfaceHolder;
    private int mCameraID;
    private Camera mCamera;
    private boolean cameraReady = false;


    public CameraManager(Context context) {
        mContext = context;
        HandlerThread cameraHandlerThread = new HandlerThread("CameraHandler");
        cameraHandlerThread.start();
        mCameraHandler = new Handler(cameraHandlerThread.getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case MSG_OPEN:  //打开摄像头
                        openCamera(mCameraID);
                        break;
                    case MSG_STARTPREVIEW:  //开始预览
                        mCamera.startPreview();
                        break;
                    case MSG_STOPPREVIEW:   //停止预览
                        mCamera.stopPreview();
                        break;
                    case MSG_RELEASE:   //释放资源
                        mCamera.release();
                        break;
                }
            }
        };
    }

    /**
     * 设置默认打开的是后置摄像头
     */
    public void init(){
        mCameraID = 0;
    }

    public void openCamera(){
        mCameraHandler.sendEmptyMessage(MSG_OPEN);
    }
    /**
     * 打开摄像头
     * @param id
     */
    public void openCamera(int id) {
        Log.i(TAG, "openCamera: " + id);
        try {
            //打开摄像头
            mCamera = Camera.open(id);
            //相机的服务设置,使相机参数生效，应用程序必须调用setParameters（相机参数）
            Camera.Parameters parameters = mCamera.getParameters();
            mCamera.setParameters(parameters);
            if (mSurfaceHolder != null){
                mCamera.setPreviewDisplay(mSurfaceHolder);//添加预览
            }
            //设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
            mCamera.setDisplayOrientation(90);
            mCameraHandler.sendEmptyMessage(MSG_STARTPREVIEW);//开始预览
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "openCamera: 相机打开异常");
        }
    }

    /**
     * 添加预览
     * @param holder
     */
    public void setSurfaceHolder(SurfaceHolder holder){
        if (holder != null){
            mSurfaceHolder = holder;
            if (mCamera != null){
                try {
                    mCamera.setPreviewDisplay(mSurfaceHolder);//添加预览
                    mCameraHandler.sendEmptyMessage(MSG_STARTPREVIEW);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview(){
        Log.i(TAG, "stopPreview: ");
        mCameraHandler.sendEmptyMessage(MSG_STOPPREVIEW);
    }

    /**
     * 释放资源
     */
    public void release(){
        Log.i(TAG, "release: ");
        mCameraHandler.sendEmptyMessage(MSG_RELEASE);
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        Log.i(TAG, "switchCamera: ");

        if (cameraReady) return;//判断摄像头是否就绪
        cameraReady = false;
        mCameraID = mCameraID == 0?1:0;//设置摄像头id
        mCameraHandler.sendEmptyMessage(MSG_STOPPREVIEW);//停止预览
        mCameraHandler.sendEmptyMessage(MSG_RELEASE);//释放资源
        openCamera();//打开摄像头
    }

    /**
     * 拍照
     */
    private Bitmap mBitmap;
    public Bitmap takePicture(){

        //拍照
        mCamera.takePicture(null,null,new Camera.PictureCallback(){

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //将字节数组转化为图片
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                String name = "/sdcard/img/" + System.currentTimeMillis() + ".jpeg";
                mBitmap = bitmap;
                try {
                    FileOutputStream fileOutputStream =
                            new FileOutputStream(name);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,85,fileOutputStream);

                    // 修正图片的旋转角度，设置其不旋转。这里也可以设置其旋转的角度，可以传值过去，
                    // 例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
                    ExifInterface exifInterface = new ExifInterface(name);
                    exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_90));
                    exifInterface.saveAttributes();

                    fileOutputStream.close();//关闭资源
                    camera.stopPreview();//关闭预览
                    camera.startPreview();//开启预览
                    Log.i(TAG, "onPictureTaken: ");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        return mBitmap;
    }

}
