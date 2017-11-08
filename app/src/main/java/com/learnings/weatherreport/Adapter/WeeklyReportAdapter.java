package com.learnings.weatherreport.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.learnings.weatherreport.Model.WeatherReport;
import com.learnings.weatherreport.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Likitha on 03/11/17.
 */

public class WeeklyReportAdapter extends RecyclerView.Adapter<WeeklyReportAdapter.MyViewHolder> {

    final Context mContext;
    final List<WeatherReport> weatherReportList;
    final Typeface mRubikMedium;
    boolean showFahrenheitUnits = true;

    public WeeklyReportAdapter(Context mContext, List<WeatherReport> weatherReportList, final boolean showFahrenheitUnits) {
        this.mContext = mContext;
        this.weatherReportList = weatherReportList;
        this.showFahrenheitUnits = showFahrenheitUnits;
        mRubikMedium = Typeface.createFromAsset(mContext.getAssets(), "Rubik-Medium.ttf");
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weekly_report, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        WeatherReport weatherReport = weatherReportList.get(position);
        holder.maximumTemp.setTypeface(mRubikMedium);
        holder.minimumTemp.setTypeface(mRubikMedium);
        holder.dayOfWeekView.setTypeface(mRubikMedium);

        if (!showFahrenheitUnits) {
            try {
                int maximumTemp = Integer.parseInt(weatherReport.maximumTemp);
                maximumTemp = (maximumTemp - 32) * 5 / 9;
                holder.maximumTemp.setText("" + maximumTemp + (char) 0x00B0);
            } catch (Exception e) {

            }
            try {
                int minimumTemp = Integer.parseInt(weatherReport.minimumTemp);
                minimumTemp = (minimumTemp - 32) * 5 / 9;
                holder.minimumTemp.setText("" + minimumTemp + (char) 0x00B0);
            } catch (Exception e) {

            }
        } else {
            if (weatherReport.maximumTemp == "") {
                holder.maximumTemp.setText(weatherReport.maximumTemp);
            } else {
                holder.maximumTemp.setText(weatherReport.maximumTemp + (char) 0x00B0);
            }
            if (weatherReport.minimumTemp == "") {
                holder.minimumTemp.setText(weatherReport.minimumTemp);
            } else {
                holder.minimumTemp.setText(weatherReport.minimumTemp + (char) 0x00B0);
            }
        }

        Picasso.with(mContext).load(weatherReport.weatherIcon).into(holder.weatherCondition);
        holder.dayOfWeekView.setText(weatherReport.dayOfTheWeek);
    }

    public void changeTemperatureUnits(boolean showFahrenheitUnits) {
        this.showFahrenheitUnits = showFahrenheitUnits;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return weatherReportList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        final TextView dayOfWeekView;
        final ImageView weatherCondition;
        final TextView maximumTemp;
        final TextView minimumTemp;

        MyViewHolder(View itemView) {
            super(itemView);
            dayOfWeekView = itemView.findViewById(R.id.text_dayName);
            weatherCondition = itemView.findViewById(R.id.image_weatherCondition);
            minimumTemp = itemView.findViewById(R.id.text_minTemp);
            maximumTemp = itemView.findViewById(R.id.text_maxTemp);
        }
    }
}
