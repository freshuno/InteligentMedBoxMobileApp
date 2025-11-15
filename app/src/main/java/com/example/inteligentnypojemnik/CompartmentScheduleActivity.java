package com.example.inteligentnypojemnik;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import java.util.Map;
import java.util.Collections;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompartmentScheduleActivity extends AppCompatActivity implements CompartmentAdapter.OnCompartmentActiveChangedListener {

    private int deviceId = -1;
    private String dayKey = "monday";
    private String deviceJson = "{}";
    public static final String EXTRA_UPDATED_JSON = "UPDATED_JSON";
    public static final int REQUEST_CODE_UPDATE = 101;

    private RecyclerView recyclerView;
    private CompartmentAdapter adapter;
    private List<Compartment> compartments;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compartment_schedule);

        ImageButton backButton = findViewById(R.id.buttonBack);
        TextView headerTitle = findViewById(R.id.header_title);
        recyclerView = findViewById(R.id.compartments_recycler_view);

        String deviceName = getIntent().getStringExtra("DEVICE_NAME");
        String dayName = getIntent().getStringExtra("DAY_NAME");
        dayKey  = getIntent().getStringExtra("DAY_KEY");
        deviceJson = getIntent().getStringExtra("DEVICE_JSON");
        deviceId = getIntent().getIntExtra("DEVICE_ID", -1);

        if (deviceName != null && dayName != null) {
            headerTitle.setText(deviceName + " - " + dayName);
        }

        backButton.setOnClickListener(v -> finish());

        loadAndDisplayCompartments(deviceJson);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });
    }

    private void loadAndDisplayCompartments(String deviceJson) {
        this.deviceJson = deviceJson;

        DeviceDetailsResponse details =
                new com.google.gson.Gson().fromJson(deviceJson, DeviceDetailsResponse.class);
        DeviceDetailsResponse.DayConfig day = details.configuration.get(dayKey);

        if (compartments == null) compartments = new ArrayList<>();
        compartments.clear();

        List<Map.Entry<String, DeviceDetailsResponse.ContainerConfig>> list =
                new ArrayList<>(day != null && day.containers != null ? day.containers.entrySet()
                        : java.util.Collections.emptyList());

        java.util.Collections.sort(list, (a,b) -> {
            try { return Integer.compare(Integer.parseInt(a.getKey()), Integer.parseInt(b.getKey())); }
            catch (NumberFormatException e) { return a.getKey().compareTo(b.getKey()); }
        });

        int idx = 1;
        for (Map.Entry<String, DeviceDetailsResponse.ContainerConfig> e : list) {
            DeviceDetailsResponse.ContainerConfig c = e.getValue();
            String title = "Przegroda " + (idx++);
            String time = (c != null && c.reminder_time != null) ? c.reminder_time : "—";
            String count = (c != null && c.medicine != null) ? String.valueOf(c.medicine.size()) : "0";

            Compartment comp = new Compartment(title, time, count);
            comp.setExtraJson(new com.google.gson.Gson().toJson(c));
            comp.setCompartmentKey(e.getKey());
            compartments.add(comp);
        }

        if (adapter == null) {
            adapter = new CompartmentAdapter(this, compartments);
            adapter.setOnItemClickListener(comp -> {
                Intent intent = new Intent(this, CompartmentDetailsActivity.class);
                intent.putExtra("COMPARTMENT_NAME", comp.getName());
                intent.putExtra("TIME", comp.getTime());
                intent.putExtra("MED_COUNT", comp.getMedCount());
                intent.putExtra("CONTAINER_JSON", comp.getExtraJson());

                intent.putExtra("DEVICE_ID", deviceId);
                intent.putExtra("DAY_KEY", dayKey);
                intent.putExtra("DEVICE_JSON", this.deviceJson);
                intent.putExtra("COMPARTMENT_KEY", comp.getCompartmentKey());

                startActivityForResult(intent, REQUEST_CODE_UPDATE);
            });

            adapter.setOnCompartmentActiveChangedListener(this);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_UPDATE && resultCode == RESULT_OK && data != null) {
            String updatedDeviceJson = data.getStringExtra(EXTRA_UPDATED_JSON);
            if (updatedDeviceJson != null) {
                this.deviceJson = updatedDeviceJson;
                loadAndDisplayCompartments(updatedDeviceJson);

                // Przekaż zaktualizowany JSON z powrotem do DeviceScheduleActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_UPDATED_JSON, updatedDeviceJson);
                setResult(RESULT_OK, resultIntent);
            }
        }
    }

    @Override
    public void onCompartmentActiveChanged(String compartmentKey, boolean isActive) {
        try {
            DeviceDetailsResponse fullDevice = gson.fromJson(deviceJson, DeviceDetailsResponse.class);
            DeviceDetailsResponse.Configuration config = fullDevice.configuration;

            DeviceDetailsResponse.DayConfig dayConfig = config.get(dayKey);
            if (dayConfig != null && dayConfig.containers != null) {
                DeviceDetailsResponse.ContainerConfig container = dayConfig.containers.get(compartmentKey);
                if (container != null) {
                    container.active = isActive;
                }
            }

            UpdateConfigRequest requestBody = new UpdateConfigRequest(config);
            Log.d("API_PUT", "Zapisywanie zmiany dla przegrody: " + compartmentKey + " (aktywny: " + isActive + ")");
            Log.d("API_PUT_JSON", gson.toJson(requestBody));

            RetrofitClient.getApiService(this).updateConfig(deviceId, requestBody).enqueue(new Callback<DeviceDetailsResponse>() {
                @Override
                public void onResponse(Call<DeviceDetailsResponse> call, Response<DeviceDetailsResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(CompartmentScheduleActivity.this, "Zapisano zmianę", Toast.LENGTH_SHORT).show();
                        String newJson = gson.toJson(response.body());
                        loadAndDisplayCompartments(newJson);

                        // Przekaż zaktualizowany JSON z powrotem do DeviceScheduleActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_UPDATED_JSON, newJson);
                        setResult(RESULT_OK, resultIntent);

                    } else {
                        Toast.makeText(CompartmentScheduleActivity.this, "Błąd zapisu: " + response.code(), Toast.LENGTH_SHORT).show();
                        loadAndDisplayCompartments(deviceJson);
                    }
                }

                @Override
                public void onFailure(Call<DeviceDetailsResponse> call, Throwable t) {
                    Toast.makeText(CompartmentScheduleActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    loadAndDisplayCompartments(deviceJson);
                }
            });

        } catch (Exception e) {
            Log.e("API_PUT_ERR", "Błąd przy zmianie statusu przegrody", e);
        }
    }
}