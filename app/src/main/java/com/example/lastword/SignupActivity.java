package com.example.lastword;

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

public class SignupActivity extends AppCompatActivity {
    private EditText etEmail, etUsername, etPassword;
    private Button btnSignup;
    private TextView tvLoginLink, tvTitle;
    private View flickerOverlay;
    private PreferencesManager prefs;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        prefs = new PreferencesManager(this);
        soundManager = new SoundManager(this);

        tvTitle = findViewById(R.id.tvTitle);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        flickerOverlay = findViewById(R.id.flickerOverlay);

        AnimationHelper.startFlickerSystem(flickerOverlay);
        
        // Atmosphere: Random subtle glitches
        final Handler atmosphereHandler = new Handler(Looper.getMainLooper());
        atmosphereHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && Math.random() > 0.8) {
                    AnimationHelper.glitchView(tvTitle);
                }
                atmosphereHandler.postDelayed(this, 5000 + (long)(Math.random() * 5000));
            }
        }, 2000);

        btnSignup.setOnClickListener(v -> {
            soundManager.playClick();
            String email = etEmail.getText().toString().trim();
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Leave no blank vows.", Toast.LENGTH_SHORT).show();
                AnimationHelper.shakeView(findViewById(R.id.formLayout), 1);
                return;
            }

            if (prefs.registerUser(user, email, pass)) {
                soundManager.vibrate(50);
                Toast.makeText(SignupActivity.this, "Soul registered into the void.", Toast.LENGTH_SHORT).show();
                finish(); 
            } else {
                Toast.makeText(SignupActivity.this, "Identity already bound in dark logs.", Toast.LENGTH_SHORT).show();
                AnimationHelper.shakeView(findViewById(R.id.formLayout), 2);
            }
        });

        tvLoginLink.setOnClickListener(v -> {
            soundManager.playClick();
            finish();
        });
    }
}
