package com.arso.tabandtoolbar;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomHistoryList<T> extends ArrayAdapter<T> {

    private Context context;
    ArrayList<HashMap<String, String>> historyArray;

    private static class Holder{
        TextView url;
    }

    public CustomHistoryList(Context context, int resource, int textViewResourceId, List<T> objects, ArrayList<HashMap<String, String>> historyArray) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
        this.historyArray = historyArray;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder=new Holder();
        View rowView;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate(R.layout.history_item, null);
        holder.url=(TextView) rowView.findViewById(R.id.textViewURL);
        holder.url.setText(historyArray.get(position).get("url"));

        if (Integer.valueOf(historyArray.get(position).get("status")) == 1){
            holder.url.setBackgroundColor(Color.GREEN);
        }else if (Integer.valueOf(historyArray.get(position).get("status")) == 2){
            holder.url.setBackgroundColor(Color.RED);
        }else
            holder.url.setBackgroundColor(Color.GRAY);

        return rowView;
    }

}
