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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.example.imageviewer.Editor.Image;
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

    private ImageView imageView1;
    private Bitmap bitmap1;
    private Image image1;

    private ImageView imageView2;
    private Bitmap bitmap2;
    private Image image2;
    private Image previousImage2;

    private EditText edgeMaskSizeInput;

    private Switch blurEdgesSwitch;

    private Button autocontrastBtn;
    private Button hybridFilterBtn;
    private Slider slider;

    private ImageView brightnessTreshholdFullness;
    private ImageView maskSizeNumberCentral;

    private ImageView brightnessCursor;
    private ImageView maskSizeCursor;

    private int brightnessThreshold = 230;
    private int fullnestPercent = 40;
    private int maskSize = 21;
    private int centralOffset = 3;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slider = findViewById(R.id.brightnessSlider);
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

        brightnessTreshholdFullness = findViewById(R.id.brightnessTreshholdFullness);
        brightnessCursor = findViewById(R.id.brightnessCursor);
        brightnessTreshholdFullness.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                double x = motionEvent.getX();
                double y = motionEvent.getY();
                int width = brightnessTreshholdFullness.getWidth();
                int height = brightnessTreshholdFullness.getHeight();
                int cursorWidth = brightnessCursor.getWidth();
                int cursorHeight = brightnessCursor.getHeight();

//                Log.d("my", "x" + x + "\n" + "y" + y);

                int[] cursorPosition = checkCursorMove(x, y, width, height, cursorWidth, cursorHeight);
                brightnessCursor.setX(cursorPosition[0]);
                brightnessCursor.setY(cursorPosition[1]);

                if (x < 0) {
                    x = 0;
                } else if (x > width) {
                    x = width;
                }

                if (y < 0) {
                    y = 0;
                } else if (y > width) {
                    y = height;
                }

                // I=x/width*(Imax-Imin) + Imin
                brightnessThreshold = (int) ((x) / width * (255 - 200) + 200);
                // Log.d("my", String.valueOf(brightnessThreshold));

                // I=(height-y)/height*(Imax-Imin) + Imin
                fullnestPercent = (int) ((height - y) / height * (95 - 3) + 3);
//                 Log.d("my", String.valueOf(fullnestPercent));

                int numberOfNotEmpty = (int) (Math.pow(maskSize, 2) * fullnestPercent) / 100;
                image2.highlightVisibleCracks(brightnessThreshold, maskSize, numberOfNotEmpty);
                imageView2.setImageBitmap(image2.getBitmap());

                return true;
            }
        });

        maskSizeNumberCentral = findViewById(R.id.maskSizeNumberCentral);
        maskSizeCursor = findViewById(R.id.maskSizeCursor);
        maskSizeNumberCentral.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                double x = motionEvent.getX();
                double y = motionEvent.getY();
                int width = maskSizeNumberCentral.getWidth();
                int height = maskSizeNumberCentral.getHeight();
                int cursorWidth = maskSizeCursor.getWidth();
                int cursorHeight = maskSizeCursor.getHeight();

//                Log.d("my", "x" + x + "\n" + "y" + y);

                int[] cursorPosition = checkCursorMove(x, y, width, height, cursorWidth, cursorHeight);
                maskSizeCursor.setX((float) (cursorPosition[0] + maskSizeNumberCentral.getX() - cursorHeight * 0.75));
                maskSizeCursor.setY((float) (cursorPosition[1] + maskSizeNumberCentral.getY() - cursorHeight * 0.75));

                if (x < 0) {
                    x = 0;
                } else if (x > width) {
                    x = width;
                }

                if (y < 0) {
                    y = 0;
                } else if (y > width) {
                    y = height;
                }

                // I=x/width*(Imax-Imin) + Imin
                maskSize = (int) ((x) / width * (71 - 3) + 3);
                if (maskSize % 2 == 0) {
                    maskSize += 1;
                }
                // Log.d("my", String.valueOf(maskSize));


                // I=(height-y)/height*(Imax-Imin) + Imin
                centralOffset = (int) ((height - y) / height * (21 - 1) + 1);
                // Log.d("my", String.valueOf(centralOffset));

                int numberOfNotEmpty = (int) (Math.pow(maskSize, 2) * fullnestPercent) / 100;
                image2.highlightVisibleCracks(brightnessThreshold, maskSize, numberOfNotEmpty);
                imageView2.setImageBitmap(image2.getBitmap());
                return true;
            }
        });

        autocontrastBtn = findViewById(R.id.autoContrastBtn);
        hybridFilterBtn = findViewById(R.id.hybridFilter);

        imageView1 = findViewById(R.id.startImage);
        imageView2 = findViewById(R.id.changedImage);

        edgeMaskSizeInput = findViewById(R.id.edgesMaskSize);

        blurEdgesSwitch = findViewById(R.id.blurEdgesSwitch);
    }

    private int[] checkCursorMove(double x, double y, int width, int height, int cursorWidth, int cursorHeight) {
        int cursorX;
        int cursorY;

        if (x - cursorWidth / 4 < 0) {
            cursorX = cursorWidth / 4;

        } else if (x - cursorWidth / 4 > width) {
            cursorX = width + cursorWidth / 4;
        } else {
            cursorX = (int) x;
        }

        if (y - cursorHeight / 4 < 0) {
            cursorY = cursorHeight / 4;

        } else if (y - cursorHeight / 4 > height) {
            cursorY = height + cursorHeight / 4;
        } else {
            cursorY = (int) y;
        }

        return new int[] {cursorX, cursorY};
    }

    private int[] getSizeOfView(View view) {
        final int[] size = new int[2];

        final ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                size[0] = view.getHeight();
                size[1] = view.getWidth();
                observer.removeGlobalOnLayoutListener(this);
            }
        });

        return size;
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
            previousImage2 = image2;

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
        int edgeMaskSize = 3;
        boolean blurEdges = blurEdgesSwitch.isChecked();

        int numberOfNotEmpty = (int) (Math.pow(maskSize, 2) * fullnestPercent) / 100;
        image2.hybridFilter(brightnessThreshold, maskSize, numberOfNotEmpty, centralOffset, blurEdges, false, edgeMaskSize);
        imageView2.setImageBitmap(image2.getBitmap());
    }

    public void highlightVisibleCracks(View view) {
        int numberOfNotEmpty = (int) (Math.pow(maskSize, 2) * fullnestPercent) / 100;

        image2.highlightVisibleCracks(brightnessThreshold, maskSize, numberOfNotEmpty);
        imageView2.setImageBitmap(image2.getBitmap());
    }

    public void highlightVisibleEdges(View view) {
        image2.highLightPixels(image2.getEdges(brightnessThreshold, 3));
        imageView2.setImageBitmap(image2.getBitmap());
    }
}