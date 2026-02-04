package com.example.art_book;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.art_book.databinding.ActivityArtBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {

    private ActivityArtBinding binding;
    Bitmap selectedImage;
    SQLiteDatabase database;
    ActivityResultLauncher<Intent> activityResultLauncher;

    ActivityResultLauncher<String> permissionLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();
        database=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

        applyEdgeToEdgePadding(view);
        ViewCompat.requestApplyInsets(view);
    }

    private void applyEdgeToEdgePadding(View view) {        //Applies WindowInsets (status/nav bars & notch) as padding to prevent UI overlap
        final int pL = view.getPaddingLeft();
        final int pT = view.getPaddingTop();
        final int pR = view.getPaddingRight();
        final int pB = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets sys = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );

            v.setPadding(
                    pL + sys.left,
                    pT + sys.top,
                    pR + sys.right,
                    pB + sys.bottom
            );
            return insets;
        });
    }

    public void choosePhoto(View view){     //Checks the required gallery permission (API 33+ uses READ_MEDIA_IMAGES, older uses READ_EXTERNAL_STORAGE) and,
                                            //if granted, launches the gallery picker so the user can select an image
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();
                }
                else{
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            }
            else{
                Intent intentToGallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                }
                else{
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
            else{
                Intent intentToGallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }
    }

    public void save(View view){
        String name=binding.editTextArtName.getText().toString();
        String artistName=binding.editTextArtistName.getText().toString();
        String year=binding.editTextYear.getText().toString();
        Bitmap smallImage= makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.JPEG,80,outputStream);
        byte[] byteArray= outputStream.toByteArray();

        try{
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR,paintername VARCHAR,year INTEGER,image BLOB)");
            String sqlString= "INSERT INTO arts (artname,paintername,year,image) VALUES(?,?,?,?)";
            SQLiteStatement sqLiteStatement=database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,artistName);
            sqLiteStatement.bindLong(3,Long.parseLong(year));
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();
        }catch (Exception e){
            e.printStackTrace();
        }

        Intent intent=new Intent(ArtActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public Bitmap makeSmallerImage(Bitmap image,int maximumSize){       //Resized and converted images to Bitmap
        int width=image.getWidth();
        int height=image.getHeight();

        float bitmapRatio= (float) width/(float) height;

        if(bitmapRatio > 1){
            width=maximumSize;
            height=(int) (width/bitmapRatio);
        }else{
            height=maximumSize;
            width=(int) (height*bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width,height,true);

    }

    private void registerLauncher(){        //Registers the launchers used in this Activity for image picking and permission requests

        //Handles the gallery result: gets the selected image URI, decodes it (ImageDecoder on API 28+, MediaStore on older), and displays it in the ImageView
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if(o.getResultCode() == RESULT_OK){
                    Intent intent = o.getData();
                    if(intent != null){
                        Uri imageData= intent.getData();
                        try {
                            if(Build.VERSION.SDK_INT>= 28){
                                ImageDecoder.Source source= ImageDecoder.createSource(ArtActivity.this.getContentResolver(),imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);
                            }else{
                                selectedImage= MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        //Handles the permission result: if granted, launches the gallery picker; if denied, shows a "permission needed" message
        permissionLauncher= registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if(o){
                    Intent intentToGallery= new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }
                else{
                    Toast.makeText(ArtActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}