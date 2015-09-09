package com.girnarsoft.android.shunshine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.girnarsoft.android.shunshine.data.WeatherContract;
import com.girnarsoft.android.shunshine.service.SunshineService;
import com.girnarsoft.android.shunshine.sync.SunshineSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ForecastAdapter adapter;
    private ListView mListView;
    private int mSelectedPosition;
    private boolean mUseTodayLayout = true;

    private static final int FORECAST_LOADER = 1;
    private static final String POSITION = "position";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mSelectedPosition != ListView.INVALID_POSITION)
            outState.putInt(POSITION, mSelectedPosition);

        super.onSaveInstanceState(outState);
    }

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View V = inflater.inflate(R.layout.fragment_main, container, false);

        if(savedInstanceState != null && savedInstanceState.containsKey(POSITION))
            mSelectedPosition = savedInstanceState.getInt(POSITION);

        mListView = (ListView) V.findViewById(R.id.listview_forecast);

        adapter = new ForecastAdapter(getActivity(), null, 0);
        adapter.setUseTodayLayout(mUseTodayLayout);

        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cur = (Cursor) adapter.getItem(position);
                String locationString = Utility.getPreferredLocation(getActivity());
                if (cur != null) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationString,
                            cur.getLong(ForecastFragment.COL_WEATHER_DATE)
                    ));

                    startActivity(intent);
                }

                mSelectedPosition = position;
            }
        });

        return V;
        //return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(FORECAST_LOADER, savedInstanceState, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refreshData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshData() {
        String pincode = Utility.getPreferredLocation(getActivity());

//        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
//        alarmIntent.putExtra("location", pincode);
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
//
//        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+5000, pendingIntent);

        //new FetchWeatherTask(this.getActivity()).execute(pincode);
//        Intent service = new Intent(this.getActivity(), SunshineService.class);
//        service.putExtra("location", pincode);
//        getActivity().startService(service);

        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;

        if(adapter != null){
            adapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    //Loader callbacks


    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationString = Utility.getPreferredLocation(getActivity());
        Long currentTime = System.currentTimeMillis();
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationString, currentTime);

        //return new Loader(getActivity(), uri, null, null, null, sortOrder);
        return new android.support.v4.content.CursorLoader(getActivity(), uri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

        if(mSelectedPosition != ListView.INVALID_POSITION)
            mListView.smoothScrollToPosition(mSelectedPosition);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public void onLocationChanged() {
        refreshData();
    }
}
