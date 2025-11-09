package com.example.inteligentnypojemnik;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PatientDevicesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient_devices);

        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView devicesRecyclerView = findViewById(R.id.devices_recycler_view);

        List<Device> deviceList = new ArrayList<>();
        deviceList.add(new Device("Pudełko1", "", "Dzisiaj, 18:00", "4", "Uzupełniono 3 dni temu"));
        deviceList.add(new Device("Pudełko2", "", "Dzisiaj, 20:00", "2", "Uzupełniono 1 dzień temu"));

        DeviceAdapter adapter = new DeviceAdapter(this, deviceList, false);


        devicesRecyclerView.setAdapter(adapter);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}