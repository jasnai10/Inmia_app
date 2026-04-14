package com.example.inmia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final Context context;

    // Datos de cada página: imagen, título, descripción
    private final int[] images = {
            R.drawable.onboarding1,
            R.drawable.onboarding2,
            R.drawable.onboarding3
    };

    private final String[] titles = {
            "Encuentra tu hogar ideal",
            "Explora propiedades",
            "Conecta con agentes"
    };

    private final String[] descriptions = {
            "Descubre miles de propiedades en venta y alquiler adaptadas a tus necesidades y presupuesto.",
            "Filtra por ubicación, precio y características. Visualiza cada detalle antes de decidir.",
            "Contacta directamente con agentes verificados y agenda visitas desde la app."
    };

    public OnboardingAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_onboarding_page, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.imgOnboarding.setImageResource(images[position]);
        holder.tvTitle.setText(titles[position]);
        holder.tvDescription.setText(descriptions[position]);
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView imgOnboarding;
        TextView tvTitle, tvDescription;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgOnboarding = itemView.findViewById(R.id.imgOnboarding);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}