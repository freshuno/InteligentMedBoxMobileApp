package com.example.inteligentnypojemnik;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import com.google.gson.Gson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeviceScheduleActivity extends AppCompatActivity implements WeekdayAdapter.OnDayActiveChangedListener {

    public static final int REQUEST_CODE_DETAILS = 200;

    private int deviceId = -1;
    private String deviceName = "Pudełko";
    private WeekdayAdapter adapter;
    private RecyclerView recyclerView;
    private DeviceDetailsResponse.Configuration currentConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_schedule);

        ImageButton backButton = findViewById(R.id.buttonBack);
        TextView headerTitle = findViewById(R.id.device_name_header);
        recyclerView = findViewById(R.id.weekdays_recycler_view);
        MaterialButton statsButton = findViewById(R.id.button_view_statistics);

        deviceName = getIntent().getStringExtra("DEVICE_NAME");
        deviceId = getIntent().getIntExtra("DEVICE_ID", -1);

        if (deviceName != null) headerTitle.setText(deviceName);

        backButton.setOnClickListener(v -> finish());
        statsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DeviceStatisticsActivity.class);
            intent.putExtra("DEVICE_ID", deviceId);
            startActivity(intent);
        });

        RetrofitClient.getApiService(this).getDeviceDetails(deviceId)
                .enqueue(new retrofit2.Callback<DeviceDetailsResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<DeviceDetailsResponse> call,
                                           retrofit2.Response<DeviceDetailsResponse> resp) {
                        if (!resp.isSuccessful() || resp.body() == null) {
                            android.widget.Toast.makeText(DeviceScheduleActivity.this,
                                    "Błąd pobierania urządzenia", android.widget.Toast.LENGTH_SHORT).show();
                            return;
                        }
                        DeviceDetailsResponse details = resp.body();
                        currentConfiguration = details.configuration;
                        String deviceJson = new com.google.gson.Gson().toJson(details);

                        java.util.List<String> weekdays = java.util.Arrays.asList(
                                "Poniedziałek","Wtorek","Środa","Czwartek","Piątek","Sobota","Niedziela"
                        );
                        adapter = new WeekdayAdapter(
                                DeviceScheduleActivity.this, weekdays, deviceName, deviceId, deviceJson, DeviceScheduleActivity.this
                        );
                        recyclerView.setLayoutManager(new LinearLayoutManager(DeviceScheduleActivity.this));
                        recyclerView.setAdapter(adapter);
                    }

                    @Override public void onFailure(retrofit2.Call<DeviceDetailsResponse> call, Throwable t) {
                        android.widget.Toast.makeText(DeviceScheduleActivity.this,
                                "Błąd sieci: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });
    }

    @Override
    public void onDayActiveChanged(String dayKey, boolean isActive, DeviceDetailsResponse.Configuration newConfig) {
        this.currentConfiguration = newConfig;
        UpdateConfigRequest requestBody = new UpdateConfigRequest(newConfig);

        Log.d("API_PUT", "Zapisywanie zmiany dla dnia: " + dayKey + " (aktywny: " + isActive + ")");

        RetrofitClient.getApiService(this).updateConfig(deviceId, requestBody).enqueue(new Callback<DeviceDetailsResponse>() {
            @Override
            public void onResponse(Call<DeviceDetailsResponse> call, Response<DeviceDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DeviceScheduleActivity.this, "Zapisano zmianę", Toast.LENGTH_SHORT).show();
                    currentConfiguration = response.body().configuration;
                    adapter.updateConfiguration(currentConfiguration);
                } else {
                    Toast.makeText(DeviceScheduleActivity.this, "Błąd zapisu", Toast.LENGTH_SHORT).show();
                    adapter.updateConfiguration(currentConfiguration);
                }
            }

            @Override
            public void onFailure(Call<DeviceDetailsResponse> call, Throwable t) {
                Toast.makeText(DeviceScheduleActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                adapter.updateConfiguration(currentConfiguration);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_DETAILS && resultCode == RESULT_OK && data != null) {
            String updatedJson = data.getStringExtra("UPDATED_JSON");

            if (updatedJson != null && adapter != null) {
                try {
                    DeviceDetailsResponse response = new Gson().fromJson(updatedJson, DeviceDetailsResponse.class);
                    if (response != null && response.configuration != null) {
                        this.currentConfiguration = response.configuration;
                        adapter.updateConfiguration(this.currentConfiguration);
                    }
                } catch (Exception e) {
                    Log.e("DeviceSchedule", "Błąd aktualizacji JSON po powrocie", e);
                }
            }
        }
    }
}