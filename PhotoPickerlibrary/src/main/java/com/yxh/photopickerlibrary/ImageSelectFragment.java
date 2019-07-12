package com.yxh.photopickerlibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by xiaohui yu on 7/21/2015.
 * This class is used to pick a picture from Camera or Local storage.
 */
public abstract class ImageSelectFragment extends Fragment {

    public abstract void displayImage(Bitmap bmp);

    public abstract void savedImage(String imagePath);

    private final static int REQ_CODE_SELECT_CAMERA = 100;
    private final static int REQ_CODE_CROP_IMAGE = 101;
    private final static int REQ_CODE_SELECT_LOCAL_IMAGE = 102;
    private final static int REQ_CODE_REQUEST_CAMERA_PERMISION = 10;

    private final static int MENU_IMAGE_FROM_CAMERA = 0;
    private final static int MENU_IMAGE_FROM_LOCAL_STORAGE = 1;

    private final static String MENU_TEXT_FROM_CAMERA = "Take a photo";
    private final static String MENU_TEXT_FROM_LOCAL_STORAGE = "Select a photo";

    private static final String DATA_CACHE_DIR = ".cache_0x0_images";

    private final static int MAX_IMAGE_WIDTH = 1000;

    private String TARGET_IMG_PATH;
    private String ORIGINAL_IMG_PATH;

    private int mCropWidth = 1000;
    private int mCropHeight = 1000;

    private boolean mEnableCrop = true;

    private Context mContext;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = this.getActivity();

        TARGET_IMG_PATH = createCacheDirs(mContext, DATA_CACHE_DIR) + "/target_image.jpg";
        ORIGINAL_IMG_PATH = createCacheDirs(mContext, DATA_CACHE_DIR) + "/original_image.jpg";

//        SmabDebug.log(TARGET_IMG_PATH);
//        SmabDebug.log(ORIGINAL_IMG_PATH);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public void setCropSize(int width, int height) {
        mCropWidth = width;
        mCropHeight = height;
    }

    public void getImageFromCamera() {
        takePictureBySysCall(ORIGINAL_IMG_PATH);
    }

    public void getImageFromLocalStorage() {
        selectPictureFromLocal();
    }

    public void enableCrop(boolean enable) {
        mEnableCrop = enable;
    }

    public void takePictureBySysCall(String imgFileName) {
        File imageFile = new File(imgFileName);
        Uri imageFileUri = Uri.fromFile(imageFile);

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);

        i.putExtra("camerasensortype", 2); // front camera
        //		i.putExtra("autofocus", true);
        //		i.putExtra("fullScreen", false);
        //		i.putExtra("showActionIcons", false);

        startActivityForResult(i, REQ_CODE_SELECT_CAMERA);
    }

    public void selectPictureFromLocal() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQ_CODE_SELECT_LOCAL_IMAGE);
    }

    public void cutPictureBySysCall(String inFilePath, String outFilePath, int width, int height) {
        final Intent intent = new Intent("com.android.camera.action.CROP");

        File mFile = new File(inFilePath);
        intent.setDataAndType(Uri.fromFile(mFile), "image/*");
        if (width != 0 && height != 0) {
//            intent.putExtra("outputX", width);
//            intent.putExtra("outputY", height);
//            intent.putExtra("aspectX", width);
//            intent.putExtra("aspectY", height);
            intent.putExtra("scale", true);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(outFilePath)));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        startActivityForResult(intent, REQ_CODE_CROP_IMAGE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
//        menu.setHeaderTitle("Context Menu");
        menu.add(0, MENU_IMAGE_FROM_CAMERA, 0, MENU_TEXT_FROM_CAMERA);
        menu.add(0, MENU_IMAGE_FROM_LOCAL_STORAGE, 0, MENU_TEXT_FROM_LOCAL_STORAGE);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_IMAGE_FROM_CAMERA) {
            requestCameraPermission();

        }
        if (item.getItemId() == MENU_IMAGE_FROM_LOCAL_STORAGE) {
            selectPictureFromLocal();
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            //show error
            return;
        }
        switch (requestCode) {
            case REQ_CODE_SELECT_CAMERA:
                if (mEnableCrop) {
                    cutPictureBySysCall(ORIGINAL_IMG_PATH, TARGET_IMG_PATH, mCropWidth, mCropHeight);
                } else {
                    saveOriginalBmp(ORIGINAL_IMG_PATH);
                }
                break;

            case REQ_CODE_CROP_IMAGE:
                Bitmap bp = BitmapFactory.decodeFile(TARGET_IMG_PATH);
                displayImage(bp);
                Bitmap resized = Bitmap.createScaledBitmap(bp, mCropWidth, (int) (mCropWidth * 1.0 * bp.getHeight() / bp.getWidth()), true);
                saveBmpTofile(resized, TARGET_IMG_PATH, 85);
                savedImage(TARGET_IMG_PATH);
                break;

            case REQ_CODE_SELECT_LOCAL_IMAGE:
                Uri uri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = mContext.getContentResolver().query(uri, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgPath = cursor.getString(columnIndex);
                cursor.close();

                if (mEnableCrop) {
                    cutPictureBySysCall(imgPath, TARGET_IMG_PATH, mCropWidth, mCropHeight);
                } else {
                    saveOriginalBmp(imgPath);
                }

                break;
        }
    }

    private void saveOriginalBmp(String path) {
        Bitmap bp = BitmapFactory.decodeFile(path);
        displayImage(bp);
        Bitmap resized = Bitmap.createScaledBitmap(bp, MAX_IMAGE_WIDTH, (int) (MAX_IMAGE_WIDTH * 1.0 * bp.getHeight() / bp.getWidth()), true);

        saveBmpTofile(resized, path, 100);
        savedImage(path);
    }

    //For Android 6.0 SDK to get permission

    private void requestCameraPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.CAMERA);

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    REQ_CODE_REQUEST_CAMERA_PERMISION
            );

            return;
        }

        takePictureBySysCall(ORIGINAL_IMG_PATH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        SmabDebug.log("Permission Result -> request Code = " + requestCode);

        if (requestCode == REQ_CODE_REQUEST_CAMERA_PERMISION) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    takePictureBySysCall(ORIGINAL_IMG_PATH);

                } else {
                    Toast.makeText(this.getActivity(), "The Camera Permission is needed to take a picture.", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    public static void saveBmpTofile(Bitmap bmp, String filename, int compression) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.JPEG, compression, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String createCacheDirs(Context ctx, String dirs) {
        File mediaFile = new File(ctx.getExternalCacheDir(), dirs);
        if (!mediaFile.exists()) {
            mediaFile.mkdirs();
        }
        return mediaFile.getAbsolutePath();
    }
}
