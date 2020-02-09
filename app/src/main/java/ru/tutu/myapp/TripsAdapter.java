package ru.tutu.myapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ru.tutu.myapp.model.Trip;

public class TripsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_TITLE = 0;
    public static final int TYPE_ITEM = 1;

    private List list;

    TripsAdapter() {
        this.list = Collections.emptyList();
    }

    public void setList(List list) {
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_trips, viewGroup, false);
            return new ItemViewHolder(view);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.title_list_trips, viewGroup, false);
            return new TitleViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ItemViewHolder) {
            ((ItemViewHolder) viewHolder).bind((Trip) list.get(position));
        } else if (viewHolder instanceof TitleViewHolder) {
            ((TitleViewHolder) viewHolder).bind((String) list.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == 2) {
            return TYPE_TITLE;
        } else {
            return TYPE_ITEM;
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView departureTimeTextView;
        private TextView arrivalTimeTextView;
        private TextView travelTimeTextView;
        private TextView trainNumberTextView;
        private TextView nameTextView;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            departureTimeTextView = itemView.findViewById(R.id.departureTimeTextView);
            arrivalTimeTextView = itemView.findViewById(R.id.arrivalTimeTextView);
            travelTimeTextView = itemView.findViewById(R.id.travelTimeTextView);
            trainNumberTextView = itemView.findViewById(R.id.trainNumberTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
        }

        void bind(Trip trip) {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String departureTime = format.format(trip.getDepartureTime());
            String arrivalTime = format.format(trip.getArrivalTime());

            int travelTimeInSeconds = trip.getTravelTimeInSeconds();
            int hours = travelTimeInSeconds / 60 / 60;
            int seconds = travelTimeInSeconds / 60 % 60;
            String travelTime = hours + "ч " + seconds + "м";

            departureTimeTextView.setText(departureTime);
            arrivalTimeTextView.setText(arrivalTime);
            travelTimeTextView.setText(travelTime);
            trainNumberTextView.setText(trip.getTrainNumber());
            String name = trip.getName();
            nameTextView.setText(name != null && !name.equals("null") ? name : "");
        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        private TextView title;

        TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sectionTitle);
        }

        void bind(String title) {
            this.title.setText(title);
        }
    }
}
