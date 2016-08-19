package com.solucionestai.basiclocation;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

public class StreetView extends FragmentActivity implements OnStreetViewPanoramaReadyCallback {

    ForegroundActivity mService;
    boolean mBound = false, justView=false, fromMap=false;
    Location location;
    StreetViewPanorama panorama;
    Button Ok,Re,hecho;
    //DecimalFormat df = new DecimalFormat("#.######");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);

        SupportStreetViewPanoramaFragment streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);

        if (getIntent().hasCategory("hSitio") ||
                getIntent().hasCategory("hAlert")){
            justView=true;
        }
        if (getIntent().hasCategory("eFromMap")){
            fromMap=true;
        }

        Log.e("onCreate justView",justView+" fromMap: "+fromMap);

        if (!justView){
            Ok=(Button)findViewById(R.id.bOKStreet);
            Ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Double lat=panorama.getLocation().position.latitude;
                    Double log=panorama.getLocation().position.longitude;
                    Intent intent = new Intent(StreetView.this, SitioEdit.class);
                    intent.putExtra("StreetLat",lat);
                    intent.putExtra("StreetLog",log);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });

            Re=(Button)findViewById(R.id.cMapsStreet);
            Re.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Double lat=panorama.getLocation().position.latitude;
                    Double log=panorama.getLocation().position.longitude;
                    Intent intent = new Intent(StreetView.this, SelectSite.class);
                    if (lat!=0 && log!=0){
                        intent.addCategory("eFromStreet");
                        intent.putExtra("StreetLat",lat);
                        intent.putExtra("StreetLog",log);
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });

            hecho=(Button)findViewById(R.id.bHechoStreet);
            hecho.setVisibility(View.GONE);

        } else {
            Ok=(Button)findViewById(R.id.bOKStreet);
            Ok.setVisibility(View.GONE);
            Re=(Button)findViewById(R.id.cMapsStreet);
            Re.setVisibility(View.GONE);
            hecho=(Button)findViewById(R.id.bHechoStreet);
            hecho.setVisibility(View.VISIBLE);
            hecho.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        if (!justView && !fromMap){
            Intent intent = new Intent(this, ForegroundActivity.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        Log.e("onStart justView",justView+" fromMap: "+fromMap);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (getIntent().hasCategory("hSitio") ||
                getIntent().hasCategory("hAlert")){
            justView=true;
        }
        if (getIntent().hasCategory("eFromMap")){
            fromMap=true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (!justView){
            if (mBound) {
                unbindService(mConnection);
                mBound = false;
            }
        }

        Log.e("StreetView","onStop");
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ForegroundActivity.LocalBinder binder = (ForegroundActivity.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            location = mService.getCurrentLocation();
            if (panorama!=null){
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                panorama.setPosition(loc);
                Toast.makeText(StreetView.this,"Ready "+location.toString(),Toast.LENGTH_SHORT).show();
            }
            Log.e("StreetView","onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.e("StreetView","onServiceDisconnected");
        }
    };


    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panoramaR) {

        panorama=panoramaR;

        if (justView){
            final double lat,log;

            if (getIntent().hasCategory("hSitio")){
                lat=Double.valueOf(getIntent().getStringExtra("lat"));
                log=Double.valueOf(getIntent().getStringExtra("long"));
            }else {
                lat=getIntent().getDoubleExtra("lat",0);
                log=getIntent().getDoubleExtra("long",0);
            }

            LatLng loc=new LatLng(lat,log);

            if (lat!=0 && log!=0){
                panorama.setPosition(loc);
            }else {
                new AlertDialog.Builder(StreetView.this)
                        .setMessage("Esta ubicacion no esta disponible en StreetView")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(StreetView.this, SelectSite.class);
                                intent.putExtra("StreetLat",lat);
                                intent.putExtra("StreetLog",log);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .show();
            }

        }else if (fromMap) {
            final double lat,log;
            String flat,flog;

            /*
            flat=df.format(getIntent().getDoubleExtra("MapLat",0));
            flog=df.format(getIntent().getDoubleExtra("MapLog",0));

            lat=Double.parseDouble(flat);
            log=Double.parseDouble(flog);
            */

            lat=getIntent().getDoubleExtra("MapLat",0);
            log=getIntent().getDoubleExtra("MapLog",0);

            LatLng loc=new LatLng(lat,log);

            if (lat!=0 && log!=0){
                panorama.setPosition(loc);
            }else {
                new AlertDialog.Builder(StreetView.this)
                        .setMessage("Esta ubicacion no esta disponible en StreetView")
                        .setCancelable(false)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Re.callOnClick();
                            }
                        })
                        .show();
            }

        } else {
            if (location!=null){
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                panorama.setPosition(loc);
            }
        }

        Log.e("onPanoramaRDY justView",justView+" fromMap: "+fromMap);
    }



}
