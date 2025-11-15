package com.example.inteligentnypojemnik;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompartmentSettingsActivity extends AppCompatActivity {

    private List<Medication> medicationList;
    private EditMedicationAdapter adapter;
    private RecyclerView recyclerView;
    private EditText inputTime;

    private int deviceId = -1;
    private String dayKey = "";
    private String compartmentKey = "";
    private String deviceJson = "{}";
    private DeviceDetailsResponse.Configuration fullConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compartment_settings);

        ImageButton backButton = findViewById(R.id.buttonBack);
        MaterialButton saveButton = findViewById(R.id.button_save);
        MaterialButton addMedButton = findViewById(R.id.button_add_med);
        recyclerView = findViewById(R.id.edit_med_recycler_view);
        inputTime = findViewById(R.id.input_time);

        try {
            deviceId = getIntent().getIntExtra("DEVICE_ID", -1);
            dayKey = getIntent().getStringExtra("DAY_KEY");
            compartmentKey = getIntent().getStringExtra("COMPARTMENT_KEY");
            deviceJson = getIntent().getStringExtra("DEVICE_JSON");
            String containerJson = getIntent().getStringExtra("CONTAINER_JSON");

            if (deviceId == -1 || dayKey == null || compartmentKey == null || deviceJson == null) {
                Toast.makeText(this, "Błąd ładowania danych (brak ID)", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            Gson gson = new Gson();
            DeviceDetailsResponse fullDevice = gson.fromJson(deviceJson, DeviceDetailsResponse.class);
            fullConfiguration = fullDevice.configuration;

            DeviceDetailsResponse.ContainerConfig currentContainer = gson.fromJson(containerJson, DeviceDetailsResponse.ContainerConfig.class);

            medicationList = new ArrayList<>();
            if (currentContainer != null) {
                inputTime.setText(currentContainer.reminder_time);

                if (currentContainer.medicine != null) {
                    for (DeviceDetailsResponse.MedicineItem m : currentContainer.medicine) {
                        medicationList.add(new Medication(m.name, formatDoseToString(m.dose)));
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Błąd deserializacji JSON: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("JSON_ERR", "Błąd", e);
            finish();
            return;
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfiguration();
            }
        });

        addMedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                medicationList.add(new Medication("", "1 kapsułka"));
                adapter.notifyItemInserted(medicationList.size() - 1);
            }
        });

        adapter = new EditMedicationAdapter(this, medicationList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void saveConfiguration() {
        if (fullConfiguration == null || dayKey.isEmpty() || compartmentKey.isEmpty()) {
            Toast.makeText(this, "Błąd: Brak danych konfiguracyjnych.", Toast.LENGTH_SHORT).show();
            return;
        }

        String newTime = inputTime.getText().toString();
        List<DeviceDetailsResponse.MedicineItem> newMedicineList = new ArrayList<>();

        for (int i = 0; i < adapter.getItemCount(); i++) {
            EditMedicationAdapter.EditViewHolder holder = (EditMedicationAdapter.EditViewHolder) recyclerView.findViewHolderForAdapterPosition(i);

            String name;
            String dosageString;

            if (holder != null) {
                name = holder.name.getText().toString();
                dosageString = holder.dosage.getText().toString();
            } else {
                View view = recyclerView.getChildAt(i);
                if(view == null) continue;

                EditText medNameView = view.findViewById(R.id.edit_med_name);
                EditText medDosageView = view.findViewById(R.id.edit_med_dosage);

                name = medNameView.getText().toString();
                dosageString = medDosageView.getText().toString();
            }

            int dose = parseDoseFromString(dosageString);

            if (name != null && !name.isEmpty() && !name.trim().isEmpty() && dose > 0) {
                DeviceDetailsResponse.MedicineItem item = new DeviceDetailsResponse.MedicineItem();
                item.name = name;
                item.dose = dose;
                newMedicineList.add(item);
            }
        }

        if (newMedicineList.isEmpty() && adapter.getItemCount() > 0 && recyclerView.getChildCount() > 0) {
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View view = recyclerView.getChildAt(i);
                if(view == null) continue;

                EditText medNameView = view.findViewById(R.id.edit_med_name);
                EditText medDosageView = view.findViewById(R.id.edit_med_dosage);

                String name = medNameView.getText().toString();
                String dosageString = medDosageView.getText().toString();
                int dose = parseDoseFromString(dosageString);

                if (name != null && !name.isEmpty() && !name.trim().isEmpty() && dose > 0) {
                    DeviceDetailsResponse.MedicineItem item = new DeviceDetailsResponse.MedicineItem();
                    item.name = name;
                    item.dose = dose;
                    newMedicineList.add(item);
                }
            }
        }


        try {
            DeviceDetailsResponse.DayConfig dayConfig = fullConfiguration.get(dayKey);
            if (dayConfig == null) dayConfig = new DeviceDetailsResponse.DayConfig();

            if (dayConfig.containers == null) {
                dayConfig.containers = new java.util.HashMap<>();
            }

            DeviceDetailsResponse.ContainerConfig containerConfig = dayConfig.containers.get(compartmentKey);
            if (containerConfig == null) containerConfig = new DeviceDetailsResponse.ContainerConfig();

            containerConfig.reminder_time = newTime.isEmpty() ? null : newTime;
            containerConfig.medicine = newMedicineList;

            // USUWAMY TĘ LINIĘ:
            // containerConfig.active = !newMedicineList.isEmpty();

            dayConfig.containers.put(compartmentKey, containerConfig);

            UpdateConfigRequest requestBody = new UpdateConfigRequest(fullConfiguration);

            Log.d("API_PUT", new Gson().toJson(requestBody));

            RetrofitClient.getApiService(this).updateConfig(deviceId, requestBody).enqueue(new Callback<DeviceDetailsResponse>() {
                @Override
                public void onResponse(Call<DeviceDetailsResponse> call, Response<DeviceDetailsResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(CompartmentSettingsActivity.this, "Zapisano zmiany!", Toast.LENGTH_SHORT).show();

                        DeviceDetailsResponse newDeviceData = response.body();
                        String newDeviceJson = new Gson().toJson(newDeviceData);

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(CompartmentDetailsActivity.EXTRA_UPDATED_JSON, newDeviceJson);
                        setResult(RESULT_OK, resultIntent);

                        finish();
                    } else {
                        String errorMsg = "Błąd zapisu: " + response.code() + " " + response.message();
                        Log.e("API_ERROR", errorMsg);
                        Toast.makeText(CompartmentSettingsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<DeviceDetailsResponse> call, Throwable t) {
                    Log.e("API_FAILURE", "Błąd połączenia: " + t.getMessage());
                    Toast.makeText(CompartmentSettingsActivity.this, "Błąd połączenia: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Log.e("SAVE_ERR", "Błąd podczas przygotowania zapisu", e);
            Toast.makeText(this, "Błąd: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

    private int parseDoseFromString(String dosageString) {
        if (dosageString == null || dosageString.isEmpty()) {
            return 0;
        }
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(dosageString);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}