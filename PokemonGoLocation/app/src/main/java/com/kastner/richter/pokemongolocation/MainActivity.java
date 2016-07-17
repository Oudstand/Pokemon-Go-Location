package com.kastner.richter.pokemongolocation;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    public static double longitude = 0.0;
    public static double latitude = 0.0;

    boolean showSmallFloatingActionButtons = false;

    private FloatingActionButton fab = null;
    private FloatingActionButton fab1 = null;
    private FloatingActionButton fab2 = null;
    private FloatingActionButton fab3 = null;
    private FloatingActionButton fab4 = null;

    public static Marker currentPositionMarker;
    private Map<Integer, ArrayList<Marker>> markers = new HashMap<Integer, ArrayList<Marker>>();

    private File FILEPATH;
    private static String FILENAME = "markedPokemon.txt";
    private File markedPokemon;
    private FileWriter writer;
    BufferedReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            FILEPATH = new File(Environment.getExternalStorageDirectory(), "PokemonGoLocations");
            if (!FILEPATH.exists()) {
                FILEPATH.mkdir();
            }
            markedPokemon = new File(FILEPATH, FILENAME);
            if (!markedPokemon.exists()) {
                markedPokemon.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.fab = (FloatingActionButton) findViewById(R.id.fab);
        this.fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        this.fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        this.fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        this.fab4 = (FloatingActionButton) findViewById(R.id.fab4);

        this.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!showSmallFloatingActionButtons) {
                    fab1.setVisibility(View.VISIBLE);
                    fab2.setVisibility(View.VISIBLE);
                    fab3.setVisibility(View.VISIBLE);
                    fab4.setVisibility(View.VISIBLE);
                    showSmallFloatingActionButtons = true;
                } else {
                    fab1.setVisibility(View.GONE);
                    fab2.setVisibility(View.GONE);
                    fab3.setVisibility(View.GONE);
                    fab4.setVisibility(View.GONE);
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

        final Intent intent = new Intent(this, PokemoncodeActivity.class);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(intent, 1);
            }
        });

        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(intent, 2);
            }
        });

        fab4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markedPokemon.delete();
                ArrayList<Marker> tmpMarker;
                for(Map.Entry<Integer, ArrayList<Marker>> tmp : markers.entrySet()){
                    tmpMarker = tmp.getValue();
                    for (Marker tmpEntry : tmpMarker){
                        tmpEntry.remove();
                    }
                }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            String pokemonCode = data.getStringExtra(PokemoncodeActivity.RESULT_POKEMONCODE);
            addNewGoogleMapsMarkerCustomIcon(new LatLng(latitude, longitude), pokemonCode);
        }
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            String pokemonCode = data.getStringExtra(PokemoncodeActivity.RESULT_POKEMONCODE);
            getClosestMarker(Integer.valueOf(pokemonCode));
        }
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
        currentPositionMarker = mMap.addMarker(new MarkerOptions().position(currentPosition).title("Deine Position"));
        moveMapToPosition(currentPosition);
        try {
            reader = new BufferedReader(new FileReader(markedPokemon));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                String[] seperated = line.split(" ");
                addGoogleMapsMarkerCustomIcon(new LatLng(Double.valueOf(seperated[1]), Double.valueOf(seperated[2])), seperated[0]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNewGoogleMapsMarkerCustomIcon(LatLng position, String pokemonCode) {
        String icon = "_" + pokemonCode;
        int pokemonId = Integer.valueOf(pokemonCode);
        int resImage = getResources().getIdentifier(icon, "drawable", this.getPackageName());
        String[] pokemonNames = getResources().getStringArray(R.array.pokemon_names);
        String pokemonName = pokemonNames[pokemonId - 4];
        Marker newMarker = mMap.addMarker(new MarkerOptions().position(position).title(pokemonName)
                .icon(BitmapDescriptorFactory.fromResource(resImage)));
        newMarker.showInfoWindow();
        if (markers.containsKey(pokemonId)) {
            markers.get(pokemonId).add(newMarker);
        } else {
            ArrayList<Marker> marker = new ArrayList<>();
            marker.add(newMarker);
            markers.put(pokemonId, marker);
        }
        currentPositionMarker.remove();
        try {
            writer = new FileWriter(markedPokemon, true);
            writer.write(pokemonId + " " + newMarker.getPosition().latitude + " " + newMarker.getPosition().longitude + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addGoogleMapsMarkerCustomIcon(LatLng position, String pokemonCode) {
        String icon = "_" + pokemonCode;
        int pokemonId = Integer.valueOf(pokemonCode);
        int resImage = getResources().getIdentifier(icon, "drawable", this.getPackageName());
        String[] pokemonNames = getResources().getStringArray(R.array.pokemon_names);
        String pokemonName = pokemonNames[pokemonId - 4];
        Marker newMarker = mMap.addMarker(new MarkerOptions().position(position).title(pokemonName)
                .icon(BitmapDescriptorFactory.fromResource(resImage)));
        newMarker.showInfoWindow();
        if (markers.containsKey(pokemonId)) {
            markers.get(pokemonId).add(newMarker);
        } else {
            ArrayList<Marker> marker = new ArrayList<>();
            marker.add(newMarker);
            markers.put(pokemonId, marker);
        }
    }

    public void getClosestMarker(int pokemonId) {
        if (markers.containsKey(pokemonId)) {
            ArrayList<Marker> markerFromId = markers.get(pokemonId);
            double closestDistance = Double.POSITIVE_INFINITY;
            double currentDistance = 0;
            Marker searchedMarker = currentPositionMarker;
            for (Marker tmp : markerFromId) {
                currentDistance = geoCoordToMeter(latitude, longitude, tmp.getPosition().latitude, tmp.getPosition().longitude);
                if (currentDistance < closestDistance) {
                    closestDistance = currentDistance;
                    searchedMarker = tmp;
                }
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(searchedMarker.getPosition().latitude, searchedMarker.getPosition().longitude), 16));
        } else {
            Toast.makeText(this, "Pokemon nicht auf der Karte gefunden", Toast.LENGTH_LONG).show();
        }
    }


    public void moveMapToPosition(LatLng position) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16));
    }

    public static double geoCoordToMeter(double latA, double lonA, double latB, double lonB) {
        double earthRadius = 6378.137d; // km
        double dLat = (latB - latA) * Math.PI / 180d;
        double dLon = (lonB - lonA) * Math.PI / 180d;
        double a = Math.sin(dLat / 2d) * Math.sin(dLat / 2d)
                + Math.cos(latA * Math.PI / 180d)
                * Math.cos(latB * Math.PI / 180d)
                * Math.sin(dLon / 2d) * Math.sin(dLon / 2);
        double c = 2d * Math.atan2(Math.sqrt(a), Math.sqrt(1d - a));
        double d = earthRadius * c;
        return (d * 1000d);
    }
}


