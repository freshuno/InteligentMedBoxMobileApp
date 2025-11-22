package com.example.inteligentnypojemnik;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CaregiverPanelActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PatientAdapter patientAdapter;
    private DeviceAdapter deviceAdapter;
    private List<Patient> patientList;
    private List<Device> deviceList; // Lista do widoku "Wszystkie urządzenia"
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton addDeviceButton;
    private ImageButton logoutButton;
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

        // Inicjalizacja pustych list
        patientList = new ArrayList<>();
        deviceList = new ArrayList<>();

        patientAdapter = new PatientAdapter(this, patientList);
        deviceAdapter = new DeviceAdapter(this, deviceList, true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Domyślnie ładujemy widok pacjentów
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

        logoutButton.setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(CaregiverPanelActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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

        // Pobieramy dane od razu przy starcie, żeby wypełnić obie listy
        fetchMyDevices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Odświeżamy dane przy powrocie
        fetchMyDevices();
    }

    private void fetchMyDevices() {
        RetrofitClient.getApiService(this).getMyDevices().enqueue(new Callback<MyDevicesResponse>() {
            @Override
            public void onResponse(Call<MyDevicesResponse> call, Response<MyDevicesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<MyDevice> rawDevices = response.body().getDevices();

                    // 1. Aktualizacja listy WSZYSTKICH urządzeń (Widok urządzeń)
                    deviceList.clear();
                    for (MyDevice apiDevice : rawDevices) {
                        deviceList.add(new Device(
                                apiDevice.getId(),
                                apiDevice.getLabel(),
                                apiDevice.getSeniorDisplayName(),
                                apiDevice.getSeniorUsername(), // Tutaj wstawiamy username jako info
                                "", // MedCount
                                ""  // Status
                        ));
                    }

                    // 2. Grupowanie urządzeń po użytkowniku (Widok podopiecznych)
                    Map<String, List<MyDevice>> groupedDevices = new HashMap<>();
                    Map<String, String> userDisplayNames = new HashMap<>();

                    for (MyDevice d : rawDevices) {
                        String username = d.getSeniorUsername();
                        if (!groupedDevices.containsKey(username)) {
                            groupedDevices.put(username, new ArrayList<>());
                            // Zapamiętujemy "ładną nazwę" dla tego username
                            userDisplayNames.put(username, d.getSeniorDisplayName());
                        }
                        groupedDevices.get(username).add(d);
                    }

                    // 3. Tworzenie obiektów Patient na podstawie grupy
                    patientList.clear();
                    for (Map.Entry<String, List<MyDevice>> entry : groupedDevices.entrySet()) {
                        String username = entry.getKey();
                        List<MyDevice> userDevices = entry.getValue();
                        String displayName = userDisplayNames.get(username);
                        if (displayName == null || displayName.isEmpty()) displayName = username;

                        String initials = getInitials(displayName);
                        String countText = userDevices.size() == 1 ? "1 urządzenie" : userDevices.size() + " urządzenia";

                        // Dodajemy pacjenta z jego prywatną listą urządzeń
                        patientList.add(new Patient(initials, displayName, countText, userDevices));
                    }

                    // Odświeżamy widok w zależności od tego, co jest wybrane
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

    // Pomocnicza metoda do inicjałów
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