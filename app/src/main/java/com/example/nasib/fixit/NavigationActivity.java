package com.example.nasib.fixit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class NavigationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener mlocListener;
    LatLng myLocation;
    LatLng destination;
    float level;
    int minUpdateTimer;

    //get battery level
    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);

            int batteryLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int batteryScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            level = batteryLevel / (float)batteryScale;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toast.makeText(getApplicationContext(),R.string.navigation_wait, Toast.LENGTH_LONG).show();

        //GET DESTINATION
        String destinationLatitude = getIntent().getStringExtra("latitude");
        String destinationLongitude = getIntent().getStringExtra("longitude");
        destination = new LatLng(Double.parseDouble(destinationLatitude), Double.parseDouble(destinationLongitude));

        receiver.onReceive(getApplicationContext(), getIntent());

        if(level >= 0.26f){
            minUpdateTimer = 0;
        }
        else
        {
            minUpdateTimer = 60000;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mlocListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                receiver.onReceive(getApplicationContext(), getIntent());

                //Whenever marker is updated, a new one is placed and the old is removed
                mMap.clear();

                //GET MY LAST KNOWN LOCATION
                double latitude = location.getLatitude();
                double longtitude = location.getLongitude();
                myLocation = new LatLng(latitude, longtitude);
                mMap.addMarker(new MarkerOptions().position(myLocation).title("My current position"));

                mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));

                if(level >= 0.26f){
                    if(minUpdateTimer != 0){
                        locationManager.removeUpdates(mlocListener);

                        minUpdateTimer = 0;

                        //Check if network provider is enabled
                        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            System.out.println("rawrhej");
                            //noinspection MissingPermission
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minUpdateTimer, 0, mlocListener);
                        }
                        else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            //noinspection MissingPermission
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minUpdateTimer, 0, mlocListener);
                        }
                    }
                }
                else
                {
                    if(minUpdateTimer != 60000){
                        locationManager.removeUpdates(mlocListener);

                        minUpdateTimer = 60000;

                        //Check if network provider is enabled
                        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            System.out.println("rawrhej");
                            //noinspection MissingPermission
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minUpdateTimer, 0, mlocListener);
                        }
                        else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            //noinspection MissingPermission
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minUpdateTimer, 0, mlocListener);
                        }
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        //Check if network provider is enabled
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            System.out.println("rawrhej");
            //noinspection MissingPermission
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minUpdateTimer, 0, mlocListener);
        }
        else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //noinspection MissingPermission
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minUpdateTimer, 0, mlocListener);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 13f));
    }

    public void btnGoBackOnClick(View view) {
        mMap.clear();
        locationManager.removeUpdates(mlocListener);
        finish();
    }
}
