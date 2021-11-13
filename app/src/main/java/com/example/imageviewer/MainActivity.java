package com.example.imageviewer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.example.imageviewer.Editor.Image;
import com.example.imageviewer.Editor.Mask;

public class MainActivity extends AppCompatActivity{
    final double[][] matrixBlur = {
            {0, 0.2, 0},
            {0.2, 0.2, 0.2},
            {0, 0.2, 0}
    };
    Mask maskBlur = new Mask(matrixBlur);

    private final double[][] maskBlur7x7 = {
            {1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f},
            {1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f},
            {1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f},
            {1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f},
            {1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f},
            {1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f},
            {1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f, 1/49f},
    };

    final double[][] matrixSharpen = {
            {-1, -1, -1},
            {-1, 9, -1},
            {-1, -1, -1}
    };
    Mask maskSharpen = new Mask(matrixSharpen);

    private final double[][] gaussRestoreMask3x3 = {
            {0.125, 0.125, 0.125},
            {0.125, 0, 0.125},
            {0.125, 0.125, 0.125}
    };
    private final double[][] matrixGaussRestore5x5 = {
            {0.0417, 0.0417, 0.0417, 0.0417, 0.0417},
            {0.0417, 0.0417, 0.0417, 0.0417, 0.0417},
               {0.0417, 0.0417, 0, 0.0417, 0.0417},
            {0.0417, 0.0417, 0.0417, 0.0417, 0.0417},
            {0.0417, 0.0417, 0.0417, 0.0417, 0.0417},
    };
    Mask maskGaussRestore5x5 = new Mask(matrixGaussRestore5x5);


    int REQUEST_CODE = 100;

    private LinearLayout actionsLayout;
    private OutputStream outputStream;

    private ImageView imageView1;
    private Bitmap bitmap1;
    private Image image1;

    private ImageView imageView2;
    private Bitmap bitmap2;
    private Image image2;

    private EditText thresholdInput;
    private EditText maskSizeInput;
    private EditText amountOfNotEmptyInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView1 = findViewById(R.id.startImage);
        imageView2 = findViewById(R.id.changedImage);
        actionsLayout = findViewById(R.id.actionsLayout);

        thresholdInput = findViewById(R.id.editTextNumber2);
        maskSizeInput = findViewById(R.id.editTextNumber3);
        amountOfNotEmptyInput = findViewById(R.id.editTextNumber4);
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage();
            } else {
                Toast.makeText(MainActivity.this, "Please provide the required permission!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImage() {
        File dir = new File(Environment.getDataDirectory()+"/"+"recoveredImage");
        dir.mkdirs();

        if (dir.isDirectory()) {
            File file = new File(dir, "recoveredImage"+System.currentTimeMillis()+".jpeg");
            try {
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Bitmap bitmap = ((BitmapDrawable) imageView2.getDrawable()).getBitmap();

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Toast.makeText(MainActivity.this, "Sucsessfully saved", Toast.LENGTH_SHORT).show();

            try {
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(MainActivity.this, "Field to create dir", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (data == null)
            {
                return;
            }
            Uri uri = data.getData();
            imageView1.setImageURI(uri);
            bitmap1 = ((BitmapDrawable) imageView1.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
            image1 = new Image(bitmap1);

            imageView2.setImageBitmap(bitmap1);
            bitmap2 = ((BitmapDrawable) imageView1.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
            image2 = new Image(bitmap2);

            for (int i = 0; i < actionsLayout.getChildCount(); i++) {
                actionsLayout.getChildAt(i).setEnabled(true);
            }

            imageView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        saveImage();
                    } else {
                        askPermission();
                    }
                }
            });
        }
    }

    public void openFileChooser(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        int requestCode = 1;
        startActivityForResult(intent, requestCode);
    }

    private boolean isEmpty(EditText myEditText) {
        return myEditText.getText().toString().trim().length() == 0;
    }

    public void makeBrighter(View view) {
        image2.changeBrightness(1.1);
        imageView2.setImageBitmap(image2.getBitmap());
    }

    public void makeDarker(View view) {
        image2.changeBrightness(0.9);
        imageView2.setImageBitmap(image2.getBitmap());
    }

    public void makeNegative(View view) {
        image2.negative();
        imageView2.setImageBitmap(image2.getBitmap());
    }

    public void makeBlurred(View view) {
        image2.applyMask(maskBlur);
        imageView2.setImageBitmap(image2.getBitmap());
    }

    public void makeSharpen(View view) {
        image2.applyMask(maskSharpen);
        imageView2.setImageBitmap(image2.getBitmap());
    }

    public void makeAutoContrast(View view) {
        image2.autoContrast();
        imageView2.setImageBitmap(image2.getBitmap());
    }

    public void useGaussFilter(View view) {
        image2.applyMask(maskGaussRestore5x5);
        imageView2.setImageBitmap(image2.getBitmap());
    }

    public void useMedianFilter(View view) {
        int threshold = Integer.parseInt(thresholdInput.getText().toString());

        image2.useMedianFilter(threshold);
        imageView2.setImageBitmap(image2.getBitmap());
    }

    public void useAdaptiveGauss(View view) {
        int threshold;
        int size;
        int amountOfNotEmptyPixelsThreshold;

        if (isEmpty(thresholdInput) || isEmpty(maskSizeInput) || isEmpty(amountOfNotEmptyInput)) {
            Toast.makeText(this, "Please fill depended fields", Toast.LENGTH_SHORT).show();
        } else {
            threshold = Integer.parseInt(thresholdInput.getText().toString());
            size = Integer.parseInt(maskSizeInput.getText().toString());
            amountOfNotEmptyPixelsThreshold = Integer.parseInt(amountOfNotEmptyInput.getText().toString());

            image2.adaptiveGauss(threshold, size, amountOfNotEmptyPixelsThreshold);
            imageView2.setImageBitmap(image2.getBitmap());
        }
    }

    public void highlightVisibleCracks(View view) {
        int threshold = Integer.parseInt(thresholdInput.getText().toString());

        image2.highlightVisibleCracks(threshold);
        imageView2.setImageBitmap(image2.getBitmap());
    }
}