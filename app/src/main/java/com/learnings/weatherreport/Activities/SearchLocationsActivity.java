package com.learnings.weatherreport.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.learnings.weatherreport.Model.Location;
import com.learnings.weatherreport.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Likitha on 04/11/17.
 */

public class SearchLocationsActivity extends Activity {

    private AutoCompleteTextView mSearchLocation;
    private Button mSearchButton;
    private LinearLayout mLoadingAnim;

    private List<Location> mLocations;

    private ArrayAdapter<String> adapter;
    private String[] countries;

    /*
        To check if entered data is string or zipcode
     */
    public static Boolean isZipCodeEntered(String value) {
        try {
            Double val = Double.valueOf(value);
            return val != null;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);
        mSearchLocation = findViewById(R.id.textView_Search);
        mSearchButton = findViewById(R.id.button_search);
        mLoadingAnim = findViewById(R.id.loadingLayout);

        new FetchLocations(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLocationFound = false;
                if (mSearchLocation.getText().toString().length() == 0) {
                    return;
                }
                if (isZipCodeEntered(mSearchLocation.getText().toString())) {
                    Intent newIntent = new Intent(SearchLocationsActivity.this, DetailedWeatherReport.class);
                    newIntent.putExtra("ZipCode", mSearchLocation.getText().toString());
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(newIntent);
                } else {
                    //Check if we have this location or not
                    //If we dont have, then show error.
                    for (Location location : mLocations) {
                        if (mSearchLocation.getText().toString().equalsIgnoreCase(location.locationName)) {
                            String latitude = location.latitude;
                            String longitude = location.longitude;
                            Intent newIntent = new Intent(SearchLocationsActivity.this, DetailedWeatherReport.class);
                            newIntent.putExtra("Latitude", latitude);
                            newIntent.putExtra("Longitude", longitude);
                            newIntent.putExtra("Location", location.locationName);
                            Log.e("SearchLocationActivity", "Latitude -> " + latitude + "  Longitude -> " + longitude);
                            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(newIntent);
                            isLocationFound = true;
                            finish();
                        }
                    }
                    if (!isLocationFound)
                        showErrorAlert();

                }
            }
        });
    }

    private void showErrorAlert() {
        android.support.v7.app.AlertDialog.Builder mAlert = new android.support.v7.app.AlertDialog.Builder(SearchLocationsActivity.this);
        mAlert.setMessage(getString(R.string.invalid_city));
        mAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        mAlert.show();
        mAlert.setCancelable(false);
    }

    private class FetchLocations extends AsyncTask<Void, Void, Void> {

        final Context mContext;
        List<String> latLongList;
        List<String> locationList;

        FetchLocations(final Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLocations = new ArrayList<>();
            latLongList = new ArrayList<>();
            locationList = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String locationsQuerryStirng = "https://graphical.weather.gov/xml/sample_products/browser_interface/ndfdXMLclient.php?listCitiesLevel=1234";
                URL locationsQuerryURL = new URL(locationsQuerryStirng);

                // Create XML Parser instance
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xmlParser = factory.newPullParser();
                xmlParser.setInput(getInputStream(locationsQuerryURL), "UTF_8");

                //Start Parsing the xml
                int eventType = xmlParser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    // Looking for a start tag
                    if (eventType == XmlPullParser.START_TAG) {
                        //We look for "title" tag in XML response
                        if (xmlParser.getName().equalsIgnoreCase("latLonList")) {
                            //Once we found the "title" tag, add the text it contains to our builder
                            String latLongString = xmlParser.nextText();
                            latLongList = Arrays.asList(latLongString.split(" "));
                        } else if (xmlParser.getName().equalsIgnoreCase("cityNameList")) {
                            String location = xmlParser.nextText();
                            locationList = Arrays.asList(location.split("\\|"));
                        }
                    }
                    eventType = xmlParser.next();
                }

                if (latLongList.size() > 0 && locationList.size() > 0) {
                    for (int i = 0; i < latLongList.size(); i++) {
                        Location location = new Location();
                        String[] tempLocatin = locationList.get(i).split("\\s*,\\s*");
                        location.locationName = tempLocatin[0];
                        location.state = tempLocatin[1];
                        String[] tempLatLong = latLongList.get(i).split(",");
                        location.latitude = tempLatLong[0];
                        location.longitude = tempLatLong[1];
                        mLocations.add(location);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mLoadingAnim.setVisibility(View.GONE);
            countries = new String[mLocations.size()];
            for (int i = 0; i < mLocations.size(); i++) {
                countries[i] = mLocations.get(i).locationName;
                Log.e("SearchLocationsActivity", "country -> " + countries[i]);
            }
            //Create adapter and pass country array to it
            adapter = new ArrayAdapter<String>(SearchLocationsActivity.this, android.R.layout.simple_dropdown_item_1line, countries);
            mSearchLocation.setAdapter(adapter);
        }

        private InputStream getInputStream(URL url) {
            try {
                return url.openConnection().getInputStream();
            } catch (IOException e) {
                return null;
            }
        }
    }
}
