package com.example.inteligentnypojemnik;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.google.android.material.button.MaterialButton;
import android.app.AlertDialog;

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
    private ImageButton scanQrButton;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() != null) {
                    inputSerial.setText(result.getContents());
                    Toast.makeText(AddDeviceActivity.this, "Zeskanowano: " + result.getContents(), Toast.LENGTH_SHORT).show();
                }
            });

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
        scanQrButton = findViewById(R.id.button_scan_qr);

        backButton.setOnClickListener(v -> finish());

        scanQrButton.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Zeskanuj kod QR z urządzenia");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setBarcodeImageEnabled(false);
            barcodeLauncher.launch(options);
        });

        generateButton.setOnClickListener(v -> pairNewDevice());

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
                    MyDevice device = response.body();
                    String apiKey = device.getApiKey();

                    if (apiKey != null && !apiKey.isEmpty()) {
                        showApiKeyDialog(apiKey);
                    } else {
                        Toast.makeText(AddDeviceActivity.this, "Urządzenie dodane, ale brak API Key w odpowiedzi", Toast.LENGTH_SHORT).show();
                        finish();
                    }

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

    private void showApiKeyDialog(String apiKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sukces! Wygenerowano klucz API");
        builder.setMessage("Oto Twój klucz API:\n\n" + apiKey + "\n\nUWAGA: Ten klucz wyświetla się TYLKO RAZ! Skopiuj go teraz i zapisz w bezpiecznym miejscu.");
        builder.setCancelable(false);

        builder.setNeutralButton("Kopiuj", (dialog, which) -> {
            // Placeholder, onClick zostanie nadpisany
        });

        builder.setPositiveButton("Zamknij", (dialog, which) -> {
            dialog.dismiss();
            finish(); // Zamykamy ekran dodawania
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Nadpisujemy przycisk "Kopiuj", żeby nie zamykał dialogu automatycznie
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("API Key", apiKey);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(AddDeviceActivity.this, "Skopiowano do schowka!", Toast.LENGTH_SHORT).show();
        });
    }
}