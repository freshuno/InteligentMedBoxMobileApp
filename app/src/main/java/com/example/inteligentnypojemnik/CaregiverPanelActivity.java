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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CaregiverPanelActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PatientAdapter patientAdapter;
    private DeviceAdapter deviceAdapter;
    private List<Patient> patientList;
    private List<Device> deviceList;
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

        preparePatientList();

        deviceList = new ArrayList<>();

        patientAdapter = new PatientAdapter(this, patientList);
        deviceAdapter = new DeviceAdapter(this, deviceList, true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(patientAdapter);
        addDeviceButton.setVisibility(View.GONE);

        toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    if (checkedId == R.id.toggle_patients) {
                        showPatientView();
                    } else if (checkedId == R.id.toggle_devices) {
                        showDeviceView();
                    }
                }
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionManager.clearSession();
                Intent intent = new Intent(CaregiverPanelActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        addDeviceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CaregiverPanelActivity.this, AddDeviceActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (toggleGroup.getCheckedButtonId() == R.id.toggle_devices) {
            fetchMyDevices();
        }
    }

    private void preparePatientList() {
        patientList = new ArrayList<>();
        patientList.add(new Patient("AK", "Anna Kowalska", "2 urządzenia"));
        patientList.add(new Patient("JN", "Jan Nowak", "1 urządzenie"));
        patientList.add(new Patient("MW", "Maria Wiśniewska", "3 urządzenia"));
    }

    private void fetchMyDevices() {
        RetrofitClient.getApiService(this).getMyDevices().enqueue(new Callback<MyDevicesResponse>() {
            @Override
            public void onResponse(Call<MyDevicesResponse> call, Response<MyDevicesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    deviceList.clear();

                    for (MyDevice apiDevice : response.body().getDevices()) {
                        deviceList.add(new Device(
                                apiDevice.getId(),
                                apiDevice.getLabel(),
                                apiDevice.getSeniorDisplayName(),
                                apiDevice.getSeniorUsername(),
                                "",
                                ""
                        ));
                    }

                    deviceAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(CaregiverPanelActivity.this, "Nie udało się pobrać urządzeń", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MyDevicesResponse> call, Throwable t) {
                Log.e("API_FAILURE", "Błąd pobierania urządzeń: " + t.getMessage());
                Toast.makeText(CaregiverPanelActivity.this, "Błąd połączenia: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPatientView() {
        recyclerView.setAdapter(patientAdapter);
        addDeviceButton.setVisibility(View.GONE);
    }

    private void showDeviceView() {
        recyclerView.setAdapter(deviceAdapter);
        addDeviceButton.setVisibility(View.VISIBLE);
        fetchMyDevices();
    }
}