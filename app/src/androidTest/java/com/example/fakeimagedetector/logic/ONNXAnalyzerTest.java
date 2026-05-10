package com.example.fakeimagedetector.logic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ONNXAnalyzerTest {

    private ONNXAnalyzer onnxAnalyzer;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        onnxAnalyzer = new ONNXAnalyzer(context);
    }

    @Test
    public void testInizializzazioneSessione() {
        assertNotNull("L'analizzatore ONNX non dovrebbe essere nullo", onnxAnalyzer);
    }

    @Test
    public void testPredictProbabilityRange() {
        try {
            Bitmap dummy = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888);
            dummy.eraseColor(Color.GRAY);

            double probability = onnxAnalyzer.predict(dummy);

            assertTrue("La probabilità non può essere NaN", !Double.isNaN(probability));
            assertTrue("La probabilità deve essere >= 0", probability >= 0);
            assertTrue("La probabilità deve essere <= 100", probability <= 100);

        } catch (Exception e) {
            fail("L'inferenza ha lanciato un'eccezione: " + e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPredictWithNullBitmap() throws Exception {
        onnxAnalyzer.predict(null);
    }

    @Test
    public void testConsistencySameImage() throws Exception {
        Bitmap dummy = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888);
        dummy.setPixel(112, 112, Color.GREEN);

        double res1 = onnxAnalyzer.predict(dummy);
        double res2 = onnxAnalyzer.predict(dummy);

        assertTrue("Risultati IA non consistenti", res1 == res2);
    }
}