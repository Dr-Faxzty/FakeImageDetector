package com.example.fakeimagedetector.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.fakeimagedetector.R;
import com.example.fakeimagedetector.security.AuthManager;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnAction;
    private TextView tvSwitchAuth;
    private AuthManager authManager;
    private boolean isRegistrationMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = new AuthManager(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnAction = findViewById(R.id.btnLogin);
        tvSwitchAuth = findViewById(R.id.tvSwitchAuth);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        if (!authManager.isAnyUserRegistered()) {
            setupRegistrationUI();
        }

        btnAction.setOnClickListener(v -> handleAuthAction());

        tvSwitchAuth.setOnClickListener(v -> {
            isRegistrationMode = !isRegistrationMode;
            updateUI();
        });
    }

    private void handleAuthAction() {
        String user = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Inserisci tutte le credenziali", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isRegistrationMode) {
            authManager.register(user, pass);
            Toast.makeText(this, "Registrazione completata! Accedi ora.", Toast.LENGTH_SHORT).show();
            isRegistrationMode = false;
            updateUI();
        } else {
            if (authManager.login(user, pass)) {
                Intent intent = new Intent(LoginActivity.this, ImagePickerActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Credenziali errate", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUI() {
        if (isRegistrationMode) {
            btnAction.setText(R.string.btn_register);
            tvSwitchAuth.setText(R.string.auth_switch_login);

            btnAction.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary));
        } else {
            btnAction.setText(R.string.btn_login);
            tvSwitchAuth.setText(R.string.auth_switch_register);

            btnAction.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.secondary));
        }
    }

    private void setupRegistrationUI() {
        isRegistrationMode = true;
        updateUI();
    }
}