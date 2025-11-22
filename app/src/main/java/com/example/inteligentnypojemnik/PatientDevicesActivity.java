package com.example.inteligentnypojemnik;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView; // Dodano
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class PatientDevicesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient_devices);

        ImageButton backButton = findViewById(R.id.buttonBack);
        TextView headerName = findViewById(R.id.patient_name_header);
        RecyclerView devicesRecyclerView = findViewById(R.id.devices_recycler_view);

        String patientName = getIntent().getStringExtra("PATIENT_NAME");
        String devicesJson = getIntent().getStringExtra("DEVICES_JSON");

        if (patientName != null) {
            headerName.setText(patientName);
        }

        List<Device> deviceList = new ArrayList<>();

        if (devicesJson != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<MyDevice>>(){}.getType();
            List<MyDevice> myDevices = gson.fromJson(devicesJson, listType);

            for (MyDevice md : myDevices) {
                deviceList.add(new Device(
                        md.getId(),
                        md.getLabel(),
                        md.getSeniorDisplayName(),
                        "",
                        "",
                        md.isActive() ? "Aktywne" : "Nieaktywne"
                ));
            }
        } else {
            Toast.makeText(this, "Brak urządzeń", Toast.LENGTH_SHORT).show();
        }

        DeviceAdapter adapter = new DeviceAdapter(this, deviceList, false);

        devicesRecyclerView.setAdapter(adapter);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        backButton.setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}