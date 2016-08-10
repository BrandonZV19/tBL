package com.solucionestai.basiclocation;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;

public class SelectSite extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Double lat, lng;
    Button change,ok;
    Marker markerDa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_site);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapS);
        mapFragment.getMapAsync(this);

        lat=getIntent().getDoubleExtra("StreetLat",0);
        lng=getIntent().getDoubleExtra("StreetLog",0);

        change=(Button)findViewById(R.id.cStreetSelect);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectSite.this, StreetView.class);
                intent.addCategory("eFromMap");
                //intent.putExtra("MapLat",df.format(lat));
                //intent.putExtra("MapLog",df.format(lng));
                intent.putExtra("MapLat",lat);
                intent.putExtra("MapLog",lng);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                Log.e("change","lat:"+lat+" long:"+lng);
            }
        });

        ok=(Button)findViewById(R.id.bOKSelect);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SelectSite.this, SitioEdit.class);
                intent.putExtra("MapLat",lat);
                intent.putExtra("MapLog",lng);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                Log.e("OK","lat:"+lat+" long:"+lng);
            }
        });

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
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(lat, lng);
        markerDa=mMap.addMarker(new MarkerOptions().position(sydney).title("Mantenme presionado para arrastarme").draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,14));
        markerDa.showInfoWindow();

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                markerDa.setTitle("lat:"+marker.getPosition().latitude+"lng:"+marker.getPosition().longitude);
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                lat=marker.getPosition().latitude;
                lng=marker.getPosition().longitude;
            }
        });

    }

}
