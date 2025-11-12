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

        if (compartmentName != null) headerTitle.setText(compartmentName);
        if (time != null) textTime.setText("Godzina przyjmowania: " + time);
        if (medCount != null) textMedCount.setText("Liczba leków: " + medCount);

        backButton.setOnClickListener(v -> finish());

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(CompartmentDetailsActivity.this, CompartmentSettingsActivity.class);
            startActivity(intent);
        });

        String containerJson = getIntent().getStringExtra("CONTAINER_JSON");

        List<Medication> medications = new ArrayList<>();

        if (containerJson != null && !containerJson.isEmpty()) {
            DeviceDetailsResponse.ContainerConfig container =
                    new com.google.gson.Gson().fromJson(containerJson, DeviceDetailsResponse.ContainerConfig.class);

            if (container != null && container.medicine != null) {
                for (DeviceDetailsResponse.MedicineItem m : container.medicine) {
                    String name = (m.name != null) ? m.name : "Lek";
                    String doseText;
                    if (m.dose == 1) {
                        doseText = "1 kapsułka";
                    } else if (m.dose >= 2 && m.dose <= 4) {
                        doseText = m.dose + " kapsułki";
                    } else {
                        doseText = m.dose + " kapsułek";
                    }
                    medications.add(new Medication(name, doseText));
                }
            }
        }

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
