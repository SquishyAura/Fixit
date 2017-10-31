package com.example.nasib.fixit;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
    LatLng myLocation;
    Location markerLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toast.makeText(getApplicationContext(),R.string.navigation_wait, Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //Check if network provider is enabled
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {
                    //Whenever marker is updated, a new one is placed and the old is removed
                    mMap.clear();

                    //GET MY LAST KNOWN LOCATION
                    double latitude = location.getLatitude();
                    double longtitude = location.getLongitude();
                    myLocation = new LatLng(latitude, longtitude);
                    mMap.addMarker(new MarkerOptions().position(myLocation).title("My current position"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13f));


                    //GET DESTINATION
                    String destinationLatitude = getIntent().getStringExtra("latitude");
                    String destinationLongitude = getIntent().getStringExtra("longitude");
                    LatLng destination = new LatLng(Double.parseDouble(destinationLatitude), Double.parseDouble(destinationLongitude));
                    mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //Whenever marker is updated, a new one is placed and the old is removed
                    mMap.clear();

                    //GET MY LAST KNOWN LOCATION
                    double latitude = location.getLatitude();
                    double longtitude = location.getLongitude();
                    myLocation = new LatLng(latitude, longtitude);
                    mMap.addMarker(new MarkerOptions().position(myLocation).title("My current position"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13f));

                    //GET DESTINATION
                    String destinationLatitude = getIntent().getStringExtra("latitude");
                    String destinationLongitude = getIntent().getStringExtra("longitude");
                    LatLng destination = new LatLng(Double.parseDouble(destinationLatitude), Double.parseDouble(destinationLongitude));
                    mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void btnGoBackOnClick(View view) {
        finish();
    }
}
