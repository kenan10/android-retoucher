package com.example.imageviewer.Editor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.Arrays;

public class Image {
    private final Bitmap bitmap;
    int[][] argbValues;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Image (Bitmap bitmap) {
        this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        this.argbValues = getRgbValuesFromBitmap(this.bitmap);
    }

    private int[][] getRgbValuesFromBitmap(@NonNull Bitmap bitmap) {
        int[][] argbValues = new int[bitmap.getWidth()][bitmap.getHeight()];
        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                int pixel = bitmap.getPixel(i, j);
                int newPixel = (Color.red(pixel));
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

    private Mask getAdaptionMask(int x, int y, int size, int threshold, int amountOfNotEmptyPixelsThreshold, boolean useDiagonalPixels) {
        int lastIndex = size / 2;
        float amountOfNotEmptyPixels = 0;
        double[][] matrix = new double[size][size];
        Mask mask;


        if (useDiagonalPixels) {
            for (int i = -lastIndex; i <= lastIndex; i++) {
                for (int j = -lastIndex; j <= lastIndex; j++) {

                    if (Color.red(argbValues[x + i][y + j]) <= threshold) {
                        amountOfNotEmptyPixels++;
                    }
                }
            }
        } else {
            for (int i = -lastIndex; i <= lastIndex; i++) {
                for (int j = -lastIndex; j <= lastIndex; j++) {
                    if ((i + j) % 2 != 0 || i == j) {
                        if (Color.red(argbValues[x + i][y + j]) <= threshold) {
                            amountOfNotEmptyPixels++;
                        }
                    }
                }
            }
        }

        if (amountOfNotEmptyPixels < amountOfNotEmptyPixelsThreshold) {
            return null;
        }
        double coeff = 1 / amountOfNotEmptyPixels;

        if (useDiagonalPixels) {
            for (int i = -lastIndex; i <= lastIndex; i++) {
                for (int j = -lastIndex; j <= lastIndex; j++) {
                    if (Color.red(argbValues[x + i][y + j]) <= threshold) {
                        matrix[i + lastIndex][j + lastIndex] = coeff;
                    } else {
                        matrix[i + lastIndex][j + lastIndex] = 0;
                    }
                }
            }
        } else {
            for (int i = -lastIndex; i <= lastIndex; i++) {
                for (int j = -lastIndex; j <= lastIndex; j++) {
                    if (Color.red(argbValues[x + i][y + j]) <= threshold) {
                        if ((i + j) % 2 != 0 || i == j) {
                            matrix[i + lastIndex][j + lastIndex] = coeff;
                        }
                    } else {
                        matrix[i + lastIndex][j + lastIndex] = 0;
                    }
                }
            }
        }

        mask = new Mask(matrix);

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

    private double[] sortFromLToH(double[][] arr) {
        double[] newArr = new double[arr.length * arr[0].length];

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                newArr[(i * arr.length) + j] = arr[i][j];
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

    public void adaptiveGauss(int threshold, int size, int amountOfNotEmptyPixelsThreshold, boolean blurEdges, boolean combinedApplyMask) {
        int offset = size / 2;
        int[][] tempArgbValues = argbValues.clone();

        if (blurEdges) {
            for (int i = offset * 2; i < bitmap.getWidth() - offset * 2; i++) {
                for (int j = offset * 2; j < bitmap.getHeight() - offset * 2; j++) {

                    if (Color.red(argbValues[i][j]) >= threshold) {
                        for (int k = -offset; k <= offset; k++) {
                            for (int l = -offset; l <= offset; l++) {
                                if (Color.red(argbValues[i + k][j + l]) <= threshold) {
                                    if ((i + j) % 2 != 0 || i == j) {
                                        Mask mask = getAdaptionMask(i + k, j + l, 3, threshold, 0, false);

                                        if (mask != null) {
                                            int newA = Color.alpha(argbValues[i + k][j + l]);
                                            float newR;
                                            if (mask.getMatrix() != null) {
                                                newR = Math.min(applyMaskForPixel(mask, i + k, j + l), 255);
                                                newR = Math.max(newR, 0);
                                            } else {
                                                newR = Color.red(argbValues[i + k][j + l]);
                                            }
                                            int finalR = Math.round(newR);
                                            tempArgbValues[i + k][j + l] = Color.argb(newA, finalR, finalR, finalR);
                                        }

                                        bitmap.setPixel(i + k, j + l, tempArgbValues[i + k][j + l]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int i = offset; i < bitmap.getWidth() - offset; i++) {
            for (int j = offset; j < bitmap.getHeight() - offset; j++) {

                if (Color.red(argbValues[i][j]) >= threshold) {
                    Mask mask = getAdaptionMask(i, j, size, threshold, amountOfNotEmptyPixelsThreshold, true);
                    if (mask != null) {
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

    public void highlightVisibleCracks(int threshold, int maskSize, int amountOfNotEmpty) {
        int offset = maskSize / 2;

        for (int i = 0; i < bitmap.getWidth(); i++) {
            for (int j = 0; j < bitmap.getHeight(); j++) {
                bitmap.setPixel(i, j, argbValues[i][j]);
            }
        }

        for (int i = offset; i < bitmap.getWidth() - offset; i++) {
            for (int j = offset; j < bitmap.getHeight() - offset; j++) {
                if (Color.red(argbValues[i][j]) >= threshold) {
                    Mask mask = getAdaptionMask(i, j, maskSize, threshold, amountOfNotEmpty, true);
                    if (mask != null) {
                        bitmap.setPixel(i, j, Color.RED);
                    }
                }
            }
        }
    }
}
