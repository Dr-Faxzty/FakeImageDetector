package com.example.fakeimagedetector.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fakeimagedetector.R;
import com.example.fakeimagedetector.logic.FFTAnalyzer;
import com.example.fakeimagedetector.logic.ONNXAnalyzer;
import com.example.fakeimagedetector.security.AnalysisManager;
import com.example.fakeimagedetector.security.AuthManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResultActivity extends AppCompatActivity {
    private ONNXAnalyzer onnxAnalyzer;
    private CircularProgressIndicator loadingProgress;
    private View resultsContainer;
    private TextView tvResultFFT, tvResultAI, tvVerdictText, tvHelpHint;
    private MaterialCardView cardVerdict;

    private AnalysisManager analysisManager;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportPostponeEnterTransition();
        setContentView(R.layout.activity_result);

        ImageView ivFftResult = findViewById(R.id.ivFftResult);
        resultsContainer = findViewById(R.id.resultsContainer);
        tvResultFFT = findViewById(R.id.tvResultFFT);
        tvResultAI = findViewById(R.id.tvResultAI);
        tvVerdictText = findViewById(R.id.tvVerdictText);
        tvHelpHint = findViewById(R.id.tvHelpHint);
        cardVerdict = findViewById(R.id.cardVerdict);
        Button btnClose = findViewById(R.id.btnClose);
        loadingProgress = findViewById(R.id.loadingProgress);

        loadingProgress.setVisibility(View.VISIBLE);
        resultsContainer.setVisibility(View.INVISIBLE);
        cardVerdict.setVisibility(View.INVISIBLE);
        tvHelpHint.setVisibility(View.INVISIBLE);

        analysisManager = new AnalysisManager(this);
        authManager = new AuthManager(this);

        String uriString = getIntent().getStringExtra("IMAGE_URI");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        if (uriString != null) {
            Uri imageUri = Uri.parse(uriString);
            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.execute(() -> {
                try {
                    Bitmap original = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                    FFTAnalyzer.AnalysisResult fftResult = FFTAnalyzer.analyze(original);

                    if (onnxAnalyzer == null) {
                        onnxAnalyzer = new ONNXAnalyzer(this);
                    }
                    double aiProbability = onnxAnalyzer.predict(original);

                    double average = (fftResult.fakeProbability + aiProbability) / 2;
                    String verdictLabel = getVerdictLabel(average);
                    int currentUserId = authManager.getLoggedInUserId();

                    analysisManager.addAnalysis(currentUserId, average, verdictLabel);

                    runOnUiThread(() -> {
                        loadingProgress.setVisibility(View.GONE);
                        resultsContainer.setVisibility(View.VISIBLE);
                        cardVerdict.setVisibility(View.VISIBLE);
                        tvHelpHint.setVisibility(View.VISIBLE);

                        ivFftResult.setImageBitmap(fftResult.fftBitmap);

                        ivFftResult.getViewTreeObserver().addOnPreDrawListener(
                                new ViewTreeObserver.OnPreDrawListener() {
                                    @Override
                                    public boolean onPreDraw() {
                                        ivFftResult.getViewTreeObserver().removeOnPreDrawListener(this);
                                        supportStartPostponedEnterTransition();
                                        return true;
                                    }
                                });

                        updateVerdictUI(average, label -> {
                            tvResultFFT.setText(String.format("%.1f%%", fftResult.fakeProbability));
                            tvResultAI.setText(String.format("%.1f%%", aiProbability));
                        });
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        loadingProgress.setVisibility(View.GONE);
                        supportStartPostponedEnterTransition();
                        Toast.makeText(this, "Errore analisi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            });
        }

        btnClose.setOnClickListener(v -> finishAfterTransition());
    }

    private String getVerdictLabel(double average) {
        if (average > 70) return "SOSPETTO FAKE";
        if (average < 30) return "PROBABILMENTE REALE";
        return "ANALISI INCERTA";
    }

    private void updateVerdictUI(double average, UIUpdateCallback callback) {
        String label = getVerdictLabel(average);
        tvVerdictText.setText(label);

        int color;
        if (average > 70) {
            color = getColor(R.color.fake_red);
        } else if (average < 30) {
            color = getColor(R.color.real_green);
        } else {
            color = getColor(R.color.primary);
        }

        cardVerdict.setStrokeColor(color);
        tvVerdictText.setTextColor(color);

        if (callback != null) callback.onUpdate(label);
    }

    // Interfaccia funzionale per pulizia del codice UI
    private interface UIUpdateCallback {
        void onUpdate(String label);
    }
}