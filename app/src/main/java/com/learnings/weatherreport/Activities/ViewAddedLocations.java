package com.learnings.weatherreport.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.learnings.weatherreport.Model.Location;
import com.learnings.weatherreport.R;
import com.learnings.weatherreport.WeatherApplication;

import java.util.List;

/**
 * Created by Likitha on 05/11/17.
 */

public class ViewAddedLocations extends Activity {

    private ImageView mAddLocationImg;
    private LinearLayout mLoading;
    private RecyclerView mAddedLocationsRv;
    private List<Location> mAddedLocations;
    private AddedLocationsAdapter mLocationsAdapter;
    private TextView mPageTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_added_locations);

        mLoading = findViewById(R.id.loadingLayout);
        mPageTitle = findViewById(R.id.text_PageTitle);
        mAddLocationImg = (ImageView) findViewById(R.id.image_addLocation);
        mAddedLocationsRv = findViewById(R.id.recyclerView_addedLocations);
        fetchAddedLocationsList(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAddLocationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(ViewAddedLocations.this, SearchLocationsActivity.class);
                startActivity(mIntent);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void fetchAddedLocationsList(final Context mContext) {
        mAddedLocations = ((WeatherApplication) getApplication()).getAddedLocations();
        mLocationsAdapter = new AddedLocationsAdapter(mContext, mAddedLocations);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mAddedLocationsRv.setLayoutManager(layoutManager);
        mAddedLocationsRv.setAdapter(mLocationsAdapter);
        mAddedLocationsRv.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.GONE);
    }

    class AddedLocationsAdapter extends RecyclerView.Adapter<AddedLocationsAdapter.MyViewHolder> {

        final Context mContext;
        final Typeface mRubikLight;
        final List<Location> mAddedLocations;

        public AddedLocationsAdapter(final Context mContext, final List<Location> mAddedLocations) {
            this.mContext = mContext;
            this.mAddedLocations = mAddedLocations;
            mRubikLight = Typeface.createFromAsset(mContext.getAssets(), "Rubik-Medium.ttf");
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_added_location, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Location mLocation = mAddedLocations.get(position);
            holder.locationName.setTypeface(mRubikLight);
            holder.minimumTemp.setTypeface(mRubikLight);
            holder.maximumTemp.setTypeface(mRubikLight);

            holder.locationName.setText(mLocation.locationName);
            holder.maximumTemp.setText(mLocation.maxTemperature + (char) 0x00B0);
            holder.minimumTemp.setText(mLocation.minTemperature + (char) 0x00B0);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newIntent = new Intent(ViewAddedLocations.this, DetailedWeatherReport.class);
                    newIntent.putExtra("Latitude", mLocation.latitude);
                    newIntent.putExtra("Longitude", mLocation.longitude);
                    newIntent.putExtra("Location", mLocation.locationName);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(newIntent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAddedLocations.size();
        }


        class MyViewHolder extends RecyclerView.ViewHolder {
            final TextView locationName;
            final TextView maximumTemp;
            final TextView minimumTemp;

            MyViewHolder(View itemView) {
                super(itemView);
                locationName = itemView.findViewById(R.id.text_locationName);
                minimumTemp = itemView.findViewById(R.id.text_minTemp);
                maximumTemp = itemView.findViewById(R.id.text_maxTemp);
            }
        }
    }
}
