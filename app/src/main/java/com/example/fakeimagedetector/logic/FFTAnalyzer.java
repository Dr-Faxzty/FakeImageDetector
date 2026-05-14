package com.example.fakeimagedetector.logic;

import android.graphics.Bitmap;
import android.graphics.Color;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class FFTAnalyzer {
    public static class AnalysisResult {
        public Bitmap fftBitmap;
        public double fakeProbability;

        public AnalysisResult(Bitmap fftBitmap, double fakeProbability) {
            this.fftBitmap = fftBitmap;
            this.fakeProbability = fakeProbability;
        }
    }

    public static AnalysisResult analyze(Bitmap bitmap) {
        int size = 256;
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, size, size, true);

        Complex[][] matrix = new Complex[size][size];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int p = scaled.getPixel(x, y);
                double gray = (Color.red(p) * 0.299 + Color.green(p) * 0.587 + Color.blue(p) * 0.114);
                matrix[y][x] = new Complex(gray, 0);
            }
        }

        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);

        for (int i = 0; i < size; i++) {
            matrix[i] = transformer.transform(matrix[i], TransformType.FORWARD);
        }

        for (int j = 0; j < size; j++) {
            Complex[] column = new Complex[size];
            for (int i = 0; i < size; i++) column[i] = matrix[i][j];
            column = transformer.transform(column, TransformType.FORWARD);
            for (int i = 0; i < size; i++) matrix[i][j] = column[i];
        }

        return processResults(matrix, size);
    }

    private static AnalysisResult processResults(Complex[][] matrix, int size) {
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        double highFreqEnergy = 0;
        double totalEnergy = 0;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double mag = Math.log(1 + matrix[y][x].abs());

                int displayX = (x + size / 2) % size;
                int displayY = (y + size / 2) % size;
                int colorVal = (int) Math.min(255, mag * 20);
                output.setPixel(displayX, displayY, Color.rgb(colorVal, colorVal, colorVal));

                double dist = Math.sqrt(Math.pow(x - size/2.0, 2) + Math.pow(y - size/2.0, 2));
                if (dist > (size / 4.0)) {
                    highFreqEnergy += mag;
                }
                totalEnergy += mag;
            }
        }

        double probability = (highFreqEnergy / totalEnergy) * 100;
        return new AnalysisResult(output, Math.min(100, probability));
    }
}