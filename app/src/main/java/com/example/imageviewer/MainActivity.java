package com.example.imageviewer;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private final double[][] maskBlur = {
            {0, 0.2, 0},
            {0.2, 0.2, 0.2},
            {0, 0.2, 0}
    };
    private final double[][] maskSharpen = {
            {-1, -1, -1},
            {-1, 9, -1},
            {-1, -1, -1}
    };
    private final double[][] gaussRestoreMask3x3 = {
            {0.125, 0.125, 0.125},
            {0.125, 0, 0.125},
            {0.125, 0.125, 0.125}
    };
    private final double[][] gaussRestoreMask5x5 = {
            {0.0417, 0.0417, 0.0417, 0.0417, 0.0417},
            {0.0417, 0.0417, 0.0417, 0.0417, 0.0417},
               {0.0417, 0.0417, 0, 0.0417, 0.0417},
            {0.0417, 0.0417, 0.0417, 0.0417, 0.0417},
            {0.0417, 0.0417, 0.0417, 0.0417, 0.0417},
    };

    int LIMIT = 220;

    private LinearLayout actionsLayout;

    private ImageView imageView1;
    private int[][] argbValues1;
    private Bitmap bitmap1;

    private ImageView imageView2;
    private int[][] argbValues2;
    private Bitmap bitmap2;

    private ImageView imageView3;
    private int[][] argbValues3;
    private Bitmap bitmap3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView1 = findViewById(R.id.startImage);
        imageView2 = findViewById(R.id.changedImage);
        imageView3 = findViewById(R.id.changedImage2);
        actionsLayout = findViewById(R.id.actionsLayout);
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
            argbValues1 = getRgbValuesFromBitmap(bitmap1);

            imageView2.setImageBitmap(bitmap1);
            bitmap2 = ((BitmapDrawable) imageView1.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
            argbValues2 = getRgbValuesFromBitmap(bitmap1);

            imageView3.setImageBitmap(bitmap1);
            bitmap3 = ((BitmapDrawable) imageView1.getDrawable()).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
            argbValues3 = getRgbValuesFromBitmap(bitmap1);


            for (int i = 0; i < actionsLayout.getChildCount(); i++) {
                actionsLayout.getChildAt(i).setEnabled(true);
            }
        }
    }

    public void openFileChooser(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        int requestCode = 1;
        startActivityForResult(intent, requestCode);
    }

    private int[][] getRgbValuesFromBitmap(@NonNull Bitmap bitmap) {
        int[][] argbValues = new int[bitmap.getWidth()][bitmap.getHeight()];
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                int pixel = bitmap.getPixel(i, j);
                argbValues[i][j] = pixel;
            }
        }
        return argbValues;
    }

    public void makeBrighter(View view) {
        for (int i = 0; i < bitmap2.getWidth(); i++) {
            for (int j = 0; j < bitmap2.getHeight(); j++) {
                int newA = Color.alpha(argbValues2[i][j]);
                int newR = Math.min(((int) Math.round(Color.red(argbValues2[i][j]) * 1.1)), 255);
                int newG = Math.min(((int) Math.round(Color.green(argbValues2[i][j]) * 1.1)), 255);
                int newB = Math.min(((int) Math.round(Color.blue(argbValues2[i][j]) * 1.1)), 255);

                argbValues2[i][j] = Color.argb(newA, newR, newG, newB);
                bitmap2.setPixel(i, j, argbValues2[i][j]);
            }
        }

        imageView2.setImageBitmap(bitmap2);
    }

    public void makeDarker(View view) {
        for (int i = 0; i < bitmap2.getWidth(); i++) {
            for (int j = 0; j < bitmap2.getHeight(); j++) {
                int newA = Color.alpha(argbValues2[i][j]);
                int newR = (int) Math.round(Color.red(argbValues2[i][j]) * 0.9);
                int newG = (int) Math.round(Color.green(argbValues2[i][j]) * 0.9);
                int newB = (int) Math.round(Color.blue(argbValues2[i][j]) * 0.9);

                argbValues2[i][j] = Color.argb(newA, newR, newG, newB);
                bitmap2.setPixel(i, j, argbValues2[i][j]);
            }
        }

        imageView2.setImageBitmap(bitmap2);
    }

    public void makeNegative(View view) {
        for (int i = 0; i < bitmap2.getWidth(); i++) {
            for (int j = 0; j < bitmap2.getHeight(); j++) {
                int newA = Color.alpha(argbValues2[i][j]);
                int newR = 256 - Color.red(argbValues2[i][j]);
                int newG = 256 - Color.green(argbValues2[i][j]);
                int newB = 256 - Color.blue(argbValues2[i][j]);


                argbValues2[i][j] = Color.argb(newA, newR, newG, newB);
                bitmap2.setPixel(i, j, argbValues2[i][j]);
            }
        }

        imageView2.setImageBitmap(bitmap2);
    }

    private int applyMask(@ColorInt int[][] argbValues, int x, int y, String channel, double[][] coefficients, int size) {
        int result;
        int summa = 0;
        int lastIndex = Math.round(size / 2);

        switch (channel) {
            case "RED":
                for (int i = -lastIndex; i <= lastIndex; i++) {
                    for (int j = -lastIndex; j <= lastIndex; j++) {
                        summa += coefficients[i + lastIndex][j + lastIndex] * Color.red(argbValues[x + i][y + j]);
                    }
                }
                summa = Math.round(summa);
                result = summa;
                break;
            case "GREEN":
                for (int i = -lastIndex; i <= lastIndex; i++) {
                    for (int j = -lastIndex; j <= lastIndex; j++) {
                        summa +=coefficients[i + lastIndex][j + lastIndex] * Color.green(argbValues[x + i][y + j]);
                    }
                }
                summa = Math.round(summa);
                result = summa;
                break;
            case "BLUE":
                for (int i = -lastIndex; i <= lastIndex; i++) {
                    for (int j = -lastIndex; j <= lastIndex; j++) {
                        summa += coefficients[i + lastIndex][j + lastIndex] * Color.blue(argbValues[x + i][y + j]);
                    }
                }
                summa = Math.round(summa);
                result = summa;
                break;
            default:
                result = Color.BLUE;
                break;
        }

        return result;
    }

    private double[][] getAdaptionMask(@ColorInt int[][] argbValues, int x, int y, int size) {
        int lastIndex = Math.round(size / 2);
        float amountOfNotEmptyPixels = 0;
        double[][] mask = new double[size][size];

        for (int i = -lastIndex; i <= lastIndex; i++) {
            for (int j = -lastIndex; j <= lastIndex; j++) {
                if (Color.red(argbValues[x + i][y + j]) <= LIMIT) {
                    amountOfNotEmptyPixels++;
                }
            }
        }
        float coeff = 1/(amountOfNotEmptyPixels) ;

        for (int i = -lastIndex; i <= lastIndex; i++) {
            for (int j = -lastIndex; j <= lastIndex; j++) {
                if (Color.red(argbValues[x + i][y + j]) <= LIMIT) {
                    mask[i + lastIndex][j + lastIndex] = coeff;
                } else {
                    mask[i + lastIndex][j + lastIndex] = 0;
                }
            }
        }
        return mask;
    }

    public void makeBlurred(View view) {
        int[][] tempArgbValues = new int[bitmap2.getWidth()][bitmap2.getHeight()];
        for (int i = 1; i < bitmap2.getWidth() - 1; i++) {
            for (int j = 1; j < bitmap2.getHeight() - 1; j++) {
                int newA = Color.alpha(argbValues2[i][j]);
                int newR = Math.min(this.applyMask(argbValues2, i, j, "RED", maskBlur, 3), 255);
                newR = Math.max(newR, 0);
                int newB = Math.min(this.applyMask(argbValues2, i, j, "BLUE", maskBlur, 3), 255);
                newB = Math.max(newB, 0);
                int newG = Math.min(this.applyMask(argbValues2, i, j, "GREEN", maskBlur, 3), 255);
                newG = Math.max(newG, 0);


                tempArgbValues[i][j] = Color.argb(newA, newR, newG, newB);
                bitmap2.setPixel(i, j, tempArgbValues[i][j]);
            }
        }
        argbValues2 = tempArgbValues;
        imageView2.setImageBitmap(bitmap2);
    }

    public void makeSharpen(View view) {
        int[][] tempArgbValues = new int[bitmap2.getWidth()][bitmap2.getHeight()];
        for (int i = 1; i < bitmap2.getWidth() - 1; i++) {
            for (int j = 1; j < bitmap2.getHeight() - 1; j++) {
                int newA = Color.alpha(argbValues2[i][j]);
                int newR = Math.min(this.applyMask(argbValues2, i, j, "RED", maskSharpen, 3), 255);
                newR = Math.max(newR, 0);
                int newB = Math.min(this.applyMask(argbValues2, i, j, "BLUE", maskSharpen, 3), 255);
                newB = Math.max(newB, 0);
                int newG = Math.min(this.applyMask(argbValues2, i, j, "GREEN", maskSharpen, 3), 255);
                newG = Math.max(newG, 0);

                tempArgbValues[i][j] = Color.argb(newA, newR, newG, newB);
                bitmap2.setPixel(i, j, tempArgbValues[i][j]);
            }
        }
        argbValues2 = tempArgbValues;
        imageView2.setImageBitmap(bitmap2);
    }

    public void makeDifference(View view) {
        for (int i = 0; i < bitmap2.getWidth(); i++) {
            for (int j = 0; j < bitmap2.getHeight(); j++) {
                int newA = Color.alpha(argbValues2[i][j]);
                int newR = Color.red(argbValues2[i][j] - argbValues1[i][j]);
                int newG = Color.green(argbValues2[i][j] - argbValues1[i][j]);
                int newB = Color.blue(argbValues2[i][j] - argbValues1[i][j]);

                argbValues3[i][j] = Color.argb(newA, newR, newG, newB);
                bitmap3.setPixel(i, j, argbValues2[i][j]);
            }
        }
        imageView3.setImageBitmap(bitmap3);
    }

    private int[] sortFromLToH(int[][] arr) {
        int[] newArr = new int[arr.length * arr[0].length];

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                newArr[(i * arr.length) + j] = Color.red(arr[i][j]);
            }
        }
        Arrays.sort(newArr);
        return newArr;
    }

    public void makeAutoContrast(View view) {
        autoContrast();
        imageView2.setImageBitmap(bitmap2);
    }

    private void autoContrast() {
        int[][] tempArgbValues = new int[bitmap2.getWidth()][bitmap2.getHeight()];
        int[] sortedArgbValues2 = sortFromLToH(argbValues2);
        int min = sortedArgbValues2[0];
        int max = sortedArgbValues2[sortedArgbValues2.length - 1];

        for (int i = 0; i < bitmap2.getWidth(); i++) {
            for (int j = 0; j < bitmap2.getHeight(); j++) {
                int newA = Color.alpha(argbValues2[i][j]);
                int newR = Math.min((Color.red(argbValues2[i][j])-min) * 255 / (max-min), 255);
                newR = Math.max(newR, 0);

                tempArgbValues[i][j] = Color.argb(newA, newR, newR, newR);
                bitmap2.setPixel(i, j, tempArgbValues[i][j]);
            }
        }
        argbValues2 = tempArgbValues;
    }

    public void useGaussFilter(View view) {
        autoContrast();
        int[][] tempArgbValues = new int[bitmap2.getWidth()][bitmap2.getHeight()];
        for (int i = 2; i < bitmap2.getWidth() - 2; i++) {
            for (int j = 2; j < bitmap2.getHeight() - 2; j++) {
                if (Color.red(argbValues2[i][j]) >= 230) {
                    int newA = Color.alpha(argbValues2[i][j]);
                    int newR = Math.min(this.applyMask(argbValues2, i, j, "RED", gaussRestoreMask5x5, 5), 255);
                    newR = Math.max(newR, 0);
                    int newB = Math.min(this.applyMask(argbValues2, i, j, "BLUE", gaussRestoreMask5x5, 5), 255);
                    newB = Math.max(newB, 0);
                    int newG = Math.min(this.applyMask(argbValues2, i, j, "GREEN", gaussRestoreMask5x5, 5), 255);
                    newG = Math.max(newG, 0);

                    tempArgbValues[i][j] = Color.argb(newA, newR, newG, newB);
                    bitmap2.setPixel(i, j, tempArgbValues[i][j]);
                } else {
                    int newA = Color.alpha(argbValues2[i][j]);
                    int newR = Color.red(argbValues2[i][j]);

                    tempArgbValues[i][j] = Color.argb(newA, newR, newR, newR);
                    bitmap2.setPixel(i, j, tempArgbValues[i][j]);
                }
            }
        }
        argbValues2 = tempArgbValues;
        imageView2.setImageBitmap(bitmap2);
    }

    public void adaptiveGauss(View view) {
        autoContrast();
        int[][] tempArgbValues = new int[bitmap2.getWidth()][bitmap2.getHeight()];
        for (int i = 2; i < bitmap2.getWidth() - 2; i++) {
            for (int j = 2; j < bitmap2.getHeight() - 2; j++) {
                if (Color.red(argbValues2[i][j]) >= 230) {
                    double[][] mask = getAdaptionMask(argbValues2, i, j, 5);
                    int newA = Color.alpha(argbValues2[i][j]);
                    int newR = Math.min(this.applyMask(argbValues2, i, j, "RED", mask, 5), 255);
                    newR = Math.max(newR, 0);

                    tempArgbValues[i][j] = Color.argb(newA, newR, newR, newR);
                } else {
                    int newA = Color.alpha(argbValues2[i][j]);
                    int newR = Color.red(argbValues2[i][j]);

                    tempArgbValues[i][j] = Color.argb(newA, newR, newR, newR);
                }
                bitmap2.setPixel(i, j, tempArgbValues[i][j]);
            }
        }
        argbValues2 = tempArgbValues;
        imageView2.setImageBitmap(bitmap2);
    }
}