package com.example.inmia;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_register); // lo construiremos después
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
}