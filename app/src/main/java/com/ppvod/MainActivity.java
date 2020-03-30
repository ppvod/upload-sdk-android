package com.ppvod;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };


    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        verifyStoragePermissions(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 进入相册 以下是例子：不需要的api可以不写
                PictureSelector.create(MainActivity.this)
                        .openGallery(PictureMimeType.ofAll())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                        .loadImageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                        .isWithVideoImage(true)// 图片和视频是否可以同选
                        //.minSelectNum(1)// 最小选择数量
                        //.minVideoSelectNum(1)// 视频最小选择数量，如果没有单独设置的需求则可以不设置，同用minSelectNum字段
                        .maxVideoSelectNum(1) // 视频最大选择数量，如果没有单独设置的需求则可以不设置，同用maxSelectNum字段
                        .imageSpanCount(4)// 每行显示个数
                        .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                        .selectionMode(PictureConfig.MULTIPLE )// 多选 or 单选
                        .previewVideo(true)// 是否可预览视频
                        .isCamera(true)// 是否显示拍照按钮
                        .compressQuality(80)// 图片压缩后输出质量 0~ 100
                        .synOrAsy(true)//同步false或异步true 压缩 默认同步
                        .cutOutQuality(90)// 裁剪输出质量 默认100
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        .forResult(new OnResultCallbackListener<LocalMedia>() {
                            @Override
                            public void onResult(List<LocalMedia> result) {
                                // 图片选择结果回调
                                // 例如 LocalMedia 里面返回五种path
                                // 1.media.getPath(); 为原图path
                                // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                                // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                                // 4.media.getOriginalPath()); media.isOriginal());为true时此字段才有值
                                // 5.media.getAndroidQToPath();为Android Q版本特有返回的字段，此字段有值就用来做上传使用
                                // 如果同时开启裁剪和压缩，则取压缩路径为准因为是先裁剪后压缩

                                String mPath ="";

                                for (LocalMedia media : result) {

                                    String path = media.getPath();
                                    String androidQToPath = media.getAndroidQToPath();

                                    LogUtil.info( "是否压缩:" + media.isCompressed());
                                    LogUtil.info( "压缩:" + media.getCompressPath());
                                    LogUtil.info( "原图:" + path);
                                    LogUtil.info( "是否裁剪:" + media.isCut());
                                    LogUtil.info( "裁剪:" + media.getCutPath());
                                    LogUtil.info( "是否开启原图:" + media.isOriginal());
                                    LogUtil.info( "原图路径:" + media.getOriginalPath());
                                    LogUtil.info(  "Android Q 特有Path:" + androidQToPath);


                                    if(TextUtils.isEmpty(androidQToPath)){
                                        mPath =path;
                                    }else {
                                        mPath = androidQToPath;
                                    }


                                    String finalMPath = mPath;
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // 标准上传
                                            Uploader up = new Uploader("http://192.168.1.18:2100/uploads/", "v3", 20 * 1024 * 1024);
                                            JobFuture future = up.upload(finalMPath);
                                            try {
                                                Thread.sleep(3000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            System.out.println(future.getState());
                                        }
                                    }).start();

                                }
                    
                            }

                            @Override
                            public void onCancel() {
                                LogUtil.info( "PictureSelector Cancel");
                            }
                        });

            }
        });
        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 标准上传
                        Uploader up = new Uploader("http://192.168.1.18:2100/uploads/", "v3", 20 * 1024 * 1024);
                        JobFuture future = up.upload("/sdcard/Download/somebody.mp4");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println(future.getState());
                    }
                }).start();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
