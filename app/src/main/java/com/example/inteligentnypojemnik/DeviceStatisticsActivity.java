package com.example.inteligentnypojemnik;

import android.os.Bundle; // Poprawiony import
import android.view.View; // Poprawiony import
import android.widget.ImageButton; // Poprawiony import
import android.widget.TextView; // Poprawiony import
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DeviceStatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device_statistics);

        RecyclerView recyclerView = findViewById(R.id.activity_recycler_view);
        TextView activityCount = findViewById(R.id.text_activity_count);
        ImageButton backButton = findViewById(R.id.buttonBack);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        List<DeviceActivity> activities = new ArrayList<>();
        activities.add(new DeviceActivity("8:03", "Niepoprawna aktywność pudełka", true));
        activities.add(new DeviceActivity("8:51", "Aktywność pudełka", false));
        activities.add(new DeviceActivity("9:34", "Aktywność pudełka", false));
        activities.add(new DeviceActivity("9:37", "Aktywność pudełka", false));
        activities.add(new DeviceActivity("9:54", "Aktywność pudełka", false));
        activities.add(new DeviceActivity("11:34", "Aktywność pudełka", false));
        activities.add(new DeviceActivity("12:04", "Aktywność pudełka", false));
        activities.add(new DeviceActivity("12:34", "Niepoprawna aktywność pudełka", true));
        activities.add(new DeviceActivity("18:57", "Aktywność pudełka", false));
        activities.add(new DeviceActivity("19:12", "Aktywność pudełka", false));
        activities.add(new DeviceActivity("20:34", "Aktywność pudełka", false));
        activities.add(new DeviceActivity("20:35", "Aktywność pudełka", false));

        activityCount.setText(activities.size() + " aktywności");

        DeviceActivityAdapter adapter = new DeviceActivityAdapter(this, activities);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}