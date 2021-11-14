package com.example.imageviewer.Editor;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class Image {
    private Bitmap bitmap;
    int[][] argbValues;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Image (Bitmap bitmap) {
        this.bitmap = bitmap;
        this.argbValues = getRgbValuesFromBitmap(this.bitmap);
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

    public void changeBrightness(double coefficient) {
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                int newA = Color.alpha(argbValues[i][j]);
                int newR = Math.min(((int) Math.round(Color.red(argbValues[i][j]) * coefficient)), 255);
                int newG = Math.min(((int) Math.round(Color.green(argbValues[i][j]) * coefficient)), 255);
                int newB = Math.min(((int) Math.round(Color.blue(argbValues[i][j]) * coefficient)), 255);

                argbValues[i][j] = Color.argb(newA, newR, newG, newB);
                bitmap.setPixel(i, j, argbValues[i][j]);
            }
        }
    }

    public void negative() {
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                int newA = Color.alpha(argbValues[i][j]);
                int newR = 256 - Color.red(argbValues[i][j]);
                int newG = 256 - Color.green(argbValues[i][j]);
                int newB = 256 - Color.blue(argbValues[i][j]);

                argbValues[i][j] = Color.argb(newA, newR, newG, newB);
                bitmap.setPixel(i, j, argbValues[i][j]);
            }
        }
    }

    public void applyMask(Mask mask) {
        int[][] tempArgbValues = argbValues;
        int offset = mask.getSize() / 2;
        double[][] matrix = mask.getMatrix();

        for (int i = offset; i < bitmap.getWidth() - offset; i++) {
            for (int j = offset; j < bitmap.getHeight() - offset; j++) {

                int newR = 0;

                for (int k = -offset; k <= offset; k++) {
                    for (int l = -offset; l <= offset; l++) {
                        newR += matrix[k + offset][l + offset] * Color.red(argbValues[k + l][k + l]);
                    }
                }

                tempArgbValues[i][j] = Color.argb(255, newR, newR, newR);
                bitmap.setPixel(i, j, tempArgbValues[i][j]);

            }
        }

        argbValues = tempArgbValues;
    }

    public float applyMaskForPixel(Mask mask, int x, int y) {
        float newR = 0;
        int size = mask.getSize();
        double[][] matrix = mask.getMatrix();
        int offset = Math.round(size / 2);

        for (int i = -offset; i <= offset; i++) {
            for (int j = -offset; j <= offset; j++) {
                int old = Color.red(argbValues[x + i][y + j]);
                newR += matrix[i + offset][j + offset] * Color.red(argbValues[x + i][y + j]);
            }
        }

        return newR;
    }

    public void autoContrast() {
        int[][] tempArgbValues = new int[bitmap.getWidth()][bitmap.getHeight()];
        int[] sortedArgbValues2 = sortFromLToH(argbValues);
        int min = sortedArgbValues2[0];
        int max = sortedArgbValues2[sortedArgbValues2.length - 1];

        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                int newA = Color.alpha(argbValues[i][j]);
                int newR = Math.min((Color.red(argbValues[i][j])-min) * 255 / (max-min), 255);
                newR = Math.max(newR, 0);

                tempArgbValues[i][j] = Color.argb(newA, newR, newR, newR);
                bitmap.setPixel(i, j, tempArgbValues[i][j]);
            }
        }
        argbValues = tempArgbValues;
    }

    private double[][] getAdaptionMask(int x, int y, int size, int threshold, int amountOfNotEmptyPixelsThreshold) {
        int lastIndex = size / 2;
        float amountOfNotEmptyPixels = 0;
        double[][] mask = new double[size][size];

        for (int i = -lastIndex; i <= lastIndex; i++) {
            for (int j = -lastIndex; j <= lastIndex; j++) {
                if (Color.red(argbValues[x + i][y + j]) <= threshold) {
                    amountOfNotEmptyPixels++;
                }
            }
        }
        if (amountOfNotEmptyPixels < amountOfNotEmptyPixelsThreshold) {
            return null;
        }
        double coeff = 1 / ((amountOfNotEmptyPixels) - 0);

        for (int i = -lastIndex; i <= lastIndex; i++) {
            for (int j = -lastIndex; j <= lastIndex; j++) {
                if (Color.red(argbValues[x + i][y + j]) <= threshold) {
                    mask[i + lastIndex][j + lastIndex] = coeff;
                } else {
                    mask[i + lastIndex][j + lastIndex] = 0;
                }
            }
        }
        return mask;
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


    private int[][] getNeighbourPixels(int size, int x, int y, int[][] argbValues) {
        int[][] neighbourPixels = new int[size][size];
        int lastIndex = size / 2;
        for (int i = -lastIndex; i <= lastIndex; i++) {
            for (int j = -lastIndex; j <= lastIndex; j++) {
                neighbourPixels[i + lastIndex][j + lastIndex] = argbValues[x + i][y + j];
            }
        }
        return neighbourPixels;
    }


    public void useMedianFilter(int threshold) {
        int size = 9;
        int offset = size / 2;
        int[][] tempArgbValues = new int[bitmap.getWidth()][bitmap.getHeight()];
        for (int i = offset; i < bitmap.getWidth() - offset; i++) {
            for (int j = offset; j < bitmap.getHeight() - offset; j++) {
                if (Color.red(argbValues[i][j]) >= threshold) {
                    int newA = Color.alpha(argbValues[i][j]);
                    int[][] neighbourPixels = getNeighbourPixels(size, i, j, argbValues);
                    int[] sortedNeighbourPixels = sortFromLToH(neighbourPixels);
                    int newR = sortedNeighbourPixels[sortedNeighbourPixels.length / 2];
                    tempArgbValues[i][j] = Color.argb(newA, newR, newR, newR);
                } else {
                    int newA = Color.alpha(argbValues[i][j]);
                    int newR = Color.red(argbValues[i][j]);

                    tempArgbValues[i][j] = Color.argb(newA, newR, newR, newR);
                }
                bitmap.setPixel(i, j, tempArgbValues[i][j]);
            }
        }
        argbValues = tempArgbValues;
    }

    public void adaptiveGauss(int threshold, int size, int amountOfNotEmptyPixelsThreshold) {
        int offset = size / 2;
        int[][] tempArgbValues = argbValues.clone();

        for (int i = offset * 2; i < bitmap.getWidth() - offset * 2; i++) {
            for (int j = offset * 2; j < bitmap.getHeight() - offset * 2; j++) {

                if (Color.red(argbValues[i][j]) >= threshold) {
                    for (int k = -offset; k <= offset; k++) {
                        for (int l = -offset; l <= offset; l++) {
                            if (Color.red(argbValues[i + k][j + l]) <= threshold) {
                                double[][] matrix = getAdaptionMask(i + k, j + l, size, threshold, amountOfNotEmptyPixelsThreshold);
                                Mask mask;

                                if (matrix != null) {
                                    mask = new Mask(matrix);
                                    int newA = Color.alpha(argbValues[i][j]);
                                    float newR;
                                    if (mask.getMatrix() != null) {
                                        newR = Math.min(applyMaskForPixel(mask, i, j), 255);
                                        newR = Math.max(newR, 0);
                                    } else {
                                        newR = Color.red(argbValues[i][j]);
                                    }
                                    int finalR = Math.round(newR);
                                    tempArgbValues[i][j] = Color.argb(newA, finalR, finalR, finalR);
                                }
                            }
                        }
                    }
                }

                bitmap.setPixel(i, j, tempArgbValues[i][j]);
            }
        }

        for (int i = offset; i < bitmap.getWidth() - offset; i++) {
            for (int j = offset; j < bitmap.getHeight() - offset; j++) {

                if (Color.red(argbValues[i][j]) >= threshold) {
                    double[][] matrix = getAdaptionMask(i, j, size, threshold, amountOfNotEmptyPixelsThreshold);
                    Mask mask;
                    if (matrix != null) {
                        mask = new Mask(matrix);
                        int newA = Color.alpha(argbValues[i][j]);
                        float newR;
                        if (mask.getMatrix() != null) {
                            newR = Math.min(applyMaskForPixel(mask, i, j), 255);
                            newR = Math.max(newR, 0);
                        } else {
                            newR = Color.red(argbValues[i][j]);
                        }
                        int finalR = Math.round(newR);
                        tempArgbValues[i][j] = Color.argb(newA, finalR, finalR, finalR);
                    }
                }
                bitmap.setPixel(i, j, tempArgbValues[i][j]);
            }
        }
        argbValues = tempArgbValues.clone();
    }

    public void highlightVisibleCracks(int threshold) {
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                if (Color.red(argbValues[i][j]) >= threshold) {
                    bitmap.setPixel(i, j, Color.RED);
                }
            }
        }
    }
}
