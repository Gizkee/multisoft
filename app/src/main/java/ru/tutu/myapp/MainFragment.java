package ru.tutu.myapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import ru.tutu.myapp.model.Trip;

public class MainFragment extends Fragment {

    private BroadcastReceiver broadcastReceiver;

    private ProgressBar progressBar;
    private Button fetchTripsButton;
    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        fetchTripsButton = view.findViewById(R.id.fetchTripsButton);
        fetchTripsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchTripsButton.setEnabled(false);
                fetchTrips();
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        context.unregisterReceiver(broadcastReceiver);
    }

    private void startMyService(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private void fetchTrips() {
        showLoading();
        Intent requestIntent = new Intent(getActivity(), MyService.class);
        requestIntent.setAction(MyService.ACTION_GET_TRIPS);
        startMyService(requestIntent);
    }

    private void registerBroadcastReceiver() {
        broadcastReceiver = new ResponseBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(MyService.ACTION_RESPONSE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    private void moveToTripsFragment(Bundle args) {
        TripsFragment tripsFragment = new TripsFragment();
        tripsFragment.setArguments(args);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, tripsFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        fetchTripsButton.setEnabled(false);
    }

    private void showErrorMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.INVISIBLE);
        fetchTripsButton.setEnabled(true);
    }

    class ResponseBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (MyService.ACTION_RESPONSE.equals(intent.getAction())) {
                String resultCode = intent.getStringExtra(MyService.EXTRA_RESULT_CODE);
                if (MyService.RESULT_OK.equals(resultCode)) {
                    Trip closestTrip = intent.getParcelableExtra(MyService.EXTRA_CLOSEST_TRIP);
                    ArrayList<Trip> trips = intent.getParcelableArrayListExtra(MyService.EXTRA_TRIPS);
                    if (closestTrip != null && trips != null) {
                        Bundle args = new Bundle();
                        args.putParcelable(MyService.EXTRA_CLOSEST_TRIP, closestTrip);
                        args.putParcelableArrayList(MyService.EXTRA_TRIPS, trips);
                        moveToTripsFragment(args);
                    }
                } else if (MyService.RESULT_FAILURE.equals(resultCode)) {
                    String errorMessage = intent.getStringExtra(MyService.EXTRA_ERROR_MESSAGE);
                    showErrorMessage(errorMessage);
                }
            }
        }
    }
}
