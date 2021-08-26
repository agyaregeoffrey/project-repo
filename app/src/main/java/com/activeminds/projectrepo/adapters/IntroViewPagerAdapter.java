package com.activeminds.projectrepo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.activeminds.projectrepo.R;
import com.activeminds.projectrepo.models.IntroScreenModel;

import java.util.List;

public class IntroViewPagerAdapter extends PagerAdapter {
    Context mContext;
    List<IntroScreenModel> mIntroScreenModels;

    public IntroViewPagerAdapter(Context context, List<IntroScreenModel> introScreenModels) {
        mContext = context;
        mIntroScreenModels = introScreenModels;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutScreen = inflater.inflate(R.layout.intro_layout_screen, null);

        ImageView imageSlide = layoutScreen.findViewById(R.id.intro_imageView);
        TextView titleText = layoutScreen.findViewById(R.id.intro_title_text);
        TextView descriptionText = layoutScreen.findViewById(R.id.intro_description_text);

        titleText.setText(mIntroScreenModels.get(position).getTitle());
        descriptionText.setText(mIntroScreenModels.get(position).getDescription());
        imageSlide.setImageResource(mIntroScreenModels.get(position).getImage());

        container.addView(layoutScreen);
        return layoutScreen;
    }

    @Override
    public int getCount() {
        return mIntroScreenModels.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
