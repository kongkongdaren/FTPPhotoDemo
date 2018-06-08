package com.wen.asyl.ftpphotodemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.wen.asyl.util.FileTool;
import com.wen.asyl.util.NetWorkUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ImageView ivPhoto;
   private Handler handler=new Handler(){
       @Override
       public void handleMessage(Message msg) {
           super.handleMessage(msg);
           switch (msg.what){
               case 1:
                   Toast.makeText(MainActivity.this, "上传成功！", Toast.LENGTH_SHORT).show();
                   break;
               case 2:
                   Toast.makeText(MainActivity.this, "上传失败！", Toast.LENGTH_SHORT).show();
                   break;
           }
       }
   };
    private String creatTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gainCurrenTime();
        ivPhoto = (ImageView) findViewById(R.id.iv_photo);
    }
    private void gainCurrenTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        creatTime = formatter.format(curDate);
    }
    public void selectOnclick(View view){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4);
            } else {
                openAlbum();
            }
        }

    }

    private void openAlbum() {
        // 使用意图直接调用手机相册
        Intent intentPhoto = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // 打开手机相册,设置请求码
        startActivityForResult(intentPhoto, 1);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 4:
                if(grantResults.length>0 &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(this, "请允许读取相册！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
    public void updateOnclick(View view){
                        File filePhoto = new File(Environment.getExternalStorageDirectory(),"upload");
                            File[] photoAllfiles = filePhoto.listFiles();
                        if (photoAllfiles!=null) {
                            if (photoAllfiles.length == 0) {
                                Toast.makeText(MainActivity.this, "没有图片要上传", Toast.LENGTH_SHORT).show();
                            } else {
                                for (final File photoFile : photoAllfiles) {
                                    if (NetWorkUtil.isNetworkAvailable(MainActivity.this)) {
                                        new Thread() {
                                            @Override
                                            public void run() {
                                                aboutTakePhotoUp(photoFile);
                                            }
                                        }.start();
                                    } else {
                                        Toast.makeText(MainActivity.this, "对不起，没有网络！", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }else{
                            Toast.makeText(MainActivity.this, "没有图片要上传！", Toast.LENGTH_SHORT).show();
                        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1){
            if (data != null) {
                resizeImage(data.getData());
            }
        }else if (requestCode==2){
            if (data != null) {
                showResizeImage(data);
            }
        }


    }

    private void showResizeImage(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            String name = creatTime + ".jpg";
            Bundle bundle = data.getExtras();
            // 获取相机返回的数据，并转换为Bitmap图片格式
            Bitmap bitmap = (Bitmap) bundle.get("data");
            FileOutputStream b = null;
            File file = new File(getAlbumStorageDir("upload"), name);
            try {
                b = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (b != null) {
                        b.flush();
                        b.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 将图片显示在ImageView里
            ivPhoto.setImageBitmap(bitmap);
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            MainActivity.this.sendBroadcast(mediaScanIntent);
        }
    }

    public void resizeImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 2);
    }
    private void aboutTakePhotoUp(File photoFile) {
        try {
            FileInputStream in = new FileInputStream(photoFile);
            //将下面的信息换成自己需要的即可
            boolean flag = FileTool.uploadFile("FTP服务器hostname", 21,"登录名", "登录密码", "要传入的文件夹名字(若服务器没有这个文件夹，可以自动建文件夹，无需手动建)", photoFile.getName(), in);
            if (flag == true) {
                handler.sendEmptyMessage(1);
            } else {
                handler.sendEmptyMessage(2);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStorageDirectory(), albumName);
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }
}
