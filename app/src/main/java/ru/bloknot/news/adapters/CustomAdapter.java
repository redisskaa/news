package ru.bloknot.news.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import ru.bloknot.news.R;
import ru.bloknot.news.models.CardNews;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private final List<CardNews> dataList;
    private final Context context;

    public CustomAdapter(Context context, List<CardNews> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_test, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardNews data = dataList.get(position);
        holder.title.setText(data.getTitle());
        holder.category.setText(data.getCategory());
        holder.time.setText(data.getTime());
        holder.description.setText(data.getDescription());
        Picasso.get()
                .load(data.getImageUrl())
                .fit()
                .placeholder(R.drawable.lazy)
                .error(R.drawable.error)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, time, description;
        ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleText);
            category = itemView.findViewById(R.id.catText);
            time = itemView.findViewById(R.id.timeText);
            description = itemView.findViewById(R.id.descriptionText);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}