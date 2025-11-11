package com.example.inteligentnypojemnik;

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
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class CompartmentScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compartment_schedule);

        ImageButton backButton = findViewById(R.id.buttonBack);
        TextView headerTitle = findViewById(R.id.header_title);
        RecyclerView recyclerView = findViewById(R.id.compartments_recycler_view);

        String deviceName = getIntent().getStringExtra("DEVICE_NAME");
        String dayName = getIntent().getStringExtra("DAY_NAME");
        String dayKey  = getIntent().getStringExtra("DAY_KEY");
        String deviceJson = getIntent().getStringExtra("DEVICE_JSON");

        if (deviceName != null && dayName != null) {
            headerTitle.setText(deviceName + " - " + dayName);
        }

        backButton.setOnClickListener(v -> finish());

        // 1) Parse JSON → obiekt
        DeviceDetailsResponse details =
                new com.google.gson.Gson().fromJson(deviceJson, DeviceDetailsResponse.class);
        DeviceDetailsResponse.DayConfig day = details.configuration.get(dayKey);

        // 2) Zbuduj listę przegród
        List<Compartment> compartments = new ArrayList<>();
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

            // zapisujemy JSON kontenera w modelu, żeby klik przejścia mógł go przekazać dalej
            Compartment comp = new Compartment(title, time, count);
            comp.setExtraJson(new com.google.gson.Gson().toJson(c)); // dodaj pole extraJson w klasie Compartment (String)
            compartments.add(comp);
        }

        // 3) Adapter i klik → szczegóły przegrody
        CompartmentAdapter adapter = new CompartmentAdapter(this, compartments);
        adapter.setOnItemClickListener(comp -> {
            Intent intent = new Intent(this, CompartmentDetailsActivity.class);
            intent.putExtra("COMPARTMENT_NAME", comp.getName());
            intent.putExtra("TIME", comp.getTime());
            intent.putExtra("MED_COUNT", comp.getMedCount());
            intent.putExtra("CONTAINER_JSON", comp.getExtraJson()); // to przekażemy do listy leków
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });
    }
}