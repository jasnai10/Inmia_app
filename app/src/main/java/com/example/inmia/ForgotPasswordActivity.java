package com.example.inmia;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout tilForgotEmail;
    private TextInputEditText etForgotEmail;
    private MaterialButton btnSendReset, btnBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_forgot_password);

        tilForgotEmail = findViewById(R.id.tilForgotEmail);
        etForgotEmail  = findViewById(R.id.etForgotEmail);
        btnSendReset   = findViewById(R.id.btnSendReset);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnSendReset.setOnClickListener(v -> {
            String email = etForgotEmail.getText() != null
                    ? etForgotEmail.getText().toString().trim() : "";

            if (TextUtils.isEmpty(email)) {
                tilForgotEmail.setError("Ingresa tu correo");
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilForgotEmail.setError("Correo no válido");
            } else {
                tilForgotEmail.setError(null);
                // TODO: lógica de envío real
                Snackbar.make(v,
                        "Enlace enviado a " + email,
                        Snackbar.LENGTH_LONG).show();
            }
        });

        // Volver al Login
        btnBackToLogin.setOnClickListener(v -> finish());
    }
}