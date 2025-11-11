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

        EditText passwordField = findViewById(R.id.editTextPassword);
        EditText repeatPasswordField = findViewById(R.id.editTextRepeatPassword);
        MaterialButton createAccountButton = findViewById(R.id.buttonCreateAccount);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordField.getText().toString();
                String repeatPassword = repeatPasswordField.getText().toString();

                if (userEmail == null || userEmail.isEmpty() || !userEmail.contains("@")) {
                    Toast.makeText(CreatePasswordActivity.this, "Błąd: Niepoprawny email!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty() || !password.equals(repeatPassword)) {
                    Toast.makeText(CreatePasswordActivity.this, "Hasła się nie zgadzają!", Toast.LENGTH_SHORT).show();
                    return;
                }

                registerUser(userEmail, password);
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


    private void registerUser(String email, String password) {

        String username = email.split("@")[0];
        String displayName = "Test User";

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
                    String errorMsg = "Błąd rejestracji: " + response.code() + " " + response.message();
                    Log.e("API_ERROR", errorMsg);
                    Toast.makeText(CreatePasswordActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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