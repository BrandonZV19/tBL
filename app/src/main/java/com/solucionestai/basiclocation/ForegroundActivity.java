package com.solucionestai.basiclocation;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

/**
 * Created by TAI on 19/07/2016.
 */
public class ForegroundActivity extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient mGoogleApiClient;
    Boolean requestingLocations=false,notificar=true, subir=true, toast;
    SharedPreferences preferences;
    long MIN_TIME_BW_UPDATES;
    Location locacion;
    isBetterLocation isBetter = new isBetterLocation();
    String lastDate = "", idUsuario="",
            urlRegistrarCoordenadas = "http://191.101.156.66/erp/ws_gps/ws_registro_coordenadas";
    int contadorLocaciones=0, idRegistro=0, idRegistroA, contadorNotificaciones;
    DBConnection dbc;
    SQLiteDatabase db;
    isConnectedToInternet isConnected;
    ArrayList<Alertas> listaAlertas;
    MediaPlayer soundAlert;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        ForegroundActivity getService() {
            // Return this instance of LocalService so clients can call public methods
            return ForegroundActivity.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(ForegroundActivity.this)
                    .addOnConnectionFailedListener(ForegroundActivity.this)
                    .addApi(LocationServices.API)
                    .build();
        }

        dbc = new DBConnection(ForegroundActivity.this, "RegistrosLoc", null, 1);
        db = dbc.getWritableDatabase();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preferences = PreferenciasFragment.getPref(ForegroundActivity.this);
        MIN_TIME_BW_UPDATES = 1000 * preferences.getInt(getString(R.string.StringFrecSegundos), 60); // secs
        idUsuario = preferences.getString(getString(R.string.StringIDUsuarioGuardado), "ERROR");
        toast = preferences.getBoolean(PreferenciasFragment.getKeyToast(), false);

        contadorNotificaciones=0;

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

        isConnected = new isConnectedToInternet();
        if (isConnected.isConnectedToInternet(ForegroundActivity.this)){

            requestingLocations=true;

            if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()){
                mGoogleApiClient.connect();

                listaAlertas = new ArrayList<>();

                if (idUsuario != null && !idUsuario.contains("ERROR")) {
                    db.execSQL("DROP TABLE IF EXISTS Usuario" + idUsuario + "");
                    db.execSQL("CREATE TABLE Usuario" + idUsuario + "(idRegistro INTEGER PRIMARY KEY, latitud REAL, longitud REAL," +
                            "fechaHora DATETIME, enviado INTEGER)");

                    db.execSQL("CREATE TABLE IF NOT EXISTS Alertas(id INTEGER PRIMARY KEY, latitud REAL," +
                            "longitud REAL, fechaHora DATETIME, fechaHoraS DATETIME, idUsuario INTEGER," +
                            "status INTEGER,arriba INTEGER, comentarios TEXT, usUsuario TEXT)");

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
                            Log.e("onStartCommand", "idAlerta: " + cc.getInt(0));
                        } while (cc.moveToNext());
                    }
                    cc.close();

                } else {
                    //Agregar codigo de error
                    Toast.makeText(getApplicationContext(), "Por favor inicia sesión.", Toast.LENGTH_LONG)
                            .show();
                    stopSelf();
                }
            }

        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Actualizaciones de ubicacion en segundo plano")
                        .setVibrate(new long[] { 500, 400, 200, 400, 200, 400 })
                        .setContentText("Soluciones T@i esta registrando tu ubicacion en segundo plano");

        notificar=preferences.getBoolean(PreferenciasFragment.getKeyIconoBarraChk(),true);
        subir=preferences.getBoolean(PreferenciasFragment.getKeySincronizacionChk(),true);

        if (notificar){
            Intent resultIntent;

            resultIntent = new Intent(this, MainActivity.class);

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

            startForeground(2,mBuilder.build());
        }
        return START_STICKY_COMPATIBILITY;
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
                        Log.e("FA getPridcFsdLctn","SUCCES");
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
                                mGoogleApiClient, finalMLocationRequest, ForegroundActivity.this);

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    (Activity) getApplicationContext(), 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                            Log.e("FA getPrdcFsdLct","Error in RESOLUTION_REQUIRED");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        new AlertDialog.Builder(ForegroundActivity.this)
                                .setTitle("¡UPS!")
                                .setMessage("Hay un problema con los requerimientos para utilizar esta aplicacion." +
                                        "Por favor revisa tu dispositivo o tus ajustes antes de continuar.")
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

                                        requestingLocations = false;

                                        dialog.dismiss();
                                        Intent intentm = new Intent(ForegroundActivity.this, MainActivity.class);
                                        startActivity(intentm);
                                        Toast.makeText(ForegroundActivity.this, "Hasta pronto", Toast.LENGTH_SHORT).show();
                                        stopSelf();
                                    }
                                })
                                .show();

                        Log.e("FA gtPrdcFsdLct","Error=SETTINGS_CHANGE_UNAVAILABLE");
                        break;
                }
            }
        });
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void guardarLocal(Location location) {
        Double latitud = location.getLatitude();
        Double longitud = location.getLongitude();
        String dateTime = getDateTime();

        if (lastDate != dateTime) {
            lastDate = dateTime;
            contadorLocaciones = contadorLocaciones + 1;

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
            if (isConnected.isConnectedToInternet(ForegroundActivity.this)) {
                //subir a la nube
                if (subir) {
                    subirLocacion(idRegistro, latitud, longitud, dateTime);
                }
            }

            Log.e("FA idUuario", idUsuario + "");
            Log.e("FA latitud", latitud + "");
            Log.e("FA longitud", longitud + "");
            Log.e("FA fechaHora", dateTime + "");
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
                Log.e("FA Rgstr d ubccns", "Ubicacion sin subir con fecha y hora: " + dateTime0);
                if (isConnected.isConnectedToInternet(ForegroundActivity.this)) {
                    //Subir a la nuve registros locales sin subir
                    if (subir) {
                        subirLocacion(idRegistro, latitud, longitud, dateTime);
                    }
                    Log.e("FA Rgstr d ubccns", "Ubicacion sin subir con fecha y hora: " + dateTime0
                            + " se ha colocado en la cola de subida");
                }
            } while (cursorSinSubir.moveToNext());
        }
        cursorSinSubir.close();
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
            Log.e("FA WS coordenadas", "idusuario: " + idUsuario);
            client.post(urlRegistrarCoordenadas, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    boolean registrado = false;
                    boolean hayAlerta = false;
                    String response = new String(responseBody);
                    Log.e("FA RegistroCoordenadas", response);

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
                        if(toast){
                            Toast.makeText(ForegroundActivity.this, "Ubicación " + dateTime + " exitosa.", Toast.LENGTH_SHORT).show();
                        }
                        Log.e("FA subirLocacion:OK", dateTime);
                    } else {
                        ContentValues nuevoRegistro = new ContentValues();
                        nuevoRegistro.put("enviado", 0);
                        db.update("Usuario" + idUsuario, nuevoRegistro, "idRegistro=" + idRegistrof, null);
                        Toast.makeText(ForegroundActivity.this, "Ubicación " + dateTime + " fallida.", Toast.LENGTH_SHORT).show();
                        Log.e("FA subirLocacion:FAILED", dateTime);
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
                                    Log.e("FA for hayAlerta", "id alerta=" + idAlerta);
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
            new AlertDialog.Builder(ForegroundActivity.this)
                    .setMessage("Algo ha salido mal, vuelve a loguarte o vuelve mas tarde." +
                            "Error 002.")
                    .setTitle("¡Huy!")
                    .setCancelable(false)
                    .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(ForegroundActivity.this, MainActivity.class);
                            startActivity(intent);
                            dialog.dismiss();
                            stopSelf();
                        }
                    })
                    .show();
        }
    }

    private void comprobarAlertas() {
        int lastID = 0, bdSize=0;

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
        Log.e("FA comprobarAlertas", "lastIDList="+lastIDList+" lastID="+lastID);

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

            Log.e("FA comprobarAlertas", "y="+y+" index="+index);
            for (int i = 0; i < y; i++) {
                index = index + 1;

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

                Log.e("FA comprobarAlertas", "Insertado nuevo id=" + id);

                //Las notificaciones se agruparan por usuario (idU)
                notificar("Alerta de "+usU,"\""+comens+".\""+" "+fecha,1,idU,id);
            }
            if (preferences.getBoolean(PreferenciasFragment.getKeySonidoChk(), true)){

                if (soundAlert == null) {
                    if (preferences.getString(PreferenciasFragment.getKeyElegirSonidoLst(), "1").contains("0")) {
                        soundAlert = MediaPlayer.create(this, R.raw.gamejump);
                    } else {
                        soundAlert = MediaPlayer.create(this, R.raw.sweetpolite);
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
        }
    }

    private void notificar(String titulo, String mensaje, int codigo, int mId, int idAlerta){
        contadorNotificaciones = contadorNotificaciones + 1;
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("NOTIFICACIONES", contadorNotificaciones);
        editor.commit();
        Log.e("FA notificar", "contadorNotificaciones="+contadorNotificaciones);

        if (notificar) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setVibrate(new long[]{200, 200, 200, 200, 200})
                            .setNumber(contadorNotificaciones)
                            .setContentTitle(titulo)
                            .setContentText(mensaje);

            Intent resultIntent;

            switch (codigo) { //Personalizar posible sonido respecto del codigo
                case 1: //Notificacion de alertas
                    resultIntent = new Intent(this, MapsActivity.class);
                    resultIntent.putExtra("idAlerta", idAlerta);
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
    }

    public Location getCurrentLocation(){
        return locacion;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        requestingLocations=false;
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            try{
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient, this);
            }catch (Exception ignored){
            }

            mGoogleApiClient.disconnect();
            if (toast) {
                Toast.makeText(getApplicationContext(), "Servicio finalizado",
                        Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (requestingLocations){
            requestingLocations=false;
            getPeriodicFusedLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //cachar error y detener
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //cachar error y detener
    }

    @Override
    public void onLocationChanged(Location location) {
        if (locacion==null || location.getTime()>locacion.getTime()){
            if (isBetter.isBetterLocation(location,locacion)){
                locacion=location;
                guardarLocal(locacion);
                Log.e("FA onLocationChanged",location.toString());
            }
        }
    }


}
