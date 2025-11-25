package com.example.inteligentnypojemnik;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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

public class CreatePasswordActivity extends AppCompatActivity {

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_password);

        userEmail = getIntent().getStringExtra("USER_EMAIL");

        // Pobieranie referencji do pól
        EditText displayNameField = findViewById(R.id.editTextDisplayName);
        EditText passwordField = findViewById(R.id.editTextPassword);
        EditText repeatPasswordField = findViewById(R.id.editTextRepeatPassword);
        MaterialButton createAccountButton = findViewById(R.id.buttonCreateAccount);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName = displayNameField.getText().toString().trim();
                String password = passwordField.getText().toString();
                String repeatPassword = repeatPasswordField.getText().toString();

                // --- WALIDACJA ---

                // 1. Walidacja Nazwy
                if (displayName.isEmpty()) {
                    displayNameField.setError("Nazwa jest wymagana");
                    return;
                }
                if (displayName.length() < 3) {
                    displayNameField.setError("Nazwa musi mieć min. 3 znaki");
                    return;
                }
                // Dozwolone tylko litery, cyfry i spacje (w tym polskie znaki)
                if (!displayName.matches("[a-zA-Z0-9ąęćłńóśźżĄĘĆŁŃÓŚŹŻ ]+")) {
                    displayNameField.setError("Nazwa zawiera niedozwolone znaki");
                    return;
                }

                // 2. Walidacja Emaila (na wypadek błędu przy przekazywaniu)
                if (userEmail == null || userEmail.isEmpty() || !userEmail.contains("@")) {
                    Toast.makeText(CreatePasswordActivity.this, "Błąd: Niepoprawny email!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 3. Walidacja haseł
                if (password.isEmpty()) {
                    passwordField.setError("Hasło jest wymagane");
                    return;
                }
                if (password.length() < 6) { // Przykładowa walidacja długości
                    passwordField.setError("Hasło musi mieć min. 6 znaków");
                    return;
                }
                if (!password.equals(repeatPassword)) {
                    repeatPasswordField.setError("Hasła się nie zgadzają");
                    Toast.makeText(CreatePasswordActivity.this, "Hasła się nie zgadzają!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Jeśli wszystko OK -> rejestracja
                registerUser(displayName, userEmail, password);
            }
        });

        TextView signInLink = findViewById(R.id.textViewSignIn);
        signInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreatePasswordActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void registerUser(String displayName, String email, String password) {

        String username = email.split("@")[0];

        Log.d("API_CALL", "Rejestracja: dn=" + displayName + ", u=" + username + ", e=" + email + ", p=" + password);

        RegisterRequest request = new RegisterRequest(displayName, username, email, password);

        RetrofitClient.getApiService(CreatePasswordActivity.this).registerUser(request).enqueue(new Callback<RegisterResponse>() {

            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    Toast.makeText(CreatePasswordActivity.this, "Rejestracja udana! Zaloguj się.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(CreatePasswordActivity.this, SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    String errorMsg = "Błąd rejestracji: " + response.code();
                    Log.e("API_ERROR", errorMsg);
                    Toast.makeText(CreatePasswordActivity.this, "Błąd: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e("API_FAILURE", "Błąd połączenia: " + t.getMessage());
                Toast.makeText(CreatePasswordActivity.this, "Błąd połączenia: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}