package com.example.inteligentnypojemnik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.google.android.material.button.MaterialButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(getApplicationContext());

        ImageButton backButton = findViewById(R.id.buttonBack);
        RadioGroup radioGroup = findViewById(R.id.radioGroupRole);
        RadioButton radioAlwaysAsk = findViewById(R.id.radioAlwaysAsk);
        RadioButton radioElderly = findViewById(R.id.radioElderly);
        RadioButton radioCaregiver = findViewById(R.id.radioCaregiver);
        MaterialButton btnSwitchNow = findViewById(R.id.button_switch_mode_now);

        String currentRole = sessionManager.getDefaultRole();
        if (currentRole == null) {
            radioAlwaysAsk.setChecked(true);
        } else if ("elderly".equals(currentRole)) {
            radioElderly.setChecked(true);
        } else if ("caregiver".equals(currentRole)) {
            radioCaregiver.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioElderly) {
                sessionManager.saveDefaultRole("elderly");
            } else if (checkedId == R.id.radioCaregiver) {
                sessionManager.saveDefaultRole("caregiver");
            } else {
                sessionManager.saveDefaultRole(null);
            }
        });

        btnSwitchNow.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, RoleSelectionActivity.class);
            intent.putExtra("FORCE_SELECT", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        backButton.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}