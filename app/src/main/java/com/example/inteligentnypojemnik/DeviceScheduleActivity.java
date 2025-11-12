package com.example.inteligentnypojemnik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
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

public class DeviceScheduleActivity extends AppCompatActivity {

    private int deviceId = -1;
    private String deviceName = "Pudełko";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_schedule);

        ImageButton backButton = findViewById(R.id.buttonBack);
        TextView headerTitle = findViewById(R.id.device_name_header);
        RecyclerView recyclerView = findViewById(R.id.weekdays_recycler_view);
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
                        String deviceJson = new com.google.gson.Gson().toJson(details);

                        java.util.List<String> weekdays = java.util.Arrays.asList(
                                "Poniedziałek","Wtorek","Środa","Czwartek","Piątek","Sobota","Niedziela"
                        );
                        WeekdayAdapter adapter = new WeekdayAdapter(
                                DeviceScheduleActivity.this, weekdays, deviceName, deviceId, deviceJson
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
}