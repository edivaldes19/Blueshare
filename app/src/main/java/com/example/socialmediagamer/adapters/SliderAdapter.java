package com.example.socialmediagamer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.socialmediagamer.R;
import com.example.socialmediagamer.activities.PhotoActivity;
import com.example.socialmediagamer.models.SliderItem;
import com.google.android.material.imageview.ShapeableImageView;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterVH> {
    Context context;
    private final List<SliderItem> mSliderItems;

    public SliderAdapter(Context context, List<SliderItem> sliderItems) {
        this.context = context;
        mSliderItems = sliderItems;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        @SuppressLint("InflateParams") View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_layout_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {
        SliderItem sliderItem = mSliderItems.get(position);
        if (sliderItem.getImageUrl() != null) {
            if (!sliderItem.getImageUrl().isEmpty()) {
                Picasso.get().load(sliderItem.getImageUrl()).into(viewHolder.imageViewSlider);
                viewHolder.imageViewSlider.setOnClickListener(v -> goToShowPhotoActivity(sliderItem.getImageUrl()));
            }
        }
    }

    @Override
    public int getCount() {
        return mSliderItems.size();
    }

    private void goToShowPhotoActivity(String url) {
        Intent intent = new Intent(context, PhotoActivity.class);
        intent.putExtra("urlImagePost", url);
        context.startActivity(intent);
    }

    public static class SliderAdapterVH extends SliderViewAdapter.ViewHolder {
        View itemView;
        ShapeableImageView imageViewSlider;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewSlider = itemView.findViewById(R.id.imageViewSlider);
            this.itemView = itemView;
        }
    }
}