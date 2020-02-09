package ru.tutu.myapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import ru.tutu.myapp.model.Trip;

import static ru.tutu.myapp.App.CHANNEL_ID;

public class MyService extends Service {

    static final int SERVICE_ID = 1;
    static final String ACTION_GET_TRIPS = "ru.tutu.myapp.App.ACTION_GET_TRIPS";
    static final String ACTION_RESPONSE = "ru.tutu.myapp.App.ACTION_RESPONSE";
    static final String RESULT_OK = "ru.tutu.myapp.App.RESULT_OK";
    static final String RESULT_FAILURE = "ru.tutu.myapp.App.RESULT_FAILURE";

    static final String EXTRA_RESULT_CODE = "ru.tutu.myapp.App.EXTRA_RESULT_CODE";
    static final String EXTRA_CLOSEST_TRIP = "ru.tutu.myapp.App.EXTRA_CLOSEST_TRIP";
    static final String EXTRA_TRIPS = "ru.tutu.myapp.App.EXTRA_TRIPS";
    static final String EXTRA_ERROR_MESSAGE = "ru.tutu.myapp.App.EXTRA_ERROR_MESSAGE";

    private static final long REFRESH_INTERVAL_FIVE_MINUTES = 5 * 60 * 1000;
    private static final String URL = "https://suggest.travelpayouts.com/search?service=tutu_trains&term=2006004&term2=2004001";

    private final Timer timer;
    private final Handler handler;

    private String jsonData;

    public MyService() {
        timer = new Timer();
        handler = new Handler();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buildNotification();
        }
        scheduleRefreshDataTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_GET_TRIPS.equals(intent.getAction())) {
            sendResponse();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(getString(R.string.service_is_running))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(SERVICE_ID, notification);
    }

    private void scheduleRefreshDataTask() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refreshData();
            }
        }, 0, REFRESH_INTERVAL_FIVE_MINUTES);
    }

    private void refreshData() {
        HttpURLConnection connection = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;

        try {
            URL url = new URL(URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept-Language", "*");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                is = connection.getInputStream();
                bos = new ByteArrayOutputStream();

                int read;
                while ((read = is.read()) != -1) {
                    bos.write(read);
                }
                byte[] result = bos.toByteArray();
                is.close();
                bos.close();

                jsonData = new String(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendResponse() {
        final Intent responseIntent = new Intent();
        responseIntent.setAction(ACTION_RESPONSE);
        responseIntent.addCategory(Intent.CATEGORY_DEFAULT);

        if (jsonData != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ArrayList<Trip> trips = parseJson(jsonData);
                        sortTripsByDepartureTime(trips);
                        Trip closestTrip = getClosestTimeTrip(trips);
                        trips.remove(closestTrip);
                        sendSuccess(closestTrip, trips);
                    } catch (JSONException e) {
                        sendFailure(getString(R.string.trips_format_error_message));
                    }
                }

                private void sortTripsByDepartureTime(ArrayList<Trip> trips) {
                    Collections.sort(trips, new Comparator<Trip>() {
                        @Override
                        public int compare(Trip o1, Trip o2) {
                            if (o1.getDepartureTime().getTime() < o2.getDepartureTime().getTime()) {
                                return -1;
                            } else if (o1.getDepartureTime().getTime() > o2.getDepartureTime().getTime()) {
                                return 1;
                            }
                            return 0;
                        }
                    });
                }

                private Trip getClosestTimeTrip(ArrayList<Trip> trips) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                    Calendar calendar = Calendar.getInstance();
                    Date now = calendar.getTime();
                    String nowString = dateFormat.format(now);

                    Trip closestTrip = trips.get(0);
                    for (int i = 1; i < trips.size(); i++) {
                        String departureTimeString = dateFormat.format(trips.get(i).getDepartureTime().getTime());
                        if (departureTimeString.compareTo(nowString) > 0) {
                            closestTrip = trips.get(i);
                            break;
                        }
                    }
                    return closestTrip;
                }

                private void sendSuccess(final Trip closest, final ArrayList<Trip> trips) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            responseIntent.putExtra(EXTRA_RESULT_CODE, RESULT_OK);
                            responseIntent.putExtra(EXTRA_CLOSEST_TRIP, closest);
                            responseIntent.putParcelableArrayListExtra(EXTRA_TRIPS, trips);
                            sendBroadcast(responseIntent);
                        }
                    });
                }

                private void sendFailure(final String errorMessage) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            responseIntent.putExtra(EXTRA_RESULT_CODE, RESULT_FAILURE);
                            responseIntent.putExtra(EXTRA_ERROR_MESSAGE, errorMessage);
                            sendBroadcast(responseIntent);
                        }
                    });
                }
            }).start();
        } else {
            responseIntent.putExtra(EXTRA_RESULT_CODE, RESULT_FAILURE);
            responseIntent.putExtra(EXTRA_ERROR_MESSAGE, getString(R.string.fetch_data_error_message));
            sendBroadcast(responseIntent);
        }
    }

    private ArrayList<Trip> parseJson(String jsonData) throws JSONException {
        ArrayList<Trip> trips = new ArrayList<>();
        JSONObject rootJsonObject = new JSONObject(jsonData);
        JSONArray tripsJsonArray = new JSONArray(rootJsonObject.getString("trips"));

        for (int i = 0; i < tripsJsonArray.length(); i++) {
            JSONObject tripJsonObject = tripsJsonArray.getJSONObject(i);

            String name = tripJsonObject.getString("name");
            String departureStation = tripJsonObject.getString("departureStation");
            String arrivalStation = tripJsonObject.getString("arrivalStation");
            String runDepartureStation = tripJsonObject.getString("runDepartureStation");
            String runArrivalStation = tripJsonObject.getString("runArrivalStation");
            String departureTimeString = tripJsonObject.getString("departureTime");
            String arrivalTimeString = tripJsonObject.getString("arrivalTime");
            String trainNumber = tripJsonObject.getString("trainNumber");
            int travelTimeInSeconds = Integer.parseInt(tripJsonObject.getString("travelTimeInSeconds"));
            boolean firm = tripJsonObject.getBoolean("firm");

            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date departureTime = null;
            Date arrivalTime = null;
            try {
                departureTime = format.parse(departureTimeString);
                arrivalTime = format.parse(arrivalTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Trip trip = new Trip(name, departureStation, arrivalStation,
                    runDepartureStation, runArrivalStation, departureTime,
                    arrivalTime, trainNumber, travelTimeInSeconds, firm);

            trips.add(trip);
        }
        return trips;
    }
}
