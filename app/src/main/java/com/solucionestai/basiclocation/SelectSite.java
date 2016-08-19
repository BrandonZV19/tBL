package com.solucionestai.basiclocation;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class SelectSite extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    Double lat, lng;
    Button change,ok;
    Marker markerDa;
    boolean fromStreet, mBound = false;
    ForegroundActivity mService;
    Location location;
    private Geocoder mGeocoder;
    AutoCompleteTextView mAutocompleteView;
    PlaceAutocompleteAdapter mAdapter;
    List<Address> addressInfo, latlgnInfo;
    String address;
    Address inputAddress;
    GoogleApiClient mGoogleApiClient;
    TextView mPlaceDetailsText, mPlaceDetailsAttribution;
    private static final LatLngBounds BOUNDS = new LatLngBounds(
            new LatLng(19.70069804989642, -101.274118394775370), new LatLng(19.736572205952953, -101.10417363159178));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_site);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapS);
        mapFragment.getMapAsync(this);

        fromStreet=getIntent().hasCategory("eFromStreet");

        if (fromStreet){
            lat=getIntent().getDoubleExtra("StreetLat",0);
            lng=getIntent().getDoubleExtra("StreetLog",0);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API)
                .build();
        mAutocompleteView = (AutoCompleteTextView)
                findViewById(R.id.autoCompleteTextView);
        // Register a listener that receives callbacks when a suggestion has been selected
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);
        // Retrieve the TextViews that will display details and attributions of the selected place.
        mPlaceDetailsText = (TextView) findViewById(R.id.place_details);
        mPlaceDetailsAttribution = (TextView) findViewById(R.id.place_attribution);
        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS,
                null);
        mAutocompleteView.setAdapter(mAdapter);

        change=(Button)findViewById(R.id.cStreetSelect);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectSite.this, StreetView.class);
                intent.addCategory("eFromMap");
                //intent.putExtra("MapLat",df.format(lat));
                //intent.putExtra("MapLog",df.format(lng));
                lat=markerDa.getPosition().latitude;
                lng=markerDa.getPosition().longitude;

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

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

            String text = mAutocompleteView.getText().toString();
            mGeocoder = new Geocoder(SelectSite.this, Locale.getDefault());
            try {
                latlgnInfo = mGeocoder.getFromLocationName(text, 1);
                inputAddress=latlgnInfo.get(0);
                if (latlgnInfo.size()>0){
                    LatLng user = new LatLng(inputAddress.getLatitude(),inputAddress.getLongitude());
                    markerDa.setPosition(user);
                    //Actualizar camara
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user,14));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            // Format details of the place for display and show it in a TextView.
            mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(),
                    place.getId(), place.getAddress(), place.getPhoneNumber(),
                    place.getWebsiteUri()));

            // Display the third party attributions if set.
            final CharSequence thirdPartyAttribution = places.getAttributions();
            if (thirdPartyAttribution == null) {
                mPlaceDetailsAttribution.setVisibility(View.GONE);
            } else {
                mPlaceDetailsAttribution.setVisibility(View.VISIBLE);
                mPlaceDetailsAttribution.setText(Html.fromHtml(thirdPartyAttribution.toString()));
            }

            places.release();
        }
    };

    @SuppressLint("StringFormatInvalid")
    private static Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        if (!fromStreet){
            Intent intent = new Intent(this, ForegroundActivity.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        Log.e("onStart fromMap: ",fromStreet+"");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        fromStreet=getIntent().hasCategory("eFromStreet");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (!fromStreet){
            if (mBound) {
                unbindService(mConnection);
                mBound = false;
            }
        }

        Log.e("StreetView","onStop");
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ForegroundActivity.LocalBinder binder = (ForegroundActivity.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            location = mService.getCurrentLocation();

            lat=location.getLatitude();
            lng=location.getLongitude();

            if (mMap!=null){
                LatLng loc = new LatLng(lat, lng);
                markerDa=mMap.addMarker(new MarkerOptions().position(loc).title("Mantenme presionado para arrastarme").draggable(true));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,14));
                markerDa.showInfoWindow();
            }
            Log.e("StreetView","onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.e("StreetView","onServiceDisconnected");
        }
    };


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
        // Add a marker in loc and move the camera

        if (!fromStreet) {
            if (location != null) {
                lat=location.getLatitude();
                lng=location.getLongitude();
                LatLng loc = new LatLng(lat, lng);
                markerDa = mMap.addMarker(new MarkerOptions().position(loc).title("Mantenme presionado para arrastarme").draggable(true));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));
                markerDa.showInfoWindow();
            }
        }else {
            LatLng loc = new LatLng(lat, lng);
            markerDa = mMap.addMarker(new MarkerOptions().position(loc).title("Mantenme presionado para arrastarme").draggable(true));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));
            markerDa.showInfoWindow();
        }

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                marker.setTitle("lat:"+marker.getPosition().latitude+"lng:"+marker.getPosition().longitude);
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                lat=marker.getPosition().latitude;
                lng=marker.getPosition().longitude;
            }
        });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,
                "No se pudo conectar al Api de google" + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }
}
