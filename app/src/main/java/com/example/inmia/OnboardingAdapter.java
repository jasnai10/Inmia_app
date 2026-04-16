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

    // Imágenes — estas se quedan igual (no son strings)
    private final int[] images = {
            R.drawable.onboarding1,
            R.drawable.onboarding2,
            R.drawable.onboarding3
    };

    // Títulos y descripciones — ahora se cargan desde strings.xml
    private final String[] titles;
    private final String[] descriptions;

    public OnboardingAdapter(Context context) {
        this.context = context;

        // Se inicializan en el constructor usando context.getString()
        titles = new String[]{
                context.getString(R.string.onboarding_title_1),
                context.getString(R.string.onboarding_title_2),
                context.getString(R.string.onboarding_title_3)
        };

        descriptions = new String[]{
                context.getString(R.string.onboarding_desc_1),
                context.getString(R.string.onboarding_desc_2),
                context.getString(R.string.onboarding_desc_3)
        };
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
            tvTitle       = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}