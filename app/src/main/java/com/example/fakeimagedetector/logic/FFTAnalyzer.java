package com.example.fakeimagedetector.logic;

import android.graphics.Bitmap;
import android.graphics.Color;
import org.jtransforms.fft.DoubleFFT_2D;

public class FFTAnalyzer {
    public static class AnalysisResult {
        public Bitmap fftBitmap;
        public double fakeProbability;

        public AnalysisResult(Bitmap fftBitmap, double fakeProbability) {
            this.fftBitmap = fftBitmap;
            this.fakeProbability = fakeProbability;
        }
    }

    public static AnalysisResult analyze(Bitmap src) {
        if (src == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createScaledBitmap(src, 256, 256, true);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        double[] data = new double[width * height * 2];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                double gray = (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114);
                data[2 * (y * width + x)] = gray;
                data[2 * (y * width + x) + 1] = 0;
            }
        }

        DoubleFFT_2D fft2D = new DoubleFFT_2D(height, width);
        fft2D.complexForward(data);

       return processFFTData(data, width, height);
    }

    private static AnalysisResult processFFTData(double[] data, int w, int h) {
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        double highFreqEnergy = 0;
        double totalEnergy = 0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double re = data[2 * (y * w + x)];
                double im = data[2 * (y * w + x) + 1];
                double magnitude = Math.log(1 + Math.sqrt(re * re + im * im));

               int val = (int) Math.min(255, magnitude * 20);
                output.setPixel(x, y, Color.rgb(val, val, val));

                double distance = Math.sqrt(Math.pow(x - w/2.0, 2) + Math.pow(y - h/2.0, 2));
                if (distance > (w / 4.0)) {
                    highFreqEnergy += magnitude;
                }
                totalEnergy += magnitude;
            }
        }

        double score = (highFreqEnergy / totalEnergy) * 100;
        double probability = Math.min(100, score);

        return new AnalysisResult(output, probability);
    }
}