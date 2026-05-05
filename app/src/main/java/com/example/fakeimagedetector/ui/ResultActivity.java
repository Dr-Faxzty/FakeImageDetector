package com.example.fakeimagedetector.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fakeimagedetector.R;
import com.example.fakeimagedetector.logic.FFTAnalyzer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ImageView ivFft = findViewById(R.id.ivFftResult);
        TextView tvPercent = findViewById(R.id.tvClassification);
        Button btnClose = findViewById(R.id.btnClose);

        String uriString = getIntent().getStringExtra("IMAGE_URI");
        if (uriString != null) {
            Uri imageUri = Uri.parse(uriString);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    Bitmap original = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    FFTAnalyzer.AnalysisResult result = FFTAnalyzer.analyze(original);

                    runOnUiThread(() -> {
                        ivFft.setImageBitmap(result.fftBitmap);
                        tvPercent.setText(String.format("Artificialità: %.2f%%", result.fakeProbability));
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        btnClose.setOnClickListener(v -> finish());
    }
}