package com.tamarin.nextstep;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

public class OnboardingActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "nextstep_prefs";
    public static final String KEY_ONBOARDING_DONE = "onboarding_done";

    private ViewPager2 viewPager;
    private LinearLayout dotsContainer;
    private MaterialButton btnNext;
    private MaterialButton btnSkip;

    private static final int[] ICONS = {
            R.drawable.ic_nav_home,
            R.drawable.ic_nav_reports,
            R.drawable.ic_nav_chatbot
    };
    private static final int[] TITLES = {
            R.string.onboarding_title_1,
            R.string.onboarding_title_2,
            R.string.onboarding_title_3
    };
    private static final int[] DESCS = {
            R.string.onboarding_desc_1,
            R.string.onboarding_desc_2,
            R.string.onboarding_desc_3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        dotsContainer = findViewById(R.id.dotsContainer);
        btnNext = findViewById(R.id.btnOnboardingNext);
        btnSkip = findViewById(R.id.btnOnboardingSkip);

        viewPager.setAdapter(new OnboardingPagerAdapter());
        buildDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                buildDots(position);
                if (position == TITLES.length - 1) {
                    btnNext.setText(R.string.onboarding_start);
                    btnSkip.setVisibility(View.GONE);
                } else {
                    btnNext.setText(R.string.onboarding_next);
                    btnSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < TITLES.length - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void finishOnboarding() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void buildDots(int activeIndex) {
        dotsContainer.removeAllViews();
        for (int i = 0; i < TITLES.length; i++) {
            View dot = new View(this);
            int size = (int) (8 * getResources().getDisplayMetrics().density);
            int margin = (int) (4 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == activeIndex
                    ? R.drawable.dot_active
                    : R.drawable.dot_inactive);
            dotsContainer.addView(dot);
        }
    }

    private class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.PageViewHolder> {

        @NonNull
        @Override
        public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_onboarding_page, parent, false);
            return new PageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
            holder.icon.setImageResource(ICONS[position]);
            holder.title.setText(TITLES[position]);
            holder.desc.setText(DESCS[position]);
        }

        @Override
        public int getItemCount() {
            return TITLES.length;
        }

        class PageViewHolder extends RecyclerView.ViewHolder {
            ImageView icon;
            TextView title, desc;

            PageViewHolder(View v) {
                super(v);
                icon = v.findViewById(R.id.ivOnboardingIcon);
                title = v.findViewById(R.id.tvOnboardingTitle);
                desc = v.findViewById(R.id.tvOnboardingDesc);
            }
        }
    }
}
