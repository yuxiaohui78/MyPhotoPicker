package com.yxh.photopicker;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.yxh.photopickerlibrary.ImageSelectActivity;


public class MainActivity extends ImageSelectActivity implements View.OnClickListener {

    private ImageView ivSelectedImage;

    private Button btnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivSelectedImage = findViewById(R.id.ivImage);
        btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(this);
        registerForContextMenu(btnMenu);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnMenu){
            btnMenu.showContextMenu();
        }
    }

    @Override
    public void displayImage(Bitmap bmp) {
        ivSelectedImage.setImageBitmap(bmp);
    }

    @Override
    public void savedImage(String imagePath) {
        Toast.makeText(this, "Image Path = " + imagePath, Toast.LENGTH_SHORT).show();
    }
}
