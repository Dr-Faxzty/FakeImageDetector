package com.example.fakeimagedetector.logic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class FFTAnalyzerTest {

    @Test
    public void testAnalyzeWithNullImage() {
        FFTAnalyzer.AnalysisResult result = FFTAnalyzer.analyze(null);
        assertNull("Il risultato dovrebbe essere null per un'immagine nulla", result);
    }

    @Test
    public void testProbabilityRange() {
        Bitmap dummy = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        dummy.eraseColor(Color.BLUE);

        FFTAnalyzer.AnalysisResult result = FFTAnalyzer.analyze(dummy);

        assertNotNull("Il risultato dell'analisi non dovrebbe essere nullo", result);
        assertTrue("La probabilità deve essere >= 0", result.fakeProbability >= 0);
        assertTrue("La probabilità deve essere <= 100", result.fakeProbability <= 100);
    }

    @Test
    public void testFFTBitmapGeneration() {
        Bitmap dummy = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        FFTAnalyzer.AnalysisResult result = FFTAnalyzer.analyze(dummy);

        assertNotNull("La bitmap FFT non dovrebbe essere nulla", result.fftBitmap);
        assertTrue("La bitmap FFT dovrebbe avere dimensioni valide",
                result.fftBitmap.getWidth() > 0 && result.fftBitmap.getHeight() > 0);
    }

    @Test
    public void testConsistentResultForSameImage() {
        Bitmap dummy = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        dummy.setPixel(50, 50, Color.RED);

        FFTAnalyzer.AnalysisResult res1 = FFTAnalyzer.analyze(dummy);
        FFTAnalyzer.AnalysisResult res2 = FFTAnalyzer.analyze(dummy);

        assertTrue("Analisi identiche sulla stessa immagine devono dare la stessa probabilità",
                res1.fakeProbability == res2.fakeProbability);
    }
}