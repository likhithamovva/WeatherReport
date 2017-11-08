package com.learnings.weatherreport.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.learnings.weatherreport.R;
import com.wang.avi.AVLoadingIndicatorView;

public class SplashActivity extends AppCompatActivity {

    private final int LOCAION_SERVICE_REQUEST = 1000;
    private final int LOCATION_PERMISSION_REQUEST = 1001;

    private double currentLatitude;
    private double currentLongitude;

    private AVLoadingIndicatorView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        loadingView = findViewById(R.id.loading);
        searchLocationDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void searchLocationDetails() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isLocationServiceEnabled(getApplicationContext())) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        checkLocationPermission();
                    } else {
                        getCurrentLocation();
                    }
                } else {
                    promptUserToEnableLocation();
                }
            }
        }, 2000);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        //Show Loading Anim
        loadingView.setVisibility(View.VISIBLE);
        //Now we got the permission. Get the location.
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled && isNetworkEnabled) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location gpsProvidedLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkProvidedLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (networkProvidedLocation != null) {
                currentLatitude = networkProvidedLocation.getLatitude();
                currentLongitude = networkProvidedLocation.getLongitude();
                navigateToDetailedScreen(currentLatitude, currentLongitude);
            } else if (gpsProvidedLocation != null) {
                currentLatitude = gpsProvidedLocation.getLatitude();
                currentLongitude = gpsProvidedLocation.getLongitude();
                navigateToDetailedScreen(currentLatitude, currentLongitude);
            } else {
                //Go to current location as we didn't find GPS Location and Network Location.
                //Instead of waiting  for Network listeners we go some location.
                navigateToDetailedScreen(36.12, -97.07);
            }
        } else {
            //In case of no location found, let go with some default latitude and longitude
            navigateToDetailedScreen(36.12, -97.07);
        }
    }

    private void navigateToDetailedScreen(double currentLatitude, double currentLongitude) {
        //Navigate to DetailedWeatherReport Activity.
        Intent mIntent = new Intent(SplashActivity.this, DetailedWeatherReport.class);
//                mIntent.putExtra("Latitude",""+currentLatitude);
//                mIntent.putExtra("Longitude",""+currentLongitude);
        mIntent.putExtra("Latitude", "36.12");
        mIntent.putExtra("Longitude", "-97.07");
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mIntent);
        finish();
    }

    private void promptUserToEnableLocation() {
        AlertDialog.Builder mAlert = new AlertDialog.Builder(SplashActivity.this);
        mAlert.setMessage(getString(R.string.enable_location_service));
        mAlert.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCAION_SERVICE_REQUEST);
            }
        });
        mAlert.show();
        mAlert.setCancelable(false);
    }

    /*
        To know if location service is enabled or not
     */
    private boolean isLocationServiceEnabled(Context mContext) {
        int locationMode = 0;
        try {
            locationMode = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE);
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCAION_SERVICE_REQUEST) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission();
            } else {
                getCurrentLocation();
            }
        }
    }
}
