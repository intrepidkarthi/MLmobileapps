package com.mlmobileapps.styletransfer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

import java.lang.reflect.InvocationTargetException;

public class CameraActivity extends AppCompatActivity{

    CameraView mCameraView;

    private static final int FLASH_MODES = 3;

    private static final Drawable[] flashDrawables = new Drawable[3];

    private long captureStartTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_camera);

        mCameraView = findViewById(R.id.camera);

        mCameraView.setMethod(CameraKit.Constants.METHOD_STILL);

        mCameraView.setCropOutput(true);

        mCameraView.setPermissions(CameraKit.Constants.PERMISSIONS_PICTURE);

        mCameraView.setJpegQuality(70);


        flashDrawables[0] = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_flash_off)
                .color(getResources().getColor(R.color.colorIcon))
                .sizeDp(20)
                .contourColor(getResources().getColor(android.R.color.black))
                .contourWidthPx(1);
        flashDrawables[1] = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_flash_on)
                .color(getResources().getColor(R.color.colorIcon))
                .sizeDp(20)
                .contourColor(getResources().getColor(android.R.color.black))
                .contourWidthPx(1);
        flashDrawables[2] = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_flash_auto)
                .color(getResources().getColor(R.color.colorIcon))
                .sizeDp(20)
                .contourColor(getResources().getColor(android.R.color.black))
                .contourWidthPx(1);

        findViewById(R.id.picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureStartTime = System.currentTimeMillis();
                mCameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {
                    @Override
                    public void callback(CameraKitImage cameraKitImage) {
                        byte[] jpeg = cameraKitImage.getJpeg();

                        // Get the dimensions of the bitmap
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

                        // Decode the image file into a Bitmap sized to fill the View
                        //bmOptions.inJustDecodeBounds = false;
                        bmOptions.inMutable = true;

                        long callbackTime = System.currentTimeMillis();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length, bmOptions);
                        ResultHolder.dispose();
                        ResultHolder.setImage(bitmap);
                        ResultHolder.setNativeCaptureSize(mCameraView.getCaptureSize());
                        ResultHolder.setTimeToCallback(callbackTime - captureStartTime);
                        Intent intent = new Intent(getApplicationContext(), ShowImageActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });

        findViewById(R.id.pickfromgallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), GalleryActivity.class);
                startActivity(i);
            }
        });

        findViewById(R.id.showfrontcam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.setFacing(mCameraView.getFacing()^1);
            }
        });

        Point statusBarSize = getNavigationBarSize(getApplicationContext());

        int navigationBarHeight = statusBarSize!=null?statusBarSize.y:0;
        RelativeLayout relativeLayout = findViewById(R.id.camera_controls);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) relativeLayout.getLayoutParams();
        layoutParams.setMargins(0,0,0,navigationBarHeight);
        relativeLayout.setLayoutParams(layoutParams);

        int statusBarHeight = getStatusBarHeight(getApplicationContext());


        ImageView statusBarBg = findViewById(R.id.statusBarBg);
        statusBarBg.getLayoutParams().height = statusBarHeight;
        statusBarBg.requestLayout();

        final RelativeLayout relativeLayout1= findViewById(R.id.topControls);
        RelativeLayout.LayoutParams relativeLayoutLayoutParams = (RelativeLayout.LayoutParams) relativeLayout1.getLayoutParams();
        relativeLayoutLayoutParams.setMargins(0,statusBarHeight,0,0);
        relativeLayout1.setLayoutParams(relativeLayoutLayoutParams);

        ImageView navigationBar = findViewById(R.id.navigartionBarBg);
        navigationBar.getLayoutParams().height = navigationBarHeight;
        navigationBar.requestLayout();

        final ImageView toggleFlash = findViewById(R.id.toggleFlash);

        toggleFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.setFlash((mCameraView.getFlash()+1)%FLASH_MODES);
                toggleFlash.setImageDrawable(flashDrawables[mCameraView.getFlash()]);
            }
        });

    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        // navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }

        // navigation bar is not present
        return new Point();
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (IllegalAccessException e) {} catch (InvocationTargetException e) {} catch (NoSuchMethodException e) {}
        }

        return size;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }
}
