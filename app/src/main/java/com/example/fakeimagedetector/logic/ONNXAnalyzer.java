package com.example.fakeimagedetector.logic;

import android.content.Context;
import android.graphics.Bitmap;
import ai.onnxruntime.*;
import java.util.Collections;

public class ONNXAnalyzer {
    private final OrtEnvironment env;
    private final OrtSession session;
    private final String modelName;

    public ONNXAnalyzer(Context context, String modelName) throws Exception {
        this.env = OrtEnvironment.getEnvironment();
        this.modelName = modelName;
        String modelPath = copyAssetToFile(context);
        this.session = env.createSession(modelPath);
    }

    private String copyAssetToFile(Context context) throws Exception {
        java.io.File file = new java.io.File(context.getFilesDir(), modelName);
        if (!file.exists()) {
            try (java.io.InputStream is = context.getAssets().open(modelName);
                 java.io.OutputStream os = new java.io.FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
        }
        return file.getAbsolutePath();
    }

    public double predict(Bitmap bitmap) throws Exception {
        if (bitmap == null) {
            throw new IllegalArgumentException("Bitmap cannot be null");
        }

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        float[] inputData = bitmapToFloatArray(resized);

        long[] shape = {1, 3, 224, 224};
        OnnxTensor tensor = OnnxTensor.createTensor(env, java.nio.FloatBuffer.wrap(inputData), shape);

        OrtSession.Result result = session.run(Collections.singletonMap("input", tensor));
        float[][] output = (float[][]) result.get(0).getValue();

        double probabilityFake = 0.0;
        int outputClasses = output[0].length;

        if (outputClasses == 1) {
            float logit = output[0][0];
            probabilityFake = 1.0 / (1.0 + Math.exp(-logit));
        } else if (outputClasses >= 2) {
            float realLogit = output[0][0];
            float fakeLogit = output[0][1];

            double expReal = Math.exp(realLogit);
            double expFake = Math.exp(fakeLogit);
            probabilityFake = expFake / (expReal + expFake);
        }

        return probabilityFake * 100;
    }

    private float[] bitmapToFloatArray(Bitmap bitmap) {
        int width = 224;
        int height = 224;
        int[] intValues = new int[width * height];
        float[] floatValues = new float[3 * width * height];

        bitmap.getPixels(intValues, 0, width, 0, 0, width, height);

        for (int i = 0; i < intValues.length; i++) {
            final int val = intValues[i];
            float r = (((val >> 16) & 0xFF) / 255.0f - 0.5f) / 0.5f;
            float g = (((val >> 8) & 0xFF) / 255.0f - 0.5f) / 0.5f;
            float b = ((val & 0xFF) / 255.0f - 0.5f) / 0.5f;

            floatValues[i] = r;
            floatValues[i + width * height] = g;
            floatValues[i + 2 * width * height] = b;
        }
        return floatValues;
    }
}