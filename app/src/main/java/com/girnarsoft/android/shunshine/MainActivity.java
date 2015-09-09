package com.girnarsoft.android.shunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = getClass().getSimpleName();
    private String mLocation;
    private static final String DETAILFRAGEMENT_TAG = "DFTG";
    private boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocation = Utility.getPreferredLocation(this);

        if(findViewById(R.id.weather_detail_container) != null){
            mTwoPane = true;

            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.weather_detail_container, new DetailFragment())
                        .commit();
            }
        } else {
            mTwoPane = false;

            getSupportActionBar().setElevation(0f);
        }

        ForecastFragment fragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.weather_detail_container);
        if(fragment != null) {
            fragment.setUseTodayLayout(!mTwoPane);
        }
    }

    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation( this );
        // update the location in our second pane using the fragment manager
            if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.weather_detail_container);
            if ( null != ff ) {
                    ff.onLocationChanged();
                }

            DetailFragment df = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGEMENT_TAG);
            if ( null != df ) {
                //df.onLocationChanged();
            }

            mLocation = location;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingIntent);
            return true;
        }

        if(id == R.id.action_map) {
            openPreferredLocation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocation() {
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String locatoin = pref.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default_value));

        Uri geo = Uri.parse("geo:0,0?")
                .buildUpon().appendQueryParameter("q", locatoin)
                .build();
        mapIntent.setData(geo);

        if(mapIntent.resolveActivity(getPackageManager()) != null){
            startActivity(mapIntent);
        } else {
            Log.e(LOG_TAG, "Couldn't show location " + locatoin + ", No suitable application found");
        }
    }
}
