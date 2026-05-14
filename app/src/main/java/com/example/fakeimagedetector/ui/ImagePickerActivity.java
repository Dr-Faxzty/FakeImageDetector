package com.example.fakeimagedetector.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.ActivityOptions;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fakeimagedetector.R;
import com.example.fakeimagedetector.security.AnalysisManager;
import com.example.fakeimagedetector.security.AuthManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.IOException;

public class ImagePickerActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_IMAGE_REQUEST = 101;

    private ImageView ivPreview;
    private Button btnCheck;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);

        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                handleLogout();
                return true;
            } else if (item.getItemId() == R.id.action_history) {
                showHistoryDialog();
                return true;
            }
            return false;
        });

        ivPreview = findViewById(R.id.ivPreview);
        Button btnLoad = findViewById(R.id.btnLoad);
        btnCheck = findViewById(R.id.btnCheck);

        btnLoad.setText(R.string.btn_load);
        btnCheck.setText(R.string.btn_check);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        btnLoad.setOnClickListener(v -> checkPermissionAndOpenGallery());

        btnCheck.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                Intent intent = new Intent(this, ResultActivity.class);
                intent.putExtra("IMAGE_URI", selectedImageUri.toString());

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        ivPreview,
                        "shared_image_container"
                );

                startActivity(intent, options.toBundle());
            } else {
                Toast.makeText(this, "Seleziona prima un'immagine", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLogout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkPermissionAndOpenGallery() {
        String permission;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/jpeg");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void showHistoryDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_history, null);
        androidx.recyclerview.widget.RecyclerView rvHistory = dialogView.findViewById(R.id.rvHistory);

        AnalysisManager analysisManager = new AnalysisManager(this);
        AuthManager authManager = new AuthManager(this);
        int userId = authManager.getLoggedInUserId();

        android.database.Cursor cursor = analysisManager.getHistoryCursor(userId);

        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "Nessuna analisi salvata", Toast.LENGTH_SHORT).show();
            return;
        }

        AnalysisAdapter adapter = new AnalysisAdapter(cursor);
        rvHistory.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Ultime 5 Analisi")
                .setView(dialogView)
                .setPositiveButton("Chiudi", (dialog, which) -> cursor.close()) // Ricorda di chiudere il cursor
                .setOnDismissListener(dialog -> cursor.close())
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permesso negato.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                ivPreview.setImageBitmap(bitmap);

                btnCheck.setEnabled(true);
                btnCheck.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary));
                btnCheck.setTextColor(ContextCompat.getColor(this, R.color.primary));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}