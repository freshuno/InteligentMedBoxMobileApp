package com.example.inteligentnypojemnik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CompartmentDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compartment_details);

        ImageButton backButton = findViewById(R.id.buttonBack);
        TextView headerTitle = findViewById(R.id.header_title);
        TextView textTime = findViewById(R.id.text_compartment_time);
        TextView textMedCount = findViewById(R.id.text_compartment_med_count);
        RecyclerView recyclerView = findViewById(R.id.medication_recycler_view);
        TextView settingsButton = findViewById(R.id.button_compartment_settings);

        String compartmentName = getIntent().getStringExtra("COMPARTMENT_NAME");
        String time = getIntent().getStringExtra("TIME");
        String medCount = getIntent().getStringExtra("MED_COUNT");

        if (compartmentName != null) {
            headerTitle.setText(compartmentName);
        }
        if (time != null) {
            textTime.setText("Godzina przyjmowania: " + time);
        }
        if (medCount != null) {
            textMedCount.setText("Liczba leków: " + medCount);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CompartmentDetailsActivity.this, CompartmentSettingsActivity.class);
                startActivity(intent);
            }
        });

        List<Medication> medications = new ArrayList<>();
        medications.add(new Medication("Lek1 500 mg", "1 kapsułka"));
        medications.add(new Medication("Lek2 250 mg", "2 kapsułki"));
        medications.add(new Medication("Lek3 250 mg", "1 kapsułka"));

        MedicationAdapter adapter = new MedicationAdapter(this, medications);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}