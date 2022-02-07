package com.example.imageviewer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.example.imageviewer.Editor.Image;
import com.example.imageviewer.Editor.Mask;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

@SuppressLint("UseSwitchCompatOrMaterialCode")
public class MainActivity extends AppCompatActivity {
    final double[][] matrixBlur = {
            {0, 0.2, 0},
            {0.2, 0.2, 0.2},
            {0, 0.2, 0}
    };

    int REQUEST_CODE = 100;

    private Button settingsBtn;

    private ImageView imageView1;
    private Bitmap bitmap1;
    private Image image1;

    private ImageView imageView2;
    private Bitmap bitmap2;
    private Image image2;

    private EditText thresholdInput;
    private EditText maskSizeInput;
    private EditText amountOfNotEmptyInput;
    private EditText edgeMaskSizeInput;
    private EditText numberOfCutPixelsInput;

    private Button showCracksBtn;
    private Button showEdgesBtn;
    private Switch blurEdgesSwitch;
    private Switch blurEdgesBASwitch;

    private Button autocontrastBtn;
    private Button hybridFilterBtn;
    private Slider slider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsBtn = findViewById(R.id.settings_btn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), Settings.class);
                startActivityForResult(myIntent, 0);
            }
        });

        slider = findViewById(R.id.slider);
        slider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                int newValue = (int) value;

                return String.valueOf(newValue);
            }
        });
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                value = (int) value;
                image2.setBrightness(value);
            }
        });

        autocontrastBtn = findViewById(R.id.autoContrastBtn);
        hybridFilterBtn = findViewById(R.id.hybridFilter);

        imageView1 = findViewById(R.id.startImage);
        imageView2 = findViewById(R.id.changedImage);

        thresholdInput = findViewById(R.id.editTextNumber2);
        thresholdInput.setText("230");

        maskSizeInput = findViewById(R.id.editTextNumber3);
        maskSizeInput.setText("21");

        amountOfNotEmptyInput = findViewById(R.id.editTextNumber4);
        amountOfNotEmptyInput.setText("40");

        edgeMaskSizeInput = findViewById(R.id.editTextNumber6);
        numberOfCutPixelsInput = findViewById(R.id.editTextNumber7);

        showCracksBtn = findViewById(R.id.showCracks);
        showEdgesBtn = findViewById(R.id.showEdges);
        blurEdgesSwitch = findViewById(R.id.blurEdgesSwitch);
        blurEdgesBASwitch = findViewById(R.id.bluredgesBASwitch);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void askPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage(image2.getBitmap());
            } else {
                Toast.makeText(MainActivity.this, "Please provide the required permission!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImage(Bitmap bm) {
        MediaStore.Images.Media.insertImage(getContentResolver(), bm,
                "barcodeNumber" + ".jpg Card Image", "barcodeNumber" + ".jpg Card Image");
        Toast.makeText(MainActivity.this, "Result is succesfully saved!", Toast.LENGTH_SHORT).show();
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
            imageView2.setImageBitmap(image2.getBitmap());


            showCracksBtn.setEnabled(true);
            showEdgesBtn.setEnabled(true);
            autocontrastBtn.setEnabled(true);
            hybridFilterBtn.setEnabled(true);

            imageView2.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.R)
                @Override
                public void onClick(View v) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        saveImage(image2.getBitmap());
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

    public void setEnabled(View view) {
        blurEdgesBASwitch.setEnabled(!blurEdgesBASwitch.isEnabled());
        edgeMaskSizeInput.setEnabled(!edgeMaskSizeInput.isEnabled());
    }

    private boolean isEmpty(EditText myEditText) {
        return myEditText.getText().toString().trim().length() == 0;
    }

    public void makeAutoContrast(View view) {
        image2.autoContrast();
        imageView2.setImageBitmap(image2.getBitmap());
    }

    public void useHybridFilter(View view) {
        int threshold;
        int size;
        int amountOfNotEmptyPixelsThreshold;
        int edgeMaskSize = 0;
        int numberOfCutPixels;
        boolean blurEdges = blurEdgesSwitch.isChecked();
        boolean blurEdgesBefore = blurEdgesBASwitch.isChecked();

        if ((isEmpty(thresholdInput) || isEmpty(maskSizeInput) || isEmpty(amountOfNotEmptyInput)) || (blurEdges && (isEmpty(edgeMaskSizeInput) || isEmpty(numberOfCutPixelsInput)))) {
            Toast.makeText(this, "Please fill depended fields", Toast.LENGTH_SHORT).show();
        } else {
            threshold = Integer.parseInt(thresholdInput.getText().toString());
            size = Integer.parseInt(maskSizeInput.getText().toString());
            amountOfNotEmptyPixelsThreshold = Integer.parseInt(amountOfNotEmptyInput.getText().toString());
            if (blurEdges) {
                edgeMaskSize = Integer.parseInt(edgeMaskSizeInput.getText().toString());
            }
            numberOfCutPixels = Integer.parseInt(numberOfCutPixelsInput.getText().toString());

            image2.hybridFilter(threshold, size, amountOfNotEmptyPixelsThreshold,numberOfCutPixels, blurEdges, blurEdgesBefore, edgeMaskSize);
            imageView2.setImageBitmap(image2.getBitmap());
        }
    }

    public void highlightVisibleCracks(View view) {
        int threshold = Integer.parseInt(thresholdInput.getText().toString());
        int amountOfNotEmpty = Integer.parseInt(amountOfNotEmptyInput.getText().toString());
        int maskSize = Integer.parseInt(maskSizeInput.getText().toString());

        image2.highlightVisibleCracks(threshold, maskSize, amountOfNotEmpty);
        imageView2.setImageBitmap(image2.getBitmap());
    }

    public void highlightVisibleEdges(View view) {
        int threshold = Integer.parseInt(thresholdInput.getText().toString());

        if (isEmpty(thresholdInput)) {
            Toast.makeText(this, "Please fill depended fields", Toast.LENGTH_SHORT).show();
        } else {
            image2.highLightPixels(image2.getEdges(threshold, 3));
            imageView2.setImageBitmap(image2.getBitmap());
        }
    }
}