package com.example.smartshutter;

import android.graphics.drawable.PictureDrawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.engine.GlideException;

public class SvgSoftwareLayerSetter implements RequestListener<PictureDrawable> {

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<PictureDrawable> target, boolean isFirstResource) {
        Log.e("SvgSoftwareLayerSetter", "SVG Load Failed", e);
        return false; // 기본 error 이미지를 표시
    }

    @Override
    public boolean onResourceReady(PictureDrawable resource, Object model, Target<PictureDrawable> target, DataSource dataSource, boolean isFirstResource) {
        if (target instanceof ImageViewTarget) {
            ImageView imageView = ((ImageViewTarget<PictureDrawable>) target).getView();
            imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // 소프트웨어 렌더링 설정
        }
        return false; // 기본 이미지 처리를 계속 진행
    }
}