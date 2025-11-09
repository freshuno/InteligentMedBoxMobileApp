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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_schedule);

        ImageButton backButton = findViewById(R.id.buttonBack);
        TextView headerTitle = findViewById(R.id.device_name_header);
        RecyclerView recyclerView = findViewById(R.id.weekdays_recycler_view);
        MaterialButton statsButton = findViewById(R.id.button_view_statistics);

        String deviceName = getIntent().getStringExtra("DEVICE_NAME");
        if (deviceName != null) {
            headerTitle.setText(deviceName);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceScheduleActivity.this, DeviceStatisticsActivity.class);
                startActivity(intent);
            }
        });

        List<String> weekdays = new ArrayList<>();
        weekdays.add("Poniedziałek");
        weekdays.add("Wtorek");
        weekdays.add("Środa");
        weekdays.add("Czwartek");
        weekdays.add("Piątek");
        weekdays.add("Sobota");
        weekdays.add("Niedziela");

        WeekdayAdapter adapter = new WeekdayAdapter(this, weekdays, deviceName);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}