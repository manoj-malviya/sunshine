package com.girnarsoft.android.shunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.girnarsoft.android.shunshine.R;
import com.girnarsoft.android.shunshine.Utility;
import com.girnarsoft.android.shunshine.data.WeatherContract;

import org.w3c.dom.Text;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String SHARE_HASH_TAG = " #SunshineApp";

    private String forecast;

    private static final int FORECAST_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_DATE = 0;
    static final int COL_WEATHER_DESC = 1;
    static final int COL_WEATHER_MAX_TEMP = 2;
    static final int COL_WEATHER_MIN_TEMP = 3;
    static final int COL_WEATHER_PRESSURE = 4;
    static final int COL_WEATHER_WIND_SPEED = 5;
    static final int COL_WEATHER_HUMIDITY = 6;
    static final int COL_WEATHER_DEGREE = 7;
    static final int COL_WEATHER_COND_ID = 8;
    static final int COL_WEATHER_COL_ID = 9;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(FORECAST_LOADER, savedInstanceState, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragmentdetail, menu);

        MenuItem item = menu.findItem(R.id.action_share);

        ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if(provider != null) {
            provider.setShareIntent(getShareIntent());
        } else {
            Log.e(LOG_TAG, "Shareprovider is empty?");
        }
    }

    private Intent getShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, forecast+SHARE_HASH_TAG);

        return intent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Intent intent = getActivity().getIntent();
        if(intent == null || intent.getData() == null){
            return null;
        }

        return new android.support.v4.content.CursorLoader(getActivity(), intent.getData(), FORECAST_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(!data.moveToFirst()) { return;}

        int weatherId = data.getInt(COL_WEATHER_COND_ID);

        Long date = data.getLong(COL_WEATHER_DATE);
        String friendlyDayName = Utility.getFriendlyDayString(getActivity(), date);
        String friendlyDate = Utility.getFormattedMonthDay(getActivity(), date);
        String desc = data.getString(COL_WEATHER_DESC);
        Float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        Float pressure = data.getFloat(COL_WEATHER_PRESSURE);
        Float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
        Float degree = data.getFloat(COL_WEATHER_DEGREE);

        boolean isMatric = Utility.isMetric(getActivity());

        String maxTemp = Utility.formatTemperature(getActivity(), data.getLong(COL_WEATHER_MAX_TEMP), isMatric);
        String minTemp = Utility.formatTemperature(getActivity(), data.getLong(COL_WEATHER_MIN_TEMP), isMatric);

        TextView dayView = (TextView) getView().findViewById(R.id.detail_day_text_view);
        dayView.setText(friendlyDayName);

        TextView dateView = (TextView) getView().findViewById(R.id.detail_date_text_view);
        dateView.setText(friendlyDate);

        TextView highView = (TextView) getView().findViewById(R.id.detail_high_text_view);
        highView.setText(maxTemp);

        TextView lowView = (TextView) getView().findViewById(R.id.detail_low_text_view);
        lowView.setText(minTemp);

        TextView descView = (TextView) getView().findViewById(R.id.detail_desc_view);
        descView.setText(desc);

        ImageView iconView = (ImageView) getView().findViewById(R.id.detail_icon_image_view);
        iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        TextView windSpeedView = (TextView) getView().findViewById(R.id.detail_wind_text_view);
        windSpeedView.setText(Utility.getFormattedWind(getActivity(), windSpeed, degree));

        TextView humidityView = (TextView) getView().findViewById(R.id.detail_humidity_text_view);
        humidityView.setText(getString(R.string.format_humidity, humidity));

        TextView pressureView = (TextView) getView().findViewById(R.id.detail_pressure_text_view);
        pressureView.setText(getString(R.string.format_pressure, pressure));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }
}