package com.example.imageviewer.Editor;

public class Mask {
    private final double[][] matrix;
    private final int size;
    private int amountOfCroppedPixels;

    public double[][] getMatrix() {
        return matrix;
    }

    public int getSize() {
        return size;
    }

    public Mask(double[][] matrix) {
        this.matrix = matrix;
        this.size = matrix.length;
    }

    public int getAmountOfCroppedPixels() {
        return amountOfCroppedPixels;
    }

    public void setAmountOfCroppedPixels(int amountOfCroppedPixels) {
        this.amountOfCroppedPixels = amountOfCroppedPixels;
    }
}
