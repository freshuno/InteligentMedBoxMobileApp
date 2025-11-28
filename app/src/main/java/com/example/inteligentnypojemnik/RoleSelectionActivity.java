package com.example.inteligentnypojemnik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.card.MaterialCardView; // [NOWE] Import dla kart

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

        MaterialCardView cardCaregiver = findViewById(R.id.card_caregiver);
        MaterialCardView cardElderly = findViewById(R.id.card_elderly);

        cardCaregiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoleSelectionActivity.this, CaregiverPanelActivity.class);
                startActivity(intent);
                finish();
            }
        });

        cardElderly.setOnClickListener(new View.OnClickListener() {
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