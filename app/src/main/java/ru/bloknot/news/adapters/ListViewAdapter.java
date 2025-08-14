package ru.bloknot.news.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ru.bloknot.news.R;

public class ListViewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> items;
    private LayoutInflater inflater;

    public ListViewAdapter(Context context, ArrayList<String> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // ViewHolder для оптимизации
    private static class ViewHolder {
        TextView category;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.category = convertView.findViewById(R.id.titleTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String item = items.get(position);
        holder.category.setText(item);

        return convertView;
    }
}
