package com.example.smartshutter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.time.Instant;
import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultViewHolder> {
    private List<ResultItem> results;

    public ResultAdapter(List<ResultItem> results) {
        this.results = results;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_item, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        ResultItem item = results.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    class ResultViewHolder extends RecyclerView.ViewHolder {
        ImageView resultImage;
        TextView resultDescription;
        ImageView engineIcon;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            resultImage = itemView.findViewById(R.id.resultImage);
            resultDescription = itemView.findViewById(R.id.resultDescription);
            engineIcon = itemView.findViewById(R.id.engineIcon);
        }

        public void bind(ResultItem item) {
            // Glide 또는 Picasso로 이미지 로드
            Glide.with(itemView.getContext()).load(item.getImageUrl()).into(resultImage);
            resultDescription.setText(item.getDescription());
            engineIcon.setImageResource(item.getEngineIcon());
        }
    }
}
