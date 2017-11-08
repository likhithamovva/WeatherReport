package com.learnings.weatherreport;

import android.app.Application;

import com.learnings.weatherreport.Model.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Likitha on 05/11/17.
 */

public class WeatherApplication extends Application {

    private List<Location> mAddedLocations;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void addALocation(Location mLocation) {
        if (mAddedLocations == null) {
            mAddedLocations = new ArrayList<>();
        }
        for (int i = 0; i < mAddedLocations.size(); i++) {
            if (mAddedLocations.get(i).locationName.equalsIgnoreCase(mLocation.locationName)) {
                return;
            }
        }
        mAddedLocations.add(mLocation);
    }

    public List<Location> getAddedLocations() {
        return mAddedLocations;
    }
}
