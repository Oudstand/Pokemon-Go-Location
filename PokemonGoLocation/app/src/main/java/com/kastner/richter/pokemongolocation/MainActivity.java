package com.kastner.richter.pokemongolocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MainActivity  extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    public static double longitude = 0.0;
    public static double latitude = 0.0;

    boolean showSmallFloatingActionButtons = false;

    private FloatingActionButton fab = null;
    private FloatingActionButton fab1 = null;
    private FloatingActionButton fab2 = null;
    private FloatingActionButton fab3 = null;

    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.fab = (FloatingActionButton)findViewById(R.id.fab);
        this.fab1 = (FloatingActionButton)findViewById(R.id.fab1);
        this.fab2 = (FloatingActionButton)findViewById(R.id.fab2);
        this.fab3 = (FloatingActionButton)findViewById(R.id.fab3);

        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!showSmallFloatingActionButtons){
                    fab1.setVisibility(View.VISIBLE);
                    fab2.setVisibility(View.VISIBLE);
                    fab3.setVisibility(View.VISIBLE);
                    showSmallFloatingActionButtons = true;
                }else{
                    fab1.setVisibility(View.GONE);
                    fab2.setVisibility(View.GONE);
                    fab3.setVisibility(View.GONE);
                    showSmallFloatingActionButtons = false;
                }
            }
        });

        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveMapToPosition(new LatLng(latitude, longitude));
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addGoogleMapsMarkerCustomIcon(new LatLng(latitude, longitude));
            }
        });

        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermission()) {
                requestForSpecificPermission();
            }
        }
        LocationService ls = new LocationService(this);
        this.longitude = ls.getLongitude();
        this.latitude = ls.getLatitude();
    }

    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                    LocationService ls = new LocationService(this);
                    this.longitude = ls.getLongitude();
                    this.latitude = ls.getLatitude();
                } else {
                    //not granted
                    Toast.makeText(this, "Ohne Standort funktioniert diese App nicht. Bitte in den Einstellungen Standort zulassen.", Toast.LENGTH_LONG).show();
                    System.out.println("Keine Erlaubnis!");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng currentPosition = new LatLng(this.latitude, this.longitude);
        marker = mMap.addMarker(new MarkerOptions().position(currentPosition).title("Deine Position"));
        moveMapToPosition(currentPosition);
    }

    public void addGoogleMapsMarkerCustomIcon(LatLng position){
        mMap.addMarker(new MarkerOptions().position(position).title("Deine Position")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable._4)));
         marker.remove();
    }

    public void moveMapToPosition(LatLng position){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,16));
    }
}
