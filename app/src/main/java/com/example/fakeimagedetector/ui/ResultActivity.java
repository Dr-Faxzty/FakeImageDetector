package com.example.fakeimagedetector.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fakeimagedetector.R;
import com.example.fakeimagedetector.logic.FFTAnalyzer;
import com.example.fakeimagedetector.logic.ONNXAnalyzer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResultActivity extends AppCompatActivity {
    private ONNXAnalyzer onnxAnalyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ImageView ivFftResult = findViewById(R.id.ivFftResult);
        TextView tvClassification = findViewById(R.id.tvClassification);
        Button btnClose = findViewById(R.id.btnClose);

        String uriString = getIntent().getStringExtra("IMAGE_URI");
        boolean useAI = getIntent().getBooleanExtra("USE_AI", false);

        if (uriString != null) {
            Uri imageUri = Uri.parse(uriString);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    Bitmap original = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                    final double probability;
                    final Bitmap displayBitmap;

                    if (useAI) {
                        if (onnxAnalyzer == null) {
                            onnxAnalyzer = new ONNXAnalyzer(this);
                        }
                        probability = onnxAnalyzer.predict(original);
                        displayBitmap = original;
                    } else {
                        FFTAnalyzer.AnalysisResult result = FFTAnalyzer.analyze(original);
                        probability = result.fakeProbability;
                        displayBitmap = result.fftBitmap;
                    }

                    runOnUiThread(() -> {
                        ivFftResult.setImageBitmap(displayBitmap);
                        String method = useAI ? "IA (ONNX)" : "FFT";
                        String methodLabel = getString(R.string.method_label, method);
                        String probLabel = getString(R.string.probability_label, probability);

                        tvClassification.setText(methodLabel + "\n" + probLabel);

                        if (probability > 50) {
                            tvClassification.setTextColor(getColor(R.color.fake_red));
                        } else {
                            tvClassification.setTextColor(getColor(R.color.real_green));
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(this, "Errore durante l'analisi: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            });
        }

        btnClose.setOnClickListener(v -> finish());
    }
}