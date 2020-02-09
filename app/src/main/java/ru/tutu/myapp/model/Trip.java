package ru.tutu.myapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Trip implements Parcelable {
    private String name;
    private String departureStation;
    private String arrivalStation;
    private String runDepartureStation;
    private String runArrivalStation;
    private Date departureTime;
    private Date arrivalTime;
    private String trainNumber;
    private int travelTimeInSeconds;
    private boolean firm;

    public String getName() {
        return name;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public int getTravelTimeInSeconds() {
        return travelTimeInSeconds;
    }

    public Trip(
            String name,
            String departureStation,
            String arrivalStation,
            String runDepartureStation,
            String runArrivalStation,
            Date departureTime,
            Date arrivalTime,
            String trainNumber,
            int travelTimeInSeconds,
            boolean firm
    ) {
        this.name = name;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.runDepartureStation = runDepartureStation;
        this.runArrivalStation = runArrivalStation;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.trainNumber = trainNumber;
        this.travelTimeInSeconds = travelTimeInSeconds;
        this.firm = firm;
    }

    protected Trip(Parcel in) {
        name = in.readString();
        departureStation = in.readString();
        arrivalStation = in.readString();
        runDepartureStation = in.readString();
        runArrivalStation = in.readString();
        departureTime = new Date(in.readLong());
        arrivalTime = new Date(in.readLong());
        trainNumber = in.readString();
        travelTimeInSeconds = in.readInt();
        firm = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(departureStation);
        dest.writeString(arrivalStation);
        dest.writeString(runDepartureStation);
        dest.writeString(runArrivalStation);
        dest.writeLong(departureTime.getTime());
        dest.writeLong(arrivalTime.getTime());
        dest.writeString(trainNumber);
        dest.writeInt(travelTimeInSeconds);
        dest.writeByte((byte) (firm ? 1 : 0));
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}