package com.example.inteligentnypojemnik;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddDeviceActivity extends AppCompatActivity {

    private EditText inputPatientName, inputSerial, inputLabel;
    private MaterialButton generateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_device);

        ImageButton backButton = findViewById(R.id.buttonBack);
        generateButton = findViewById(R.id.button_generate_key);
        inputPatientName = findViewById(R.id.input_patient_name);
        inputSerial = findViewById(R.id.input_serial_number);
        inputLabel = findViewById(R.id.input_label);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairNewDevice();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void pairNewDevice() {
        String seniorUsername = inputPatientName.getText().toString();
        String physicalDeviceId = inputSerial.getText().toString();
        String label = inputLabel.getText().toString();

        if (seniorUsername.isEmpty() || physicalDeviceId.isEmpty() || label.isEmpty()) {
            Toast.makeText(this, "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show();
            return;
        }

        PairDeviceRequest request = new PairDeviceRequest(physicalDeviceId, seniorUsername, label);

        RetrofitClient.getApiService(this).pairDevice(request).enqueue(new Callback<MyDevice>() {
            @Override
            public void onResponse(Call<MyDevice> call, Response<MyDevice> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AddDeviceActivity.this, "Urządzenie dodane!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = "Błąd parowania: " + response.code() + " " + response.message();
                    Log.e("API_ERROR", errorMsg);
                    Toast.makeText(AddDeviceActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MyDevice> call, Throwable t) {
                Log.e("API_FAILURE", "Błąd połączenia: " + t.getMessage());
                Toast.makeText(AddDeviceActivity.this, "Błąd połączenia: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}