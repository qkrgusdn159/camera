package com.example.david.camera;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_CODE = 10;
    private static final int GALLERY_CODE = 11;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requirePermission();

        Button gallery = findViewById(R.id.call_gallery); // 갤러리 호출
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickUpPicture();
            }
        });

        Button camera_button = findViewById(R.id.camera_button);

        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean camera = ContextCompat.checkSelfPermission
                        (view.getContext(),Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

                boolean write = ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

                if(camera && write){
                    //사진찍은 인텐트 코드 넣기

                    takePicture();

                }else{
                    Toast.makeText(MainActivity.this,"카메라 권한 및 쓰기 권한을 주지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void requirePermission(){
        String [] permissions = new String [] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ArrayList<String> listPermissionsNeeded = new ArrayList<>();

        for(String permission : permissions){

            if(ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_DENIED){
                //권한이 허가가 안됐을 경우 요청할 권한 수집
                listPermissionsNeeded.add(permission);

            }
        }

        if(!listPermissionsNeeded.isEmpty()){
            // 권한 요청하는 부분
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),1);
        }
    }



    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // startActivityForResult 에서 받은 값들 코드에 따라 처리

        if(requestCode == CAMERA_CODE){
            ImageView imageView = findViewById(R.id.userimg);
            imageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));

            galleryAddPic(); // 갤러리에 이미지 저장
        }
        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK){ //resultCode = 갤러리에서 사진을 선택했는지 여부 즉, 갤러리 호출 + 갤러리에서 사진을 선택했을 경우 이미지뷰에 이미지 등록
            Uri uri = data.getData();

            ImageView imageView = findViewById(R.id.userimg);
            imageView.setImageURI(uri);
        }
    }

    private void galleryAddPic() { //갤러리에 사진 저장
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        Toast.makeText(this,"사진이 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    void pickUpPicture(){ // 갤러리에서 사진 불러오기
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType("image/*");
        startActivityForResult(intent,GALLERY_CODE );
        //Toast.makeText(this,"갤러리에서 사진을 가져왔습니다.", Toast.LENGTH_SHORT).show();
    }

    void takePicture(){ // 카메라로 사진 찍기
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File photoFile = createImageFile();
            Uri photoUri = FileProvider.getUriForFile(this,"com.example.david.camera.fileprovider",photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
            startActivityForResult(intent, CAMERA_CODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
