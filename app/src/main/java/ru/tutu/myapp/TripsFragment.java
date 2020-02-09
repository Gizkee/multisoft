package ru.tutu.myapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ru.tutu.myapp.model.Trip;

import static ru.tutu.myapp.MyService.EXTRA_CLOSEST_TRIP;
import static ru.tutu.myapp.MyService.EXTRA_TRIPS;

public class TripsFragment extends Fragment {

    private TripsAdapter tripsAdapter;
    private RecyclerView tripsRecyclerView;

    private Trip closestTrip;
    private ArrayList<Trip> otherTrips;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            closestTrip = args.getParcelable(EXTRA_CLOSEST_TRIP);
            otherTrips = args.getParcelableArrayList(EXTRA_TRIPS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trips, container, false);
        tripsRecyclerView = view.findViewById(R.id.tripsRecyclerView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            closestTrip = savedInstanceState.getParcelable(EXTRA_CLOSEST_TRIP);
            otherTrips = savedInstanceState.getParcelableArrayList(EXTRA_TRIPS);
        }
        setupAdapter();
        showTrips(closestTrip, otherTrips);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_CLOSEST_TRIP, closestTrip);
        outState.putParcelableArrayList(EXTRA_TRIPS, otherTrips);
    }

    private void setupAdapter() {
        tripsAdapter = new TripsAdapter();
        tripsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tripsRecyclerView.setAdapter(tripsAdapter);
    }

    private void showTrips(Trip closestTrip, List<Trip> otherTrips) {
        List listToShow = new ArrayList();
        listToShow.add(getString(R.string.closest_trip));
        listToShow.add(closestTrip);
        listToShow.add(getString(R.string.other_trips));
        listToShow.addAll(otherTrips);
        tripsAdapter.setList(listToShow);
        tripsAdapter.notifyDataSetChanged();
    }
}
