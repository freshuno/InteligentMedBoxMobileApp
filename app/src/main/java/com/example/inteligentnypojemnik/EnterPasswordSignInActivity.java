package com.example.inteligentnypojemnik;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

public class EnterPasswordSignInActivity extends AppCompatActivity {

    private String userEmail;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_enter_password_sign_in);

        sessionManager = new SessionManager(getApplicationContext());

        userEmail = getIntent().getStringExtra("USER_EMAIL");

        EditText passwordField = findViewById(R.id.editTextPasswordSignIn);
        MaterialButton loginButton = findViewById(R.id.buttonLogIn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordField.getText().toString();

                if (userEmail == null || userEmail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(EnterPasswordSignInActivity.this, "Wprowadź e-mail i hasło", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginUser(userEmail, password);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private void loginUser(String email, String password) {

        String username = email.split("@")[0];

        Log.d("API_CALL", "Logowanie (JSON): u=" + username + ", p=" + password);

        LoginRequest request = new LoginRequest(username, password);

        RetrofitClient.getApiService(EnterPasswordSignInActivity.this).loginUser(request).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    String accessToken = response.body().getAccess();
                    sessionManager.saveAuthToken(accessToken);

                    Toast.makeText(EnterPasswordSignInActivity.this, "Zalogowano pomyślnie!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(EnterPasswordSignInActivity.this, RoleSelectionActivity.class);

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    String errorMsg = "Błąd logowania: " + response.code() + " " + response.message();
                    Log.e("API_ERROR", errorMsg);
                    Toast.makeText(EnterPasswordSignInActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("API_FAILURE", "Błąd połączenia: " + t.getMessage());
                Toast.makeText(EnterPasswordSignInActivity.this, "Błąd połączenia: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}