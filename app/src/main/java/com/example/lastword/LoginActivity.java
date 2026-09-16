package com.example.lastword;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lastword.data.PreferencesManager;
import com.example.lastword.utils.AnimationHelper;
import com.example.lastword.utils.SoundManager;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvSignupLink, tvTitle;
    private View flickerOverlay;
    private PreferencesManager prefs;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = new PreferencesManager(this);
        soundManager = new SoundManager(this);

        // Redirect if already logged in
        if (prefs.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        tvTitle = findViewById(R.id.tvTitle);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignupLink = findViewById(R.id.tvSignupLink);
        flickerOverlay = findViewById(R.id.flickerOverlay);

        AnimationHelper.startFlickerSystem(flickerOverlay);
        
        // Ambient psychological elements
        final Handler atmosphereHandler = new Handler(Looper.getMainLooper());
        atmosphereHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && Math.random() > 0.88) {
                    AnimationHelper.glitchView(tvTitle);
                }
                atmosphereHandler.postDelayed(this, 5000 + (long)(Math.random() * 7000));
            }
        }, 3000);

        btnLogin.setOnClickListener(v -> {
            soundManager.playClick();
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Fill all dark entries.", Toast.LENGTH_SHORT).show();
                AnimationHelper.shakeView(findViewById(R.id.formLayout), 1);
                return;
            }

            if (prefs.loginUser(user, pass)) {
                // Use robust vibration wrapper to avoid SecurityException on some systems
                soundManager.vibrate(80);
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Access denied. Records misaligned.", Toast.LENGTH_SHORT).show();
                AnimationHelper.shakeView(findViewById(R.id.formLayout), 2);
                AnimationHelper.triggerSingleFlicker(flickerOverlay);
                soundManager.playWrong();
            }
        });

        tvSignupLink.setOnClickListener(v -> {
            soundManager.playClick();
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }
}
