package com.solucionestai.basiclocation;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;

import android.os.Bundle;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    ArrayList<Alertas> listaAlertas;
    private GoogleMap mMap;
    Circle circleLocation = null;
    isBetterLocation isBetter = new isBetterLocation();

    //PreferenciasFragment preferencias;

    boolean fueAjustes = false, pauso = false, finalizar = false, requestingLocations=false,
    notifico=false,notificar=false, sonido=false, subir=true,toast=true;
    int contadorLocaciones = 0, idRegistro, idRegistroA,contadorAlertas,contadorNotificaciones;
    String idUsuario, lastDate = "",
            urlRegistrarCoordenadas = "http://191.101.156.66/erp/ws_gps/ws_registro_coordenadas",
            getUrlRegistrarAlertas = "http://191.101.156.66/erp/ws_gps/ws_registro_alerta";

    Location locacion; // location

    // The minimum distance to change Updates in meters
    long MIN_DISTANCE_CHANGE_FOR_UPDATES;
    // The minimum time between updates in milliseconds
    long MIN_TIME_BW_UPDATES;

    DBConnection dbc;
    SQLiteDatabase db;

    ProgressDialog progressDialog;

    SharedPreferences preferences;

    isConnectedToInternet isConnected;

    ImageButton alerta0;

    View vAlerta0;
    EditText observacionesA0;
    Button aAlerta0,cAlerta0;

    LayoutInflater inflater;

    GoogleApiClient mGoogleApiClient;

    Intent intentService;

    AlertDialog dialogAlerta0;

    MediaPlayer soundAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenciasFragment.getPref(MapsActivity.this);

        MIN_DISTANCE_CHANGE_FOR_UPDATES = preferences.getLong(getString(R.string.StringFrecMetros), 1); // meters
        MIN_TIME_BW_UPDATES = 1000 * preferences.getInt(getString(R.string.StringFrecSegundos), 10); // secs

        if (soundAlert == null) {
            if (preferences.getString(PreferenciasFragment.getKeyElegirSonidoLst(),"1").contains("0")){
                soundAlert = MediaPlayer.create(this, R.raw.gamejump);
            }else {
                if (preferences.getString(PreferenciasFragment.getKeyElegirSonidoLst(),"1").contains("2")){
                    soundAlert = MediaPlayer.create(this, R.raw.alert);
                }else {
                    soundAlert = MediaPlayer.create(this, R.raw.sweetpolite);
                }
            }
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (preferences.getBoolean(PreferenciasFragment.getKeyMantenerSesionChk(), false)
                || getIntent().hasCategory("login_no_sesion") || getIntent().hasExtra("idUsuario")) {


            intentService = new Intent(MapsActivity.this, ForegroundActivity.class);

            if (getIntent().hasCategory("NOTIFICATION")){
                notifico=false;
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancelAll();
            }

            isConnected = new isConnectedToInternet();

            setContentView(R.layout.activity_maps);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            if (getIntent().hasCategory("login_no_sesion") || getIntent().hasExtra("idUsuario")) {
                idUsuario = getIntent().getStringExtra("idUsuario");
                finalizar=true;
            } else {
                idUsuario = preferences.getString(getString(R.string.StringIDUsuarioGuardado), "ERROR");
            }

            listaAlertas = new ArrayList<>();

            progressDialog = new ProgressDialog(MapsActivity.this);
            progressDialog.setMax(100);
            progressDialog.setTitle("Espere . . .");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            inflater = this.getLayoutInflater();

            alerta0 = (ImageButton) findViewById(R.id.bAlerta0);
            alerta0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (vAlerta0 != null && vAlerta0.getParent() == null) {

                        if (dialogAlerta0 != null) {
                            if (dialogAlerta0.isShowing()) {
                                dialogAlerta0.dismiss();
                            }
                            dialogAlerta0 = null;
                        }

                        dialogAlerta0 = new AlertDialog.Builder(MapsActivity.this)
                                .setView(vAlerta0)
                                .setCancelable(false)
                                .show();
                    } else {
                        vAlerta0 = null; //set it to null
                        // now initialized yourView and its component again
                        vAlerta0 = inflater.inflate(R.layout.layout_alerta0, null);
                        observacionesA0 = (EditText) vAlerta0.findViewById(R.id.observacionesAlerta0);

                        aAlerta0 = (Button) MapsActivity.this.vAlerta0.findViewById(R.id.bAAlerta0);
                        aAlerta0.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (locacion != null) {
                                    if (observacionesA0.getText().toString().trim() != "") {
                                        String dateTime = getDateTime();
                                        //metodo con webservice enviar alerta
                                        if (isConnected.isConnectedToInternet(MapsActivity.this)) {
                                            subirAlerta(locacion.getLatitude(),
                                                    locacion.getLongitude(), dateTime,
                                                    observacionesA0.getText().toString());
                                        } else {
                                            Toast.makeText(MapsActivity.this, "Por favor conectate a internet",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(MapsActivity.this, "Por favor ingresa algun comentario",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    dialogAlerta0.cancel();
                                } else {
                                    //Agregar codigo de error
                                    Toast.makeText(MapsActivity.this, "Hay un problema con tu ubicacion, " +
                                                    "por favor intenta de nuevo mas tarde.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        cAlerta0 = (Button) MapsActivity.this.vAlerta0.findViewById(R.id.bCAlerta0);
                        cAlerta0.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogAlerta0.cancel();
                            }
                        });

                        if (dialogAlerta0 != null) {
                            if (dialogAlerta0.isShowing()) {
                                dialogAlerta0.dismiss();
                            }
                            dialogAlerta0 = null;
                        }

                        dialogAlerta0 = new AlertDialog.Builder(MapsActivity.this)
                                .setView(vAlerta0)
                                .setCancelable(false)
                                .show();
                    }

                }
            });


            dbc = new DBConnection(MapsActivity.this, "RegistrosLoc", null, 1);
            db = dbc.getWritableDatabase();

            db.execSQL("CREATE TABLE IF NOT EXISTS Alertas(id INTEGER PRIMARY KEY, latitud REAL," +
                    "longitud REAL, fechaHora DATETIME, fechaHoraS DATETIME, idUsuario INTEGER," +
                    "status INTEGER,arriba INTEGER, comentarios TEXT, usUsuario TEXT)");

            if (idUsuario != null) {
                db.execSQL("CREATE TABLE IF NOT EXISTS Usuario" + idUsuario + "(idRegistro INTEGER PRIMARY KEY, latitud REAL, longitud REAL," +
                        "fechaHora DATETIME, enviado INTEGER)");

                listaAlertas.clear();

                Cursor cc = db.rawQuery("SELECT id, latitud, longitud, fechaHora," +
                        "fechaHoraS, idUsuario, status, comentarios, usUsuario from Alertas", null);
                if (cc.moveToFirst()) { //Si hay registros, nos posicionamos en el primero (el mas viejo)
                    do {
                        Alertas alertas = new Alertas();
                        alertas.setidAlerta(cc.getInt(0));
                        alertas.setLatitudAlerta(cc.getDouble(1));
                        alertas.setLongitudAlerta(cc.getDouble(2));
                        alertas.setDateTimeAlerta(cc.getString(3));
                        alertas.setDateTimeServerAlerta(cc.getString(4));
                        alertas.setIdUsuarioAlerta(cc.getInt(5));
                        alertas.setStatusAlerta(cc.getInt(6));
                        alertas.setComentariosAlerta(cc.getString(7));
                        alertas.setusUsuario(cc.getString(8));
                        listaAlertas.add(alertas);
                        idRegistroA = cc.getInt(0) + 1;
                        Log.e("onCreate", "idAlerta: " + cc.getInt(0));
                    } while (cc.moveToNext());
                }
                cc.close();

            } else {
                //Agregar codigo de error
                Toast.makeText(getApplicationContext(), "Por favor inicia sesión.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }

        } else {
            //Agregar codigo de error
            Toast.makeText(getApplicationContext(), "Por favor inicia sesión.",
                    Toast.LENGTH_LONG)
                    .show();

            finish();
        }

    }

    //

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
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
        //si es la pimera vez que la actividad inicia el contador de las locaciones será 0, solo
        //entonces se iniciara el proceso de escucha de locaciones, y cuando el usuarioa haya ido
        //a ajustes.
        if (contadorLocaciones < 1 || fueAjustes) {
            requestingLocations=true;
            if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()){
                mGoogleApiClient.connect();
            }
        }

            contadorNotificaciones=preferences.getInt("NOTIFICACIONES",0);
            if (contadorNotificaciones>0){
                for (int i=0; i<=contadorNotificaciones; i++){
                    if (getIntent().hasExtra("Latitud"+i)){
                        if (getIntent().hasExtra("Latitud"+i)){
                            if (getIntent().hasExtra("Titulo"+i)){
                                if (getIntent().hasExtra("Mensaje"+i)){
                                    LatLng loc=new LatLng(getIntent().getDoubleExtra("Latitud"+i,0),
                                            getIntent().getDoubleExtra("Longitud"+i,0));
                                    String titulo=getIntent().getStringExtra("Titulo"+i);
                                    String mensaje=getIntent().getStringExtra("Mensaje"+i);
                                    mMap.addMarker(new MarkerOptions()
                                            .position(loc)
                                            .title(titulo)
                                            .snippet(mensaje)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                                }
                            }
                        }
                    } //Hasta aqui para notificaciones en primer plano
                }//termina for de arriba

                if (getIntent().hasExtra("idAlerta")){
                    int idAlert=getIntent().getIntExtra("idAlerta",0);
                    if (idAlert!=0){
                        for (int i=contadorNotificaciones; i>0; i--){
                            Log.e("onMapReady", "idAlert is not 0. for="+idAlert);
                            Cursor cc = db.rawQuery("SELECT id, latitud, longitud, fechaHora," +
                                    "fechaHoraS, idUsuario, status, comentarios, usUsuario from Alertas WHERE id="+idAlert, null);
                            if (cc.moveToFirst()) { //Si hay registros, nos posicionamos en el primero (el mas viejo)
                                LatLng loc=new LatLng(cc.getDouble(1),cc.getDouble(2));
                                String titulo="Alerta de "+cc.getString(8);
                                String mensaje="\"" + cc.getString(7) + "\". " + cc.getString(3);

                                mMap.addMarker(new MarkerOptions()
                                        .position(loc)
                                        .title(titulo)
                                        .snippet(mensaje)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                            }
                            cc.close();
                            idAlert=idAlert-1;
                        }
                    } else {
                        Log.e("onMapReady", "idAlert is 0 = "+idAlert);
                    }
                }

                contadorNotificaciones=0;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("NOTIFICACIONES", 0);
                editor.commit();
            }else {
                Log.e("onMapready","Las preferencias no tienen contador de notificaciones");
            }

        if (getIntent().hasCategory("hAlert")){
            double lat,log;
            String tit="Alerta",msj="";
            lat=getIntent().getDoubleExtra("lat",0);
            log=getIntent().getDoubleExtra("long",0);
            tit=getIntent().getStringExtra("us");
            msj="\""+getIntent().getStringExtra("comen")+"\". "+getIntent().getStringExtra("date");

            LatLng loc=new LatLng(lat,log);

            mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .title(tit)
                    .snippet(msj)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
                    .showInfoWindow();

        }

        if (getIntent().hasCategory("hSitio")){
            double lat,log;
            String tit="Sitio",msj="";
            lat=Double.valueOf(getIntent().getStringExtra("lat"));
            log=Double.valueOf(getIntent().getStringExtra("long"));
            tit=getIntent().getStringExtra("titulo");
            msj="\""+getIntent().getStringExtra("tel")+"\". "+getIntent().getStringExtra("date");

            LatLng loc=new LatLng(lat,log);

            mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .title(tit)
                    .snippet(msj)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
                    .showInfoWindow();
        }

    }

    private void guardarLocal(Location location) {
        Double latitud = location.getLatitude();
        Double longitud = location.getLongitude();
        String dateTime = getDateTime();

        if (lastDate != dateTime) {
            lastDate = dateTime;
            contadorLocaciones = contadorLocaciones + 1;
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

            if (circleLocation != null) {
                if (circleLocation.isVisible()) {
                    circleLocation.remove();
                }
            }

            double radius=mMap.getMaxZoomLevel()+20;

            circleLocation=mMap.addCircle(new CircleOptions()
                    .fillColor(0x500000FF)
                    .center(loc)
                    .strokeColor(0x250000FF)
                    .strokeWidth((float) (radius/4))
                    .radius(radius));

            if (contadorLocaciones <= 2) {

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(loc));
            }

            Cursor cc = db.rawQuery("SELECT idRegistro from Usuario" + idUsuario, null);
            if (cc.moveToLast()) { //Si hay registros, sacamos el valor del ultimo id y le sumamos uno
                idRegistro = cc.getInt(0) + 1;
            }
            cc.close();

            ContentValues nuevoRegistro = new ContentValues();
            nuevoRegistro.put("idRegistro", idRegistro);
            nuevoRegistro.put("latitud", latitud);
            nuevoRegistro.put("longitud", longitud);
            nuevoRegistro.put("fechaHora", dateTime);
            nuevoRegistro.put("enviado", 2);

            db.insert("Usuario" + idUsuario, null, nuevoRegistro);

            //Una vez guardadas las locaciones localmente se llama el metodo para subirlas
            //Si hay conexion y falta comprobar las preferencias para saber
            // si el usuario configuro poder subirlas
            if (isConnected.isConnectedToInternet(MapsActivity.this)) {
                if (subir) {
                    subirLocacion(idRegistro, latitud, longitud, dateTime);
                }
            }

            Log.e("idUuario", idUsuario + "");
            Log.e("latitud", latitud + "");
            Log.e("longitud", longitud + "");
            Log.e("fechaHora", dateTime + "");
        }

        //se hace una cosulta para comprobar si hay registros sin subir
        //luego un do while para subir cada registro
        Cursor cursorSinSubir =
                db.rawQuery("SELECT idRegistro, latitud, longitud, fechaHora from Usuario" + idUsuario + " where enviado=0", null);
        if (cursorSinSubir.moveToFirst()) { //Si hay registros, nos posicionamos en el primero (el mas viejo)
            do {
                int idRegistro0 = cursorSinSubir.getInt(0);
                Double latitud0 = cursorSinSubir.getDouble(1);
                Double longitud0 = cursorSinSubir.getDouble(2);
                String dateTime0 = cursorSinSubir.getString(3);
                Log.e("Registro de ubicaciones", "Ubicacion sin subir con fecha y hora: " + dateTime0);
                if (isConnected.isConnectedToInternet(MapsActivity.this)) {
                    if (subir) {
                        subirLocacion(idRegistro, latitud, longitud, dateTime);
                    }
                    Log.e("Registro de ubicaciones", "Ubicacion sin subir con fecha y hora: " + dateTime0
                            + " se ha colocado en la cola de subida");
                }
            } while (cursorSinSubir.moveToNext());
        }
        cursorSinSubir.close();
    }

    private void  subirAlerta(final Double latitud, final Double longitud, final String dateTime, String comentarios) {

        /**
         al_latitud
         al_longitud
         us_id_usuario
         al_fecha_hora_registro
         al_comentarios
         */

        if (idUsuario != "0" || idUsuario == null || idUsuario == "" || idUsuario.contains("ERROR")) {
            //mostrar e inicializar progressdialog
            AsyncHttpClient client = new AsyncHttpClient();
            client.setResponseTimeout(20000);
            client.setConnectTimeout(20000);
            client.setTimeout(20000);
            RequestParams params = new RequestParams();
            params.put("us_id_usuario", idUsuario);
            params.put("al_latitud", latitud);
            params.put("al_longitud", longitud);
            params.put("al_fecha_hora_registro", dateTime);
            params.put("al_comentarios", comentarios);
            client.post(getUrlRegistrarAlertas, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    boolean registrado = false;
                    int idRegistroAJ = 0;
                    String response = new String(responseBody), mensaje = "Error, alerta no registrada.";
                    Log.e("RegistroAlerta", response);

                    /**
                     devuelve si se registro bien la alerta
                     {"al_id_alerta":8,"registrado":true,"mensaje":"Registrado correctamente"}
                     devuelve en caso de error
                     {"al_id_alerta":0,"registrado":false,"mensaje":"Todos los campos son requeridos"}
                     */

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        registrado = (Boolean) (jsonObject).get("registrado");
                        idRegistroAJ = (Integer) (jsonObject).get("al_id_alerta");
                        mensaje = (String) (jsonObject).get("mensaje");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //progressDialog.dismiss();
                    //Si se  el regsitro en el servidor, se cambia el valor "arriba" a 1
                    //de ese registro
                    if (registrado) {
                        ContentValues nuevoRegistro = new ContentValues();
                        nuevoRegistro.put("id", idRegistroAJ);
                        nuevoRegistro.put("latitud", latitud);
                        nuevoRegistro.put("longitud", longitud);
                        nuevoRegistro.put("fechaHora", dateTime);
                        nuevoRegistro.put("arriba", 1);

                        db.insert("Alertas", null, nuevoRegistro);

                        Toast.makeText(MapsActivity.this, "Alerta exitosa.", Toast.LENGTH_SHORT).show();
                        Log.e("subirAlerta:OK", dateTime);
                    } else {
                        Toast.makeText(MapsActivity.this, mensaje, Toast.LENGTH_SHORT).show();
                        Log.e("subirAlerta:FAILED", dateTime);
                        //Agregar codigo de error
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable bbbbbbberror) {
                    //progressDialog.dismiss();
                    //Update codigo de error
                    Toast.makeText(MapsActivity.this, "Error de conexion" + " " + dateTime + " Alerta no registrada", Toast.LENGTH_SHORT).show();
                    Log.e("subirAlerta:FAILED", dateTime + " statusCode=" + statusCode);
                }
            });

        } else {
            new AlertDialog.Builder(MapsActivity.this)
                    .setMessage("Algo ha salido mal, vuelve a loguarte o intentalo mas tarde." +
                            "Error 002.")
                    .setTitle("¡Huy!")
                    .setCancelable(false)
                    .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                            startActivity(intent);
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .show();
        }
    }

    private void subirLocacion(final int idRegistrof, Double latitud, Double longitud, final String dateTime) {
        /**
         $us_id_usuario  			= el mismo que devolvi en login;
         $re_latitud                             =  '19.703664389507487';
         $re_longitud                          =  '-101.20612395';
         $re_fecha_hora_registro     = ejemplo :  '2016-06-15 06:06:06';
         */

        if (idUsuario != "0" || idUsuario == null || idUsuario == "" || idUsuario.contains("ERROR")) {
            //mostrar e inicializar progressdialog
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("us_id_usuario", idUsuario);
            params.put("re_latitud", latitud);
            params.put("re_longitud", longitud);
            params.put("re_fecha_hora_registro", dateTime);
            Log.e("WS coordenadas", "idusuario: " + idUsuario);
            client.post(urlRegistrarCoordenadas, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    boolean registrado = false;
                    boolean hayAlerta = false;
                    String response = new String(responseBody);
                    Log.e("RegistroCoordenadas", response);

                    /**
                     {"registrado":true,
                     "mensaje":"Registrado correctamente",
                     "hay_alertas":true,

                     "array_alertas":[{"al_id_alerta":"2",
                     "al_latitud":"19.6769786",
                     "al_longitud":"-101.2251047",
                     "al_status":"1",
                     "us_id_usuario":"1",
                     "al_fecha_hora_registro":"2016-06-28 04:42:02",
                     "al_fecha_hora_registro_servidor":"2016-06-28 04:42:02"},


                     {"al_id_alerta":"3",
                     "al_latitud":"19.6769786",
                     "al_longitud":"-101.2251047",
                     "al_status":"1",
                     "us_id_usuario":"1",
                     "al_fecha_hora_registro":"2016-06-28 04:42:02",
                     "al_fecha_hora_registro_servidor":"2016-06-28 04:42:02"}]}

                     {"registrado":true,"mensaje":"Registrado correctamente","hay_alertas":false,"array_alertas":[]}
                     */

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        registrado = (Boolean) (jsonObject).get("registrado");
                        hayAlerta = (Boolean) (jsonObject).get("hay_alertas");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //progressDialog.dismiss();
                    //Si se confirma el regsitro en el servidor, se cambia el valor "enviado" a 1
                    //de ese resgitro
                    if (registrado) {
                        ContentValues nuevoRegistro = new ContentValues();
                        nuevoRegistro.put("enviado", 1);
                        db.update("Usuario" + idUsuario, nuevoRegistro, "idRegistro=" + idRegistrof, null);
                        if (toast){
                            Toast.makeText(MapsActivity.this, "Ubicación " + dateTime + " exitosa.", Toast.LENGTH_SHORT).show();
                        }
                        Log.e("subirLocacion:OK", dateTime);
                    } else {
                        ContentValues nuevoRegistro = new ContentValues();
                        nuevoRegistro.put("enviado", 0);
                        db.update("Usuario" + idUsuario, nuevoRegistro, "idRegistro=" + idRegistrof, null);
                        Toast.makeText(MapsActivity.this, "Ubicación " + dateTime + " fallida.", Toast.LENGTH_SHORT).show();
                        Log.e("subirLocacion:FAILED", dateTime);
                    }

                    if (hayAlerta) {
                        int idAlerta, idUsuarioA, statusA;
                        double latitudA, longitudA;
                        String fechaHoraA, fechaHoraSA,usUsuario,comentarios;
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("array_alertas");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Alertas alertas = new Alertas();
                                idAlerta = jsonArray.getJSONObject(i).getInt("al_id_alerta");
                                idUsuarioA = jsonArray.getJSONObject(i).getInt("al_status");
                                statusA = jsonArray.getJSONObject(i).getInt("us_id_usuario");
                                latitudA = jsonArray.getJSONObject(i).getDouble("al_latitud");
                                longitudA = jsonArray.getJSONObject(i).getDouble("al_longitud");
                                fechaHoraA = jsonArray.getJSONObject(i).getString("al_fecha_hora_registro");
                                fechaHoraSA = jsonArray.getJSONObject(i).getString("al_fecha_hora_registro_servidor");
                                comentarios = jsonArray.getJSONObject(i).getString("al_comentarios");
                                usUsuario = jsonArray.getJSONObject(i).getString("us_usuario");
                                alertas.setidAlerta(idAlerta);
                                alertas.setIdUsuarioAlerta(idUsuarioA);
                                alertas.setStatusAlerta(statusA);
                                alertas.setLatitudAlerta(latitudA);
                                alertas.setLongitudAlerta(longitudA);
                                alertas.setDateTimeAlerta(fechaHoraA);
                                alertas.setDateTimeServerAlerta(fechaHoraSA);
                                alertas.setComentariosAlerta(comentarios);
                                alertas.setusUsuario(usUsuario);

                                //Agregar datos faltantes a la clase de alertas

                                if (i>listaAlertas.size()){
                                    listaAlertas.add(alertas);
                                    Log.e("for hayAlerta", "id alerta=" + idAlerta);
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        comprobarAlertas();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    //progressDialog.dismiss();
                    ContentValues nuevoRegistro = new ContentValues();
                    nuevoRegistro.put("enviado", 0);
                    db.update("Usuario" + idUsuario, nuevoRegistro, "idRegistro=" + idRegistrof, null);
                    Toast.makeText(getApplicationContext(), "Ubicacion. Error de conexion." +
                            "Error 001. Codigo de status " + statusCode, Toast.LENGTH_LONG).show();
                }
            });

        } else {
            new AlertDialog.Builder(MapsActivity.this)
                    .setMessage("Algo ha salido mal, vuelve a loguarte o intentalo mas tarde." +
                            "Error 002.")
                    .setTitle("¡Huy!")
                    .setCancelable(false)
                    .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                            startActivity(intent);
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .show();
        }
    }

    private void comprobarAlertas() {
        int lastID = 0, bdSize=0;
        contadorAlertas = 0;
        Cursor ccc = db.rawQuery("SELECT id from Alertas", null);
        if (ccc.moveToLast()) { //Si hay registros, nos posicionamos en el ultimo (el mas nuevo)
            lastID = ccc.getInt(0);
        }
        if (ccc.moveToFirst()) { //Si hay registros, nos posicionamos en el primero (el mas viejo)
            do {
                bdSize=bdSize+1;
            } while (ccc.moveToNext()); //aumentamos mientras haya siguiente
        }
        ccc.close();

        int lastIDList=listaAlertas.get(listaAlertas.size()-1).getidAlerta();
        Log.e("comprobarAlertas", "lastIDList="+lastIDList+" lastID="+lastID);

        if (lastIDList > lastID) {
            int y = listaAlertas.size() - bdSize; //y=Numero de veces que se hara un regsitro
            int index = listaAlertas.size()-1 - y;

            int id;
            Double lat;
            Double log;
            String fecha = "Tal dia y tal hora";
            String fechaS;
            int idU;
            int stat;
            String comens = "Comentarios";
            String usU = "Usuario";

            Log.e("comprobarAlertas", "y="+y+" index="+index);
            for (int i = 0; i < y; i++) {
                index = index + 1;
                contadorAlertas = contadorAlertas + 1;
                ContentValues nuevoRegistro = new ContentValues();
                id = listaAlertas.get(index).getidAlerta();
                lat = listaAlertas.get(index).getLatitudAlerta();
                log = listaAlertas.get(index).getLongitudAlerta();
                fecha = listaAlertas.get(index).getDateTimeAlerta();
                fechaS = listaAlertas.get(index).getDateTimeServerAlerta();
                idU = listaAlertas.get(index).getIdUsuarioAlerta();
                stat = listaAlertas.get(index).getStatusAlerta();
                comens = listaAlertas.get(index).getComentariosAlerta();
                usU = listaAlertas.get(index).getusUsuario();

                nuevoRegistro.put("id", id);
                nuevoRegistro.put("latitud", lat);
                nuevoRegistro.put("longitud", log);
                nuevoRegistro.put("fechaHora", fecha);
                nuevoRegistro.put("fechaHoraS", fechaS);
                nuevoRegistro.put("idUsuario", idU);
                nuevoRegistro.put("status", stat);
                nuevoRegistro.put("arriba", 1);
                nuevoRegistro.put("comentarios", comens);
                nuevoRegistro.put("usUsuario", usU);

                db.insert("Alertas", null, nuevoRegistro);

                LatLng loc = new LatLng(lat, log);
                if (mMap!=null){
                    if (lat!=null && log !=null){
                        mMap.addMarker(new MarkerOptions()
                                .position(loc)
                                .title("Alerta de "+usU)
                                .snippet("\""+comens+".\""+" "+fecha)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                        //Comprobar markes pendientes
                    }
                }

                Log.e("comprobarAlertas", "Insertado nuevo id=" + id);

                //Las notificaciones se agruparan por usuario (idU)
                if (notificar) {
                    notificar("Alerta de " + usU, "\"" + comens + ".\"" + " " + fecha, 1, idU, contadorAlertas, loc);
                }
            }
            if (contadorAlertas==1){
                if (!pauso){
                    if (sonido){

                        if (soundAlert == null) {
                            if (preferences.getString(PreferenciasFragment.getKeyElegirSonidoLst(),"1").contains("0")){
                                soundAlert = MediaPlayer.create(this, R.raw.gamejump);
                            }else {
                                if (preferences.getString(PreferenciasFragment.getKeyElegirSonidoLst(),"1").contains("2")){
                                    soundAlert = MediaPlayer.create(this, R.raw.alert);
                                }else {
                                    soundAlert = MediaPlayer.create(this, R.raw.sweetpolite);
                                }
                            }
                        }

                        soundAlert.start();

                        soundAlert.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                mp.reset();
                                Log.e("media","onError");
                                return true;
                            }
                        });

                        soundAlert.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mp.release();
                                soundAlert = null;
                                Log.e("media","onCompletion");
                            }
                        });

                    }


                    new AlertDialog.Builder(MapsActivity.this)
                            .setTitle("Alerta de "+usU)
                            .setMessage("\""+comens+"\"."+" "+fecha)
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.StringAceptar), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    notifico=false;
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }else {

                if (!pauso && sonido) {

                    if (soundAlert == null) {
                        if (preferences.getString(PreferenciasFragment.getKeyElegirSonidoLst(),"1").contains("0")){
                            soundAlert = MediaPlayer.create(this, R.raw.gamejump);
                        }else {
                            if (preferences.getString(PreferenciasFragment.getKeyElegirSonidoLst(),"1").contains("2")){
                                soundAlert = MediaPlayer.create(this, R.raw.alert);
                            }else {
                                soundAlert = MediaPlayer.create(this, R.raw.sweetpolite);
                            }
                        }
                    }

                    soundAlert.start();

                    soundAlert.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            mp.reset();
                            Log.e("media","onError");
                            return true;
                        }
                    });

                    soundAlert.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                            soundAlert = null;
                            Log.e("media","onCompletion");
                        }
                    });

                }

                Toast.makeText(this, "Tienes " + contadorAlertas + " alertas nuevas", Toast.LENGTH_LONG).show();
                notifico=false;
            }
        }
        contadorAlertas = 0;
    }

    private void notificar(String titulo, String mensaje, int codigo, int mId,
                           @Nullable int numAlertas, @Nullable LatLng loc){
        notifico=true;

        if (pauso) {
            contadorNotificaciones = contadorNotificaciones + 1;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("NOTIFICACIONES", contadorNotificaciones);
            editor.commit();
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setVibrate(new long[] { 200, 200, 200, 200, 200 })
                        .setNumber(contadorNotificaciones)
                        .setContentTitle(titulo)
                        .setContentText(mensaje);

        Intent resultIntent;

        switch (codigo){ //Personalizar posible sonido respecto del codigo
            case 1: //Notificacion de alertas
                resultIntent = new Intent(this, MapsActivity.class);
                if (loc!=null) {
                    resultIntent.putExtra("Latitud" + numAlertas, loc.latitude);
                    resultIntent.putExtra("Longitud" + numAlertas, loc.longitude);
                    resultIntent.putExtra("Titulo" + numAlertas, titulo);
                    resultIntent.putExtra("Mensaje" + numAlertas, mensaje);
                }
                break;
            default:
                resultIntent = new Intent(this, MainActivity.class);
        }
        resultIntent.addCategory("NOTIFICATION");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(MainActivity.class);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(mId, mBuilder.build());
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauso=true;
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        if (!finalizar){
            if (toast){
                Toast.makeText(getApplicationContext(), "Las ubicaciones se actualizarán en segundo plano",
                        Toast.LENGTH_LONG)
                        .show();
            }

            startService(intentService);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("onRestart","");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (soundAlert == null) {
            if (preferences.getString(PreferenciasFragment.getKeyElegirSonidoLst(),"1").contains("0")){
                soundAlert = MediaPlayer.create(this, R.raw.gamejump);
            }else {
                if (preferences.getString(PreferenciasFragment.getKeyElegirSonidoLst(),"1").contains("2")){
                    soundAlert = MediaPlayer.create(this, R.raw.alert);
                }else {
                    soundAlert = MediaPlayer.create(this, R.raw.sweetpolite);
                }
            }
        }

        if (mMap!=null){
            contadorNotificaciones=preferences.getInt("NOTIFICACIONES",0);
            if (contadorNotificaciones>0){
                for (int i=0; i<=contadorNotificaciones; i++){
                    if (getIntent().hasExtra("Latitud"+i)){
                        if (getIntent().hasExtra("Latitud"+i)){
                            if (getIntent().hasExtra("Titulo"+i)){
                                if (getIntent().hasExtra("Mensaje"+i)){
                                    LatLng loc=new LatLng(getIntent().getDoubleExtra("Latitud"+i,0),
                                            getIntent().getDoubleExtra("Longitud"+i,0));
                                    String titulo=getIntent().getStringExtra("Titulo"+i);
                                    String mensaje=getIntent().getStringExtra("Mensaje"+i);
                                    mMap.addMarker(new MarkerOptions()
                                            .position(loc)
                                            .title(titulo)
                                            .snippet(mensaje)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                                }
                            }
                        }
                    } //Hasta aqui para notificaciones en primer plano
                }//termina for de arriba

                if (getIntent().hasExtra("idAlerta")){
                    int idAlert=getIntent().getIntExtra("idAlerta",0);
                    if (idAlert!=0){
                        for (int i=contadorNotificaciones; i>0; i--){
                            Log.e("onMapReady", "idAlert is not 0. for="+idAlert);
                            Cursor cc = db.rawQuery("SELECT id, latitud, longitud, fechaHora," +
                                    "fechaHoraS, idUsuario, status, comentarios, usUsuario from Alertas WHERE id="+idAlert, null);
                            if (cc.moveToFirst()) { //Si hay registros, nos posicionamos en el primero (el mas viejo)
                                LatLng loc=new LatLng(cc.getDouble(1),cc.getDouble(2));
                                String titulo="Alerta de "+cc.getString(8);
                                String mensaje="\"" + cc.getString(7) + "\". " + cc.getString(3);

                                mMap.addMarker(new MarkerOptions()
                                        .position(loc)
                                        .title(titulo)
                                        .snippet(mensaje)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
                            }
                            cc.close();
                            idAlert=idAlert-1;
                        }
                    } else {
                        Log.e("onMapReady", "idAlert is 0 = "+idAlert);
                    }
                }

                contadorNotificaciones=0;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("NOTIFICACIONES", 0);
                editor.commit();

                notifico=false;
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancelAll();

            }else {
                Log.e("onResume","Las preferencias no tienen contador de notificaciones");
            }

            if (getIntent().hasCategory("hAlert")){
                double lat,log;
                String tit="Alerta",msj="";
                lat=getIntent().getDoubleExtra("lat",0);
                log=getIntent().getDoubleExtra("long",0);
                tit=getIntent().getStringExtra("us");
                msj="\""+getIntent().getStringExtra("comen")+"\". "+getIntent().getStringExtra("date");

                LatLng loc=new LatLng(lat,log);

                mMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .title(tit)
                        .snippet(msj)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
                        .showInfoWindow();

            }

            if (getIntent().hasCategory("hSitio")){
                double lat,log;
                String tit="Sitio",msj="";
                lat=Double.valueOf(getIntent().getStringExtra("lat"));
                log=Double.valueOf(getIntent().getStringExtra("long"));
                tit=getIntent().getStringExtra("titulo");
                msj="\""+getIntent().getStringExtra("tel")+"\". "+getIntent().getStringExtra("date");

                LatLng loc=new LatLng(lat,log);

                mMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .title(tit)
                        .snippet(msj)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
                        .showInfoWindow();
            }

        }

        if (pauso || fueAjustes) {

            if (pauso) pauso=false;

            if (fueAjustes) fueAjustes = false;

            if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()){

                requestingLocations=false;

                //prob try catch
                try {
                    LocationServices.FusedLocationApi.removeLocationUpdates(
                            mGoogleApiClient, this);
                }catch (Exception ignored){

                }

                mGoogleApiClient.disconnect();

                MIN_DISTANCE_CHANGE_FOR_UPDATES = preferences.getLong(getString(R.string.StringFrecMetros), 1); // meters
                MIN_TIME_BW_UPDATES = 1000 * preferences.getInt(getString(R.string.StringFrecSegundos), 10); // secs

                requestingLocations=true;

                mGoogleApiClient.connect();

                Log.e("onResume", "mGC was ON. Segundos: " + MIN_TIME_BW_UPDATES
                        + " Metros: " + MIN_DISTANCE_CHANGE_FOR_UPDATES);
            }else {
                MIN_DISTANCE_CHANGE_FOR_UPDATES = preferences.getLong(getString(R.string.StringFrecMetros), 1); // meters
                MIN_TIME_BW_UPDATES = 1000 * preferences.getInt(getString(R.string.StringFrecSegundos), 10); // secs

                requestingLocations=true;

                mGoogleApiClient.connect();
            }

        }

        notificar=preferences.getBoolean(PreferenciasFragment.getKeyIconoBarraChk(), false);
        sonido=preferences.getBoolean(PreferenciasFragment.getKeySonidoChk(), false);
        subir=preferences.getBoolean(PreferenciasFragment.getKeySincronizacionChk(),true);
        toast=preferences.getBoolean(PreferenciasFragment.getKeyToast(), false);
        if (preferences.getBoolean(PreferenciasFragment.getKeyMantenerSesionChk(), false)){
            finalizar=false;
        }

        if (getIntent().hasCategory("NOTIFICATION")){
            notifico=false;
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancelAll();
        }

        stopService(intentService);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (finalizar) {
            if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()){
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
            stopService(intentService);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.cerrarSesion:
                final SharedPreferences.Editor editor = preferences.edit();
                new AlertDialog.Builder(MapsActivity.this)
                        .setMessage("¿Esta seguro que desea cerrar la sesión actual?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                editor.putBoolean(PreferenciasFragment.getKeyMantenerSesionChk(), false); //getkey from fragment
                                editor.remove(getString(R.string.StringIDUsuarioGuardado));
                                editor.remove(getString(R.string.StringContraseñaGuardada));
                                editor.remove(getString(R.string.StringNombreUsuarioGuardado));
                                editor.remove(getString(R.string.StringNickUsuarioGuardado));
                                editor.remove(getString(R.string.StringURLFotoUsuarioGuardada));
                                editor.commit();

                                finalizar = true;

                                dialog.dismiss();
                                Intent intentm = new Intent(MapsActivity.this, MainActivity.class);
                                startActivity(intentm);
                                Toast.makeText(MapsActivity.this, "Hasta pronto", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;

            case R.id.preferencias:
                Intent intentp= new Intent(MapsActivity.this, PreferenciasActivity.class);

                intentp.putExtra("idUsuario",idUsuario);

                startActivity(intentp);
                return true;

            case R.id.agregarLugar:
                Intent intent1= new Intent(MapsActivity.this, SitiosOption.class);

                intent1.putExtra("idUsuario",idUsuario);

                startActivity(intent1);
                return true;

            case R.id.buscar:
                Toast.makeText(getApplicationContext(), "¡UPS! Estamos trabajando en esta opción ;)",
                        Toast.LENGTH_LONG)
                        .show();
                return true;

            case R.id.hAlertas:
                Intent intent= new Intent(MapsActivity.this, HistorialAlertas.class);

                intent.putExtra("idUsuario",idUsuario);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    public void getPeriodicFusedLocation() {
        final Context context = this;

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(MIN_TIME_BW_UPDATES);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                            builder.build());

            final LocationRequest finalMLocationRequest = mLocationRequest;
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                    final Status status = locationSettingsResult.getStatus();

                    //final LocationSettingsStates LocationSettingsStates = locationSettingsResult.getLocationSettingsStates();

                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can
                            // initialize location requests here.
                            Log.e("getPeriodicFusedLocatio","SUCCES");
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            LocationServices.FusedLocationApi.requestLocationUpdates(
                                    mGoogleApiClient, finalMLocationRequest, MapsActivity.this);

                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied, but this can be fixed
                            // by showing the user a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                fueAjustes=true;
                                status.startResolutionForResult(
                                        MapsActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                                Log.e("getPeriodicFusedLocatio","Error in RESOLUTION_REQUIRED");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way
                            // to fix the settings so we won't show the dialog.

                            new AlertDialog.Builder(MapsActivity.this)
                                    .setTitle("¡UPS!")
                                    .setMessage("Hay un problema con los requerimientos para utilizar esta aplicacion." +
                                            "Por favor revisa tu dispositivo o ajustes antes de continuar.")
                                    //Codigo de error
                                    .setNeutralButton(getString(R.string.StringCancelar), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            final SharedPreferences.Editor editor = preferences.edit();

                                            editor.putBoolean(PreferenciasFragment.getKeyMantenerSesionChk(), false);
                                            editor.remove(getString(R.string.StringIDUsuarioGuardado));
                                            editor.remove(getString(R.string.StringContraseñaGuardada));
                                            editor.remove(getString(R.string.StringNombreUsuarioGuardado));
                                            editor.remove(getString(R.string.StringNickUsuarioGuardado));
                                            editor.remove(getString(R.string.StringURLFotoUsuarioGuardada));
                                            editor.commit();

                                            finalizar = true;

                                            dialog.dismiss();
                                            Intent intentm = new Intent(MapsActivity.this, MainActivity.class);
                                            startActivity(intentm);
                                            Toast.makeText(MapsActivity.this, "Hasta pronto", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    })
                                    .show();

                            Log.e("getPeriodicFusedLocatio","Error=SETTINGS_CHANGE_UNAVAILABLE");
                            break;
                    }
                }
            });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("onConencted","");
        if (requestingLocations){
            requestingLocations=false;
            getPeriodicFusedLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("onConenctionSuspended",i+"");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("onConenctionFailed",connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        if (locacion==null || location.getTime()>locacion.getTime()){
            if (isBetter.isBetterLocation(location,locacion)){
                locacion=location;
                guardarLocal(locacion);
                Log.e("onLocationChanged",location.toString());
            }
        }
    }
}
