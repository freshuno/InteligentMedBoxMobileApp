package com.example.inteligentnypojemnik;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.gson.Gson;

public class CompartmentDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_UPDATED_JSON = "UPDATED_JSON";
    public static final int REQUEST_CODE_UPDATE = 101;

    private TextView headerTitle, textTime, textMedCount;
    private RecyclerView recyclerView;
    private MedicationAdapter adapter;
    private List<Medication> medications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compartment_details);

        ImageButton backButton = findViewById(R.id.buttonBack);
        headerTitle = findViewById(R.id.header_title);
        textTime = findViewById(R.id.text_compartment_time);
        textMedCount = findViewById(R.id.text_compartment_med_count);
        recyclerView = findViewById(R.id.medication_recycler_view);
        TextView settingsButton = findViewById(R.id.button_compartment_settings);

        String compartmentName = getIntent().getStringExtra("COMPARTMENT_NAME");
        String time = getIntent().getStringExtra("TIME");
        String medCount = getIntent().getStringExtra("MED_COUNT");
        String containerJson = getIntent().getStringExtra("CONTAINER_JSON");

        headerTitle.setText(compartmentName);

        medications = new ArrayList<>();
        adapter = new MedicationAdapter(this, medications);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        refreshViews(time, medCount, containerJson);

        backButton.setOnClickListener(v -> finish());

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(CompartmentDetailsActivity.this, CompartmentSettingsActivity.class);
            intent.putExtra("DEVICE_ID", getIntent().getIntExtra("DEVICE_ID", -1));
            intent.putExtra("DAY_KEY", getIntent().getStringExtra("DAY_KEY"));
            intent.putExtra("DEVICE_JSON", getIntent().getStringExtra("DEVICE_JSON"));
            intent.putExtra("COMPARTMENT_KEY", getIntent().getStringExtra("COMPARTMENT_KEY"));
            intent.putExtra("CONTAINER_JSON", getIntent().getStringExtra("CONTAINER_JSON"));
            startActivityForResult(intent, REQUEST_CODE_UPDATE);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void refreshViews(String time, String medCount, String containerJson) {
        if (time != null) textTime.setText("Godzina przyjmowania: " + time);
        if (medCount != null) textMedCount.setText("Liczba leków: " + medCount);

        medications.clear();

        if (containerJson != null && !containerJson.isEmpty()) {
            DeviceDetailsResponse.ContainerConfig container =
                    new Gson().fromJson(containerJson, DeviceDetailsResponse.ContainerConfig.class);

            if (container != null && container.medicine != null) {
                for (DeviceDetailsResponse.MedicineItem m : container.medicine) {
                    String name = (m.name != null) ? m.name : "Lek";
                    String doseText = formatDoseToString(m.dose);
                    medications.add(new Medication(name, doseText));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_UPDATE && resultCode == RESULT_OK && data != null) {
            String updatedDeviceJson = data.getStringExtra(EXTRA_UPDATED_JSON);
            if (updatedDeviceJson == null) return;

            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_UPDATED_JSON, updatedDeviceJson);
            setResult(RESULT_OK, resultIntent);

            try {
                Gson gson = new Gson();
                String dayKey = getIntent().getStringExtra("DAY_KEY");
                String compartmentKey = getIntent().getStringExtra("COMPARTMENT_KEY");

                DeviceDetailsResponse newDetails = gson.fromJson(updatedDeviceJson, DeviceDetailsResponse.class);
                DeviceDetailsResponse.DayConfig newDay = newDetails.configuration.get(dayKey);
                DeviceDetailsResponse.ContainerConfig newContainer = newDay.containers.get(compartmentKey);

                String newContainerJson = gson.toJson(newContainer);
                String newTime = (newContainer != null && newContainer.reminder_time != null) ? newContainer.reminder_time : "—";
                String newMedCount = (newContainer != null && newContainer.medicine != null) ? String.valueOf(newContainer.medicine.size()) : "0";

                getIntent().putExtra("DEVICE_JSON", updatedDeviceJson);
                getIntent().putExtra("CONTAINER_JSON", newContainerJson);
                getIntent().putExtra("TIME", newTime);
                getIntent().putExtra("MED_COUNT", newMedCount);

                refreshViews(newTime, newMedCount, newContainerJson);

            } catch (Exception e) {
                Log.e("JSON_UPDATE", "Błąd odświeżania DetailsActivity", e);
            }
        }
    }

    private String formatDoseToString(int dose) {
        if (dose == 1) {
            return "1 kapsułka";
        } else if (dose >= 2 && dose <= 4) {
            return dose + " kapsułki";
        } else {
            return dose + " kapsułek";
        }
    }
}