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
import java.net.MalformedURLException;
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

        // 이미지 URL이 유효한지 검사하고, 유효할 경우만 Glide로 로드
        if (isValidImageUrl(item.getImageUrl()) && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.imageView.getContext())
                    .load(item.getImageUrl()) // 이미지 URL 로드
                    .placeholder(android.R.drawable.ic_menu_gallery) // 기본 로딩 이미지
                    .error(android.R.drawable.ic_menu_report_image) // 오류 발생 시 기본 이미지
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image); // 기본 이미지 설정
        }
    }

    private boolean isValidImageUrl(String url) {
        try {
            new URL(url);  // URL이 유효한지 확인
            return true;
        } catch (MalformedURLException e) {
            Log.e("Image URL Error", "Invalid URL: " + url);
            return false;
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
            // URL이 유효한지 검사
            if (url == null || !url.startsWith("http://") && !url.startsWith("https://")) {
                Log.e("ResultAdapter", "Invalid URL: " + url);
                return null; // 잘못된 URL은 처리하지 않음
            }

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
            return null; // 오류 발생 시 null 반환
        }
    }



}
