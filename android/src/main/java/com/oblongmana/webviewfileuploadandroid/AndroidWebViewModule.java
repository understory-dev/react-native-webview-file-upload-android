package com.oblongmana.webviewfileuploadandroid;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.FileNotFoundException;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ActivityEventListener;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.content.ContentResolver;

import com.facebook.react.common.annotations.VisibleForTesting;

public class AndroidWebViewModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private ContentResolver mContentResolver;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadCallbackAboveL;

    @VisibleForTesting
    public static final String REACT_CLASS = "AndroidWebViewModule";

    public AndroidWebViewModule(ReactApplicationContext context){
        super(context);
        context.addActivityEventListener(this);
        mContentResolver = context.getContentResolver();
    }

    private AndroidWebViewPackage aPackage;

    public void setPackage(AndroidWebViewPackage aPackage) {
        this.aPackage = aPackage;
    }

    public AndroidWebViewPackage getPackage() {
        return this.aPackage;
    }

    @Override
    public String getName(){
        return REACT_CLASS;
    }

    @SuppressWarnings("unused")
    public Activity getActivity() {
        return getCurrentActivity();
    }

    public void setUploadMessage(ValueCallback<Uri> uploadMessage) {
        mUploadMessage = uploadMessage;
    }

    public void setmUploadCallbackAboveL(ValueCallback<Uri[]> mUploadCallbackAboveL) {
        this.mUploadCallbackAboveL = mUploadCallbackAboveL;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) throws FileNotFoundException {
        Log.i("############################", "onActivityResult");
        // super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL){
                return;
            }
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) throws FileNotFoundException {
        Log.i("############################", "onActivityResultAboveL");
        if (requestCode != 1 || mUploadCallbackAboveL == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            Log.i("############################", "RESULT_OK");
            if (data != null) {
                Bundle extras = data.getExtras();
                ClipData clipData = data.getClipData();
                String dataString = data.getDataString();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        Log.i("############################", "imageBitmap != null");
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName = "IMG_" + timeStamp;
                        String imagePath = MediaStore.Images.Media.insertImage(mContentResolver, imageFileName, "Image", null);
                        results = new Uri[]{Uri.parse(imagePath)};
                    }
                }
                if (clipData != null) {
                    Log.i("############################", "clipData != null");
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                        Log.i("############################", item.getUri().toString());
                    }
                } else if (dataString != null) {
                    Log.i("############################", "dataString != null");
                    Log.i("############################", dataString);
                    results = new Uri[]{Uri.parse(dataString)};
                }
                /*
                public Uri getImageUri(Context inContext, Bitmap inImage) {
                  return Uri.parse(path);
                }
                */
            }
        }
        mUploadCallbackAboveL.onReceiveValue(results);
        mUploadCallbackAboveL = null;
    }
    public void onNewIntent(Intent intent) {}
}
