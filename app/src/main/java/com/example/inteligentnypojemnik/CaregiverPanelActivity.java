package com.example.inteligentnypojemnik;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Build;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;

public class CaregiverPanelActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PatientAdapter patientAdapter;
    private DeviceAdapter deviceAdapter;
    private List<Patient> patientList;
    private List<Device> deviceList;
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton addDeviceButton;
    private ImageButton logoutButton;
    private ImageButton settingsButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_caregiver_panel);

        sessionManager = new SessionManager(getApplicationContext());

        recyclerView = findViewById(R.id.caregiver_recycler_view);
        toggleGroup = findViewById(R.id.toggle_group);
        addDeviceButton = findViewById(R.id.add_device_button);
        logoutButton = findViewById(R.id.button_logout);
        settingsButton = findViewById(R.id.button_settings_caregiver);

        patientList = new ArrayList<>();
        deviceList = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        PeriodicWorkRequest checkDosesRequest =
                new PeriodicWorkRequest.Builder(MissedDoseWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "MonitorLekow",
                ExistingPeriodicWorkPolicy.KEEP,
                checkDosesRequest
        );

        patientAdapter = new PatientAdapter(this, patientList);

        deviceAdapter = new DeviceAdapter(this, deviceList, true, (deviceId, deviceName) -> {
            showDeleteConfirmationDialog(deviceId, deviceName);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(patientAdapter);
        addDeviceButton.setVisibility(View.GONE);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.toggle_patients) {
                    showPatientView();
                } else if (checkedId == R.id.toggle_devices) {
                    showDeviceView();
                }
            }
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(CaregiverPanelActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(CaregiverPanelActivity.this)
                    .setTitle("Wylogowanie")
                    .setMessage("Czy na pewno chcesz się wylogować?")
                    .setPositiveButton("Tak", (dialog, which) -> {
                        sessionManager.clearSession();
                        Intent intent = new Intent(CaregiverPanelActivity.this, SignInActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Nie", null)
                    .show();
        });

        addDeviceButton.setOnClickListener(v -> {
            Intent intent = new Intent(CaregiverPanelActivity.this, AddDeviceActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fetchMyDevices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchMyDevices();
    }

    private void showDeleteConfirmationDialog(int deviceId, String deviceName) {
        new AlertDialog.Builder(this)
                .setTitle("Usuń urządzenie")
                .setMessage("Czy na pewno chcesz usunąć pudełko \"" + deviceName + "\"? Tej operacji nie można cofnąć.")
                .setPositiveButton("Usuń", (dialog, which) -> deleteDeviceFromApi(deviceId))
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void deleteDeviceFromApi(int deviceId) {
        RetrofitClient.getApiService(this).deleteDevice(deviceId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CaregiverPanelActivity.this, "Urządzenie usunięte", Toast.LENGTH_SHORT).show();
                    fetchMyDevices();
                } else {
                    Toast.makeText(CaregiverPanelActivity.this, "Błąd usuwania: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CaregiverPanelActivity.this, "Błąd sieci: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMyDevices() {
        RetrofitClient.getApiService(this).getMyDevices().enqueue(new Callback<MyDevicesResponse>() {
            @Override
            public void onResponse(Call<MyDevicesResponse> call, Response<MyDevicesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MyDevice> rawDevices = response.body().getDevices();

                    deviceList.clear();
                    for (MyDevice apiDevice : rawDevices) {
                        deviceList.add(new Device(
                                apiDevice.getId(),
                                apiDevice.getLabel(),
                                apiDevice.getSeniorDisplayName(),
                                apiDevice.getSeniorUsername(),
                                "",
                                ""
                        ));
                    }

                    Map<String, List<MyDevice>> groupedDevices = new HashMap<>();
                    Map<String, String> userDisplayNames = new HashMap<>();

                    for (MyDevice d : rawDevices) {
                        String username = d.getSeniorUsername();
                        if (!groupedDevices.containsKey(username)) {
                            groupedDevices.put(username, new ArrayList<>());
                            userDisplayNames.put(username, d.getSeniorDisplayName());
                        }
                        groupedDevices.get(username).add(d);
                    }

                    patientList.clear();
                    for (Map.Entry<String, List<MyDevice>> entry : groupedDevices.entrySet()) {
                        String username = entry.getKey();
                        List<MyDevice> userDevices = entry.getValue();
                        String displayName = userDisplayNames.get(username);
                        if (displayName == null || displayName.isEmpty()) displayName = username;

                        String initials = getInitials(displayName);
                        String countText = userDevices.size() == 1 ? "1 urządzenie" : userDevices.size() + " urządzenia";

                        patientList.add(new Patient(initials, displayName, countText, userDevices));
                    }

                    if (toggleGroup.getCheckedButtonId() == R.id.toggle_patients) {
                        patientAdapter.notifyDataSetChanged();
                    } else {
                        deviceAdapter.notifyDataSetChanged();
                    }

                } else {
                    Toast.makeText(CaregiverPanelActivity.this, "Nie udało się pobrać urządzeń", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MyDevicesResponse> call, Throwable t) {
                Log.e("API_FAILURE", "Błąd: " + t.getMessage());
            }
        });
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else if (name.length() >= 2) {
            return name.substring(0, 2).toUpperCase();
        }
        return name.substring(0, 1).toUpperCase();
    }

    private void showPatientView() {
        recyclerView.setAdapter(patientAdapter);
        addDeviceButton.setVisibility(View.GONE);
    }

    private void showDeviceView() {
        recyclerView.setAdapter(deviceAdapter);
        addDeviceButton.setVisibility(View.VISIBLE);
    }
}