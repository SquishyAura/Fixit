package com.example.nasib.fixit;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    //LocationManager locationManager;
    //LatLng myLocation;
    Location markerLocation;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toast.makeText(getApplicationContext(),R.string.Tap_the_map, Toast.LENGTH_LONG).show();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                markerLocation = new Location(LocationManager.GPS_PROVIDER);
                markerLocation.setLatitude(point.latitude);
                markerLocation.setLongitude(point.longitude);
                MarkerOptions marker = new MarkerOptions().position(new LatLng(point.latitude, point.longitude)).title("New Marker");

                //Clear all markers
                googleMap.clear();
                //Add the new marker by touching
                googleMap.addMarker(marker);
            }
        });

        //noinspection MissingPermission
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng mLatLng = new LatLng(location.getLatitude(),location.getLongitude());

                            mMap.addMarker(new MarkerOptions().position(mLatLng).title("It's Me!"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 13f));
                            //System.out.println(location.getLatitude() + " testy2 " + location.getLongitude());
                            markerLocation = location;
                            //markerLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        }
                    }
                });

    }

    public void btnMyLocationOnClick(View view) {
        Intent intent = new Intent();
        intent.putExtra("LocationLat", markerLocation.getLatitude());
        intent.putExtra("LocationLng", markerLocation.getLongitude());
        setResult(RESULT_OK, intent);
        finish();

    }
}
