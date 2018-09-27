package com.mlmobileapps.styletransfer;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.mikepenz.iconics.context.IconicsContextWrapper;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;
import java.util.ArrayList;

import static com.mlmobileapps.styletransfer.CameraActivity.getNavigationBarSize;
import static com.mlmobileapps.styletransfer.CameraActivity.getStatusBarHeight;

public class ShowImageActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private boolean debug = false;
    private static final String MODEL_FILE = "file:///android_asset/art_style.pb";
    private static final String INPUT_NODE = "input";
    private static final String STYLE_NODE = "style_num";
    private static final String OUTPUT_NODE = "transformer/expand/conv3/conv/Sigmoid";
    private static final int NUM_STYLES = 26;

    private int MY_DIVISOR = 8;

    private static final String TAG = "ShowImageActivity";

    private final float[] styleVals = new float[NUM_STYLES];
    private int[] intValues;
    private float[] floatValues;

    private TensorFlowInferenceInterface inferenceInterface;
    private ImageView mPreviewImage = null;
    private ImageView mOriginalImage = null;
    private Bitmap mImgBitmap = null;
    private Bitmap mOrigBitmap = null;

    private ProgressDialog progress =null;
    private Handler handler;
    private HandlerThread handlerThread;
    private ImageView shareButton;

    private RecyclerView mRecyclerView;
    private HorizontalListAdapter mHrAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Bitmap> myStylesBmList = new ArrayList<Bitmap>();

    private int mSelectedStyle = 0;

    private static final int REQUEST_STORAGE_PERMISSION = 1;

    final int maxMemory = (int) (Runtime.getRuntime().maxMemory());

    // Use 1/8th of the available memory for this memory cache.
    final int cacheSize = maxMemory / 8;

    private LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    private SeekBar mSeekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_show_image);

        mPreviewImage = (ImageView) findViewById(R.id.image_preview);
        mOriginalImage = (ImageView) findViewById(R.id.image_orig);

        getPreview();

        MY_DIVISOR = 1;//mImgBitmap.getHeight()/8;

        Palette.from(mOrigBitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p) {
                mPreviewImage.setBackgroundColor(p.getDominantColor(Color.BLACK));
                mOriginalImage.setBackgroundColor(p.getDominantColor(Color.BLACK));
            }
        });
        intValues = new int[mImgBitmap.getWidth() * mImgBitmap.getHeight()];
        floatValues = new float[mImgBitmap.getWidth() * mImgBitmap.getHeight() * 3];
        mOriginalImage.setImageBitmap(mOrigBitmap);
        mPreviewImage.setImageBitmap(mImgBitmap);

        shareButton = findViewById(R.id.share_button);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(ShowImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    requestStoragePermission();
                    return;
                }
                if(mImgBitmap!=null) {
                    try{
                        Bitmap newBitmap = Bitmap.createBitmap(mImgBitmap.getWidth(), mImgBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                        // create a canvas where we can draw on
                        Canvas canvas = new Canvas(newBitmap);
                        // create a paint instance with alpha
                        canvas.drawBitmap(mOrigBitmap,0,0,null);
                        Paint alphaPaint = new Paint();
                        alphaPaint.setAlpha(mSeekBar.getProgress()*255/100);
                        // now lets draw using alphaPaint instance
                        canvas.drawBitmap(mImgBitmap, 0, 0, alphaPaint);

                        String path = MediaStore.Images.Media.insertImage(ShowImageActivity.this.getContentResolver(), newBitmap, "Title", null);
                        final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                        intent.setType("image/png");
                        startActivity(intent);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        Toast.makeText(ShowImageActivity.this,"Error occurred while trying to share",Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
        loadStyleBitmaps();
        mRecyclerView = (RecyclerView) findViewById(R.id.my_rec_vw);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mHrAdapter = new HorizontalListAdapter(myStylesBmList);
        mRecyclerView.setAdapter(mHrAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(),mRecyclerView,new RecyclerItemClickListener.OnItemClickListener(){

            @Override
            public void onItemClick(View view, int position) {
                mSelectedStyle = position;
                progress = new ProgressDialog(ShowImageActivity.this);
                progress.setTitle("Loading");
                progress.setMessage("Applying your awesome style! Please wait!");
                progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                progress.show();
                runInBackground(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    stylizeImage();
                                }
                                catch(Exception e){
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),"Oops! Some error occurred!",Toast.LENGTH_SHORT).show();
                                            if(progress!=null){
                                                progress.dismiss();
                                            }
                                        }
                                    });
                                }
                            }
                        });
            }

            @Override
            public void onLongItemClick(View view, int position) {
            }
        }));
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setProgress(100);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mPreviewImage.setAlpha(seekBar.getProgress()/100.0f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNavigateUp();
            }
        });

        int statusBarHeight = getStatusBarHeight(getApplicationContext());
        ImageView statusBarBg = findViewById(R.id.statusBarBg);
        statusBarBg.getLayoutParams().height = statusBarHeight;
        statusBarBg.requestLayout();

        Point statusBarSize = getNavigationBarSize(getApplicationContext());

        int navigationBarHeight = statusBarSize!=null?statusBarSize.y:0;
        ImageView navigationBarBg = findViewById(R.id.navigartionBarBg);
        navigationBarBg.getLayoutParams().height = navigationBarHeight;
        navigationBarBg.requestLayout();

        final RelativeLayout relativeLayout= findViewById(R.id.relativeLayout);
        RelativeLayout.LayoutParams relativeLayoutLayoutParams = (RelativeLayout.LayoutParams) relativeLayout.getLayoutParams();
        Log.i(ShowImageActivity.class.getName(),String.valueOf(statusBarHeight));
        relativeLayoutLayoutParams.setMargins(0,statusBarHeight,0,0);
        relativeLayout.setLayoutParams(relativeLayoutLayoutParams);

        mPreviewImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(relativeLayout!=null){
                    if(relativeLayout.getVisibility()==View.VISIBLE) relativeLayout.setVisibility(View.GONE);
                    else relativeLayout.setVisibility(View.VISIBLE);
                }
                if(mRecyclerView!=null){
                    if(mRecyclerView.getVisibility()==View.VISIBLE) mRecyclerView.setVisibility(View.GONE);
                    else mRecyclerView.setVisibility(View.VISIBLE);
                }
                if(mSeekBar!=null){
                    if(mSeekBar.getVisibility()==View.VISIBLE) mSeekBar.setVisibility(View.GONE);
                    else mSeekBar.setVisibility(View.VISIBLE);
                }
            }
        });
        RelativeLayout.LayoutParams recyclerLayoutParams = (RelativeLayout.LayoutParams)mRecyclerView.getLayoutParams();
        recyclerLayoutParams.setMargins(0,0,0,navigationBarHeight);
        mRecyclerView.setLayoutParams(recyclerLayoutParams);
    }

    private void loadStyleBitmaps(){
        for(int i=0;i<NUM_STYLES;i++){
            try{
                myStylesBmList.add(i,BitmapFactory.decodeStream(getAssets().open("thumbnails/style"+i+".jpg")));
            }
            catch(IOException e){
                e.printStackTrace();
                Toast.makeText(ShowImageActivity.this,"Oops! Some error occurred!",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public synchronized void onResume(){
        super.onResume();
        if(inferenceInterface==null) {
            handlerThread = new HandlerThread("inference");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
            if (progress != null) progress.cancel();
            progress = new ProgressDialog(ShowImageActivity.this);
            progress.setTitle("Loading");
            progress.setMessage("Wait while loading model...");
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
            runInBackground(new Runnable() {
                @Override
                public void run() {
                    inferenceInterface = new TensorFlowInferenceInterface(getAssets(), MODEL_FILE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.cancel();
                        }
                    });
                }
            });
        }
        //mOriginalImage.setImageBitmap((getPreview()));
        //mPreviewImage.setImageBitmap(getPreview());
    }
    private Bitmap getPreview() {

        mImgBitmap = ResultHolder.getImage();
        Log.i(ShowImageActivity.class.getName(),String.valueOf(ResultHolder.getTimeToCallback()));
        if(mImgBitmap==null){
            Toast.makeText(getApplicationContext(),"Some error occurred!",Toast.LENGTH_SHORT).show();
            finish();
        }
        int photoW = mImgBitmap.getWidth();
        int photoH = mImgBitmap.getHeight();
        int targetW;
        int targetH;
        if(photoW>photoH){
            targetH = getWindowManager().getDefaultDisplay().getWidth();
            if(targetH>photoH) {
                mOrigBitmap = ResultHolder.getImage();
            }
            else{
                targetW = targetH*photoW / photoH;
                mImgBitmap = Bitmap.createScaledBitmap(mImgBitmap, targetW, targetH, false);
                mOrigBitmap = Bitmap.createScaledBitmap(mImgBitmap, targetW, targetH, false);
            }
        }
        else {
            targetW = getWindowManager().getDefaultDisplay().getWidth();
            if(targetW>photoW){
                mOrigBitmap = ResultHolder.getImage();
            }
            else {
                targetH = targetW * photoH / photoW;
                mImgBitmap = Bitmap.createScaledBitmap(mImgBitmap, targetW, targetH, false);
                mOrigBitmap = Bitmap.createScaledBitmap(mImgBitmap, targetW, targetH, false);
            }
        }
        Log.d(TAG,mImgBitmap.getHeight()+" :height - width: "+mImgBitmap.getWidth());
        return mImgBitmap;
    }

    public boolean isDebug() {
        return debug;
    }

    private void stylizeImage() {
        if(bitmapCache.get("style_"+String.valueOf(mSelectedStyle))==null) {
            ActivityManager actManager = (ActivityManager) getApplication().getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);

            mImgBitmap = Bitmap.createBitmap(mOrigBitmap);
            for (int i = 0; i < NUM_STYLES; i++) {
                if (i == mSelectedStyle) {
                    styleVals[i] = 1.0f;
                } else styleVals[i] = 0.0f;
            }
            mImgBitmap.getPixels(intValues, 0, mImgBitmap.getWidth(), 0, 0, mImgBitmap.getWidth(), mImgBitmap.getHeight());

            /*for (int i = 0; i < intValues.length; ++i) {
                final int val = intValues[i];
                floatValues[i * 3] = ((val >> 16) & 0xFF) / 255.0f;
                floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
                floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;
            }*/

            for(int i=0;i<MY_DIVISOR;i++) {
                float[] floatValuesInput = new float[floatValues.length/MY_DIVISOR];
                int myArrayLength = intValues.length/MY_DIVISOR;
                for(int x=0;x < myArrayLength;++x){
                    final int myPos = x+i*myArrayLength;
                    final int val = intValues[myPos];
                    floatValuesInput[x * 3] = ((val >> 16) & 0xFF) / 255.0f;
                    floatValuesInput[x * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
                    floatValuesInput[x * 3 + 2] = (val & 0xFF) / 255.0f;
                }
                Log.i(ShowImageActivity.class.getName(),"Sending following data to tensorflow : floarValuesInput length : " + floatValuesInput.length+" image bitmap height :" + mImgBitmap.getHeight() + " image bitmap width : " + mImgBitmap.getWidth());
                // Copy the input data into TensorFlow.
                inferenceInterface.feed(
                        INPUT_NODE, floatValuesInput, 1, mImgBitmap.getHeight()/MY_DIVISOR, mImgBitmap.getWidth(), 3);
                inferenceInterface.feed(STYLE_NODE, styleVals, NUM_STYLES);
                inferenceInterface.run(new String[]{OUTPUT_NODE}, isDebug());
                float[] floatValuesOutput = new float[floatValues.length/MY_DIVISOR];
                //floatValuesOutput  = new float[mImgBitmap.getWidth() * (mImgBitmap.getHeight() + 10) * 3];//add a little buffer to the float array because tensorflow sometimes returns larger images than what is given as input
                inferenceInterface.fetch(OUTPUT_NODE, floatValuesOutput);

                for (int j = 0; j < myArrayLength; ++j) {
                    intValues[j+i*myArrayLength] =
                            0xFF000000
                                    | (((int) (floatValuesOutput [(j) * 3] * 255)) << 16)
                                    | (((int) (floatValuesOutput [(j) * 3 + 1] * 255)) << 8)
                                    | ((int) (floatValuesOutput [(j) * 3 + 2] * 255));
                }
                //floatValues = new float[mImgBitmap.getWidth() * (mImgBitmap.getHeight()) * 3];
                mImgBitmap.setPixels(intValues, 0, mImgBitmap.getWidth(), 0, 0, mImgBitmap.getWidth(), mImgBitmap.getHeight());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPreviewImage.setImageBitmap(mImgBitmap);
                    }
                });
            }
        }
        else{
            mImgBitmap = bitmapCache.get("style_"+String.valueOf(mSelectedStyle));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mPreviewImage!=null){
                    mPreviewImage.setImageBitmap(mImgBitmap);
                    bitmapCache.put("style_"+String.valueOf(mSelectedStyle),mImgBitmap);
                    if(progress!=null){
                        progress.dismiss();
                    }
                }
            }
        });
    }
    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(ShowImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(ShowImageActivity.this,"Write permission required to share",Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_STORAGE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Camera2BasicFragment.ErrorDialog.newInstance(getString(R.string.request_permission_storage)).show(getFragmentManager(),"dialog");
            }
            else{
                shareButton.performClick();
            }
        } else {
            shareButton.performClick();
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }
}
