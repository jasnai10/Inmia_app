package com.example.inmia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnNext;
    private TextView tvSkip;
    private LinearLayout layoutIndicators;
    private OnboardingAdapter adapter;

    private static final int TOTAL_PAGES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        tvSkip = findViewById(R.id.tvSkip);
        layoutIndicators = findViewById(R.id.layoutIndicators);

        // Configurar adapter
        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        // Crear los indicadores (dots)
        setupIndicators();
        updateIndicators(0);

        // Listener al cambiar de página
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);

                if (position == TOTAL_PAGES - 1) {
                    // Última página → botón "Empezar"
                    btnNext.setText("Empezar");
                    tvSkip.setVisibility(View.INVISIBLE); // Ocultar "Saltar" en última página
                } else {
                    btnNext.setText("Continuar");
                    tvSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        // Botón Continuar / Empezar
        btnNext.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < TOTAL_PAGES - 1) {
                // Avanzar a siguiente página con animación suave
                viewPager.setCurrentItem(currentItem + 1, true);
            } else {
                // Ir al Login
                goToLogin();
            }
        });

        // Botón Saltar
        tvSkip.setOnClickListener(v -> goToLogin());
    }

    private void goToLogin() {
        Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // No regresar al onboarding con el botón atrás
    }

    private void setupIndicators() {
        ImageView[] indicators = new ImageView[TOTAL_PAGES];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < TOTAL_PAGES; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageDrawable(
                    getDrawable(R.drawable.indicator_inactive)
            );
            indicators[i].setLayoutParams(params);
            layoutIndicators.addView(indicators[i]);
        }
    }

    private void updateIndicators(int currentIndex) {
        int childCount = layoutIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView indicator = (ImageView) layoutIndicators.getChildAt(i);
            if (i == currentIndex) {
                indicator.setImageDrawable(getDrawable(R.drawable.indicator_active));
            } else {
                indicator.setImageDrawable(getDrawable(R.drawable.indicator_inactive));
            }
        }
    }
}