package com.example.inteligentnypojemnik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.button.MaterialButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        String defaultRole = sessionManager.getDefaultRole();
        boolean forceSelect = getIntent().getBooleanExtra("FORCE_SELECT", false);

        if (!forceSelect && defaultRole != null) {
            if ("caregiver".equals(defaultRole)) {
                startActivity(new Intent(this, CaregiverPanelActivity.class));
                finish();
                return;
            } else if ("elderly".equals(defaultRole)) {
                startActivity(new Intent(this, ElderlyPanelActivity.class));
                finish();
                return;
            }
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_role_selection);

        MaterialButton btnCaregiver = findViewById(R.id.button_select_caregiver);
        MaterialButton btnElderly = findViewById(R.id.button_select_elderly);

        btnCaregiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoleSelectionActivity.this, CaregiverPanelActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnElderly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoleSelectionActivity.this, ElderlyPanelActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}