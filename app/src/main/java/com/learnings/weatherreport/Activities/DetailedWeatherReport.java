package com.learnings.weatherreport.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.learnings.weatherreport.Adapter.WeeklyReportAdapter;
import com.learnings.weatherreport.Model.Location;
import com.learnings.weatherreport.Model.WeatherReport;
import com.learnings.weatherreport.R;
import com.learnings.weatherreport.WeatherApplication;
import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Likitha on 03/11/17.
 */

public class DetailedWeatherReport extends Activity {

    private static HashMap<String, List<WeatherReport>> weeklyWeatherReport;
    private static List<WeatherReport> selectedWeatherReport;
    private RecyclerView mWeeklyDataView;
    private TextView mLocationName, mWeatherCondition, mCurrentTemp;
    private ImageView mWeatherConditionImg;
    private String mLatitude, mLongitude;
    private String mLocationFromLatLong = "";
    private ImageView mAddOtherLocation;
    private LinearLayout mLoading;
    //If this is false, then we are Searching by location
    private boolean searchByZipCode = false;
    private String enteredZip;
    private boolean fahrenheitUnit = true;

    private WeeklyReportAdapter mAdapter;
    private TextView mCentigradeUnit;
    private TextView mFahrenheitUnit;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detailed_weather_report);

        mLoading = findViewById(R.id.loadingLayout);
        mLocationName = findViewById(R.id.text_locationName);
        mWeatherCondition = findViewById(R.id.text_weatherCondition);
        mWeatherConditionImg = findViewById(R.id.image_weatherCondition);
        mCentigradeUnit = (TextView) findViewById(R.id.text_degreesUnit);
        mFahrenheitUnit = findViewById(R.id.text_fahrenheitUnit);
        mCurrentTemp = findViewById(R.id.text_tempNow);
        mAddOtherLocation = findViewById(R.id.image_addCity);
        mWeeklyDataView = findViewById(R.id.recyclerView_weeklyReport);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mLatitude = extras.getString("Latitude");
            mLongitude = extras.getString("Longitude");
            mLocationFromLatLong = extras.getString("Location");
            enteredZip = extras.getString("ZipCode");
        }
        if (enteredZip != null && enteredZip.length() != 0) {
            searchByZipCode = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Context mContext = getApplicationContext();
        Typeface mRubikRegular = Typeface.createFromAsset(mContext.getAssets(), "Rubik-Regular.ttf");
        Typeface mRubikLight = Typeface.createFromAsset(mContext.getAssets(), "Rubik-Light.ttf");
        mLocationName.setTypeface(mRubikLight);
        mWeatherCondition.setTypeface(mRubikLight);
        mCurrentTemp.setTypeface(mRubikRegular);
        mCentigradeUnit.setTypeface(mRubikLight);
        mFahrenheitUnit.setTypeface(mRubikLight);
        mCentigradeUnit.setText((char) 0x00B0 + "C");
        mFahrenheitUnit.setText((char) 0x00B0 + "F");
        if (fahrenheitUnit) {
            mCentigradeUnit.setAlpha(0.4f);
            mCentigradeUnit.setClickable(true);
        } else {
            mFahrenheitUnit.setAlpha(1.0f);
            mFahrenheitUnit.setClickable(false);
        }
        fetchTempForSpecifiedLocation(getApplicationContext());
        mAddOtherLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent main = new Intent(DetailedWeatherReport.this, ViewAddedLocations.class);
                startActivity(main);
            }
        });
        mCentigradeUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter != null) {
                    mAdapter.changeTemperatureUnits(false);
                    fahrenheitUnit = false;
                    mCentigradeUnit.setAlpha(1.0f);
                    mCentigradeUnit.setClickable(false);

                    mFahrenheitUnit.setAlpha(0.4f);
                    mFahrenheitUnit.setClickable(true);

                    if (fahrenheitUnit) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCurrentTemp.setText(selectedWeatherReport.get(0).maximumTemp + (char) 0x00B0);
                            }
                        });

                    } else {
                        try {
                            //Show temp in degrees.
                            int maximumTemp = Integer.parseInt(selectedWeatherReport.get(0).maximumTemp);
                            maximumTemp = (maximumTemp - 32) * 5 / 9;
                            final int finalMaximumTemp = maximumTemp;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mCurrentTemp.setText(finalMaximumTemp + "" + (char) 0x00B0);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        mFahrenheitUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter != null) {
                    mAdapter.changeTemperatureUnits(true);
                    fahrenheitUnit = true;
                    mFahrenheitUnit.setAlpha(1.0f);
                    mFahrenheitUnit.setClickable(false);

                    mCentigradeUnit.setAlpha(0.4f);
                    mCentigradeUnit.setClickable(true);

                    if (fahrenheitUnit)
                        mCurrentTemp.setText(selectedWeatherReport.get(0).maximumTemp + (char) 0x00B0);
                    else {
                        try {
                            //Show temp in degrees.
                            int maximumTemp = Integer.parseInt(selectedWeatherReport.get(0).maximumTemp + (char) 0x00B0);
                            maximumTemp = (maximumTemp - 32) * 5 / 9;
                            mCurrentTemp.setText(maximumTemp);
                        } catch (Exception e) {

                        }
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private String getNameFromLatLong(String latitude, String longitude) {
        Geocoder geocoder = new Geocoder(DetailedWeatherReport.this, Locale.getDefault());
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            Address address = addresses.get(0);
            Log.e("DetailedWeatherReport", "Country -> " + address.getCountryName());
            Log.e("DetailedWeatherReport", "Locality -> " + address.getLocality());
            Log.e("DetailedWeatherReport", "Featured -> " + address.getFeatureName());
            Log.e("DetailedWeatherReport", "Admin Area -> " + address.getAdminArea());
            return address.getLocality().toString();
        } catch (Exception e) {

        }
        return "";
    }

    private void fetchTempForSpecifiedLocation(final Context mContext) {
        new GetWeatherReport(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showErrorAlert() {
        android.support.v7.app.AlertDialog.Builder mAlert = new android.support.v7.app.AlertDialog.Builder(DetailedWeatherReport.this);
        if (searchByZipCode) {
            mAlert.setMessage(getString(R.string.invalid_zip_code));
            mAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        } else {
            mAlert.setMessage(getString(R.string.something_went_wrong));
            mAlert.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    fetchTempForSpecifiedLocation(getApplicationContext());
                }
            });
        }

        mAlert.show();
        mAlert.setCancelable(false);
    }

    private String getDayOfTheWeek(int day) {
        switch (day) {
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
        }
        return "";
    }

    private class GetWeatherReport extends AsyncTask<Void, Void, Void> {

        final Context mContext;

        GetWeatherReport(final Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected void onPreExecute() {
            mLoading.setVisibility(View.VISIBLE);
            weeklyWeatherReport = new HashMap<>();
            selectedWeatherReport = new ArrayList<>();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String weatherReportURLString = "";
                if (searchByZipCode) {
                    weatherReportURLString = "https://graphical.weather.gov/xml/sample_products/browser_interface/ndfdBrowserClientByDay.php?zipCodeList=" + enteredZip + "&format=24+hourly&numDays=7";
                } else {
                    weatherReportURLString = "https://graphical.weather.gov/xml/sample_products/browser_interface/ndfdBrowserClientByDay.php?lat=" + mLatitude + "&lon=" + mLongitude + "&format=24+hourly&numDays=7";
                }

                // Create XML Parser instance

                URL url = new URL(weatherReportURLString);
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setConnectTimeout(10000);
                urlConn.setReadTimeout(10000);
                urlConn.connect();
                InputStream stream = urlConn.getInputStream();
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xmlParser = factory.newPullParser();
                xmlParser.setInput(stream, "UTF_8");

                //Temp variables used while parsing
                String layoutKey = "";
                List<WeatherReport> dateOfReportList = new ArrayList<>();
                List<WeatherReport> tempWeatherReportList = new ArrayList<>();
                String temperatureType = "maximum"; //default Maximum temp
                int fetchDataForRow = -1;
                boolean isValidImageURL = false;

                //Start Parsing the xml
                int eventType = xmlParser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    // Start tag in xml like <head>
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xmlParser.getName().equalsIgnoreCase("layout-key")) {
                            layoutKey = xmlParser.nextText();
                        } else if (xmlParser.getName().equalsIgnoreCase("point")) {
                            if (searchByZipCode) {
                                mLatitude = xmlParser.getAttributeValue(0);
                                mLongitude = xmlParser.getAttributeValue(1);
                            }
                        } else if (xmlParser.getName().equalsIgnoreCase("start-valid-time")) {
                            WeatherReport mWeather = new WeatherReport();
                            String dateStr = xmlParser.nextText();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
                            Date date = dateFormat.parse(dateStr);
                            dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
                            mWeather.dateOfTheReport = dateFormat.format(date);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                            mWeather.dayOfTheWeek = getDayOfTheWeek(dayOfWeek);
                            dateOfReportList.add(mWeather);
                        } else if (xmlParser.getName().equalsIgnoreCase("temperature")) {
                            layoutKey = xmlParser.getAttributeValue(2);
                            temperatureType = xmlParser.getAttributeValue(0);
                            tempWeatherReportList = weeklyWeatherReport.get(layoutKey);
                            fetchDataForRow = -1; //Reset the row value
                        } else if (xmlParser.getName().equalsIgnoreCase("value")) {
                            if (temperatureType.length() != 0) {
                                //Only if the temp is either minimum or maximum
                                fetchDataForRow = fetchDataForRow + 1;
                                if (temperatureType.equalsIgnoreCase("maximum")) {
                                    tempWeatherReportList.get(fetchDataForRow).maximumTemp = xmlParser.nextText();
                                } else {
                                    tempWeatherReportList.get(fetchDataForRow).minimumTemp = xmlParser.nextText();
                                }
                            }
                        } else if (xmlParser.getName().equalsIgnoreCase("weather")) {
                            layoutKey = xmlParser.getAttributeValue(0);
                            tempWeatherReportList = weeklyWeatherReport.get(layoutKey);
                            fetchDataForRow = -1;
                        } else if (xmlParser.getName().equalsIgnoreCase("weather-conditions")) {
                            fetchDataForRow = fetchDataForRow + 1;
                            tempWeatherReportList.get(fetchDataForRow).weatherCondition = xmlParser.getAttributeValue(0);
                        } else if (xmlParser.getName().equalsIgnoreCase("conditions-icon")) {
                            layoutKey = xmlParser.getAttributeValue(1);
                            tempWeatherReportList = weeklyWeatherReport.get(layoutKey);
                            fetchDataForRow = -1;
                        } else if (xmlParser.getName().equalsIgnoreCase("icon-link")) {
                            isValidImageURL = true;
                        }

                    } else if (eventType == XmlPullParser.TEXT) {
                        if (isValidImageURL && !xmlParser.getText().contains("\n")) {
                            fetchDataForRow = fetchDataForRow + 1;
                            tempWeatherReportList.get(fetchDataForRow).weatherIcon = xmlParser.getText();
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if (xmlParser.getName().equalsIgnoreCase("time-layout")) {
                            //weather.weatherDetails.put(key,dates);
                            weeklyWeatherReport.put(layoutKey, dateOfReportList);
                            dateOfReportList = new ArrayList<>();
                            layoutKey = "";
                        } else if (xmlParser.getName().equalsIgnoreCase("temperature")
                                || xmlParser.getName().equalsIgnoreCase("weather")) {
                            weeklyWeatherReport.put(layoutKey, tempWeatherReportList);
                            fetchDataForRow = -1;
                            temperatureType = "";
                        }
                        /*else if (xmlParser.getName().equalsIgnoreCase("weather")) {
                            weeklyWeatherReport.put(layoutKey, tempWeatherReportList);
                            fetchDataForRow = -1;
                            temperatureType = "";
                        }*/
                        else if (xmlParser.getName().equalsIgnoreCase("conditions-icon")) {
                            weeklyWeatherReport.put(layoutKey, tempWeatherReportList);
                            fetchDataForRow = -1;
                            isValidImageURL = false;
                            temperatureType = "";
                        }

                    }
                    eventType = xmlParser.next();
                }

                //For this project lets consider only 24 hour time-layout
                String timeLayoutKeyFor24Hrs = "k-p24h-n7";
                Set<String> keySet = weeklyWeatherReport.keySet();
                String todaysMaxTemp;
                String todaysMinTemp;
                for (String key : keySet) {
                    if (key.contains(timeLayoutKeyFor24Hrs)) {
                        selectedWeatherReport = weeklyWeatherReport.get(key);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

                weeklyWeatherReport = new HashMap<>();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (selectedWeatherReport.size() == 0) {
                Log.e("DetailedWeatherReport", "Something went wrong...");
                showErrorAlert();
                return;
            }
            mLoading.setVisibility(View.GONE);
            //Update UI
            if (mLocationFromLatLong == null)
                mLocationFromLatLong = getNameFromLatLong(mLatitude, mLongitude);
            mLocationName.setText(mLocationFromLatLong);
            Picasso.with(mContext).load(selectedWeatherReport.get(0).weatherIcon).into(mWeatherConditionImg);
            if (fahrenheitUnit) {
                if (selectedWeatherReport.get(0).maximumTemp.equalsIgnoreCase("")) {
                    mCurrentTemp.setText(selectedWeatherReport.get(0).maximumTemp);
                } else
                    mCurrentTemp.setText(selectedWeatherReport.get(0).maximumTemp + (char) 0x00B0);
            } else {
                try {
                    //Show temp in degrees.
                    int maximumTemp = Integer.parseInt(selectedWeatherReport.get(0).maximumTemp);
                    maximumTemp = (maximumTemp - 32) * 5 / 9;
                    mCurrentTemp.setText(maximumTemp);
                } catch (Exception e) {

                }
            }
            mWeatherCondition.setText(selectedWeatherReport.get(0).weatherCondition);
            mAdapter = new WeeklyReportAdapter(mContext, selectedWeatherReport, fahrenheitUnit);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mWeeklyDataView.setLayoutManager(layoutManager);
            mWeeklyDataView.setAdapter(mAdapter);
            mWeeklyDataView.setVisibility(View.VISIBLE);
            Location mLocation = new Location();
            mLocation.latitude = mLatitude;
            mLocation.longitude = mLongitude;
            mLocation.locationName = mLocationFromLatLong;
            mLocation.maxTemperature = selectedWeatherReport.get(0).maximumTemp;
            mLocation.minTemperature = selectedWeatherReport.get(0).minimumTemp;
            ((WeatherApplication) getApplication()).addALocation(mLocation);
        }
    }
}
