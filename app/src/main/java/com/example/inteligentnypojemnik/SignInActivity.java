package com.example.inteligentnypojemnik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(getApplicationContext());

        if (sessionManager.getAuthToken() != null && !sessionManager.getAuthToken().isEmpty()) {
            Intent intent = new Intent(SignInActivity.this, RoleSelectionActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        // ------------------------------------------------------------------

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        EditText emailField = findViewById(R.id.editTextEmail);
        Button continueSignInButton = findViewById(R.id.buttonContinueSignIn);

        continueSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();

                Intent intent = new Intent(SignInActivity.this, EnterPasswordSignInActivity.class);
                intent.putExtra("USER_EMAIL", email);
                startActivity(intent);
            }
        });

        TextView createAccountLink = findViewById(R.id.textViewCreateAccount);
        createAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}