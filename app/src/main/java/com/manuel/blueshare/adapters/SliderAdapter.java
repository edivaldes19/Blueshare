package com.manuel.blueshare.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.imageview.ShapeableImageView;
import com.manuel.blueshare.R;
import com.manuel.blueshare.activities.PhotoActivity;
import com.manuel.blueshare.models.SliderItem;
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
        if (!TextUtils.isEmpty(sliderItem.getImageUrl())) {
            Picasso.get().load(sliderItem.getImageUrl()).into(viewHolder.imageViewSlider);
            viewHolder.imageViewSlider.setOnClickListener(v -> goToShowPhotoActivity(sliderItem.getImageUrl()));
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