package com.example.smartshutter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.caverock.androidsvg.SVG;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultViewHolder> {
    private List<ResultItem> resultItems;

    public ResultAdapter(List<ResultItem> resultItems) {
        this.resultItems = resultItems;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.result_item, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        ResultItem item = resultItems.get(position);
        holder.descriptionTextView.setText(item.getDescription());
        holder.engineIconImageView.setImageResource(item.getEngineIcon());

        // 이미지 로드
        if (item.getImageUrl() != null) {
            if (item.getImageUrl().endsWith(".svg")) {
                // SVG 파일 처리
                GlideApp.with(holder.imageView.getContext())
                        .as(PictureDrawable.class) // SVG를 PictureDrawable로 처리
                        .placeholder(android.R.drawable.ic_menu_gallery) // 로딩 중 표시할 이미지
                        .error(android.R.drawable.ic_delete) // 실패 시 표시할 이미지
                        .listener(new SvgSoftwareLayerSetter()) // SVG 처리 Listener
                        .load(item.getImageUrl())
                        .into(holder.imageView);
            } else {
                // 일반 이미지 처리 (JPG, PNG, WEBP 등)
                GlideApp.with(holder.imageView.getContext())
                        .load(item.getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery) // 로딩 중 표시할 이미지
                        .error(android.R.drawable.ic_delete) // 실패 시 표시할 이미지
                        .into(holder.imageView);
            }
        } else {
            // URL이 없는 경우 기본 이미지 표시
            holder.imageView.setImageResource(android.R.drawable.ic_delete);
        }
    }


    @Override
    public int getItemCount() {
        return resultItems.size();
    }

    public static class ResultViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView descriptionTextView;
        ImageView engineIconImageView;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.result_image);
            descriptionTextView = itemView.findViewById(R.id.result_description);
            engineIconImageView = itemView.findViewById(R.id.result_engine_icon);
        }
    }

    private Bitmap loadSvgToBitmap(String url, int width, int height) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream inputStream = connection.getInputStream();

            SVG svg = SVG.getFromInputStream(inputStream);
            Picture picture = svg.renderToPicture(width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            canvas.drawPicture(picture);
            return bitmap;
        } catch (Exception e) {
            Log.e("ResultAdapter", "Error loading SVG", e);
            return null;
        }
    }
}
