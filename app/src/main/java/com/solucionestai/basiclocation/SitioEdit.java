package com.solucionestai.basiclocation;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

public class SitioEdit extends AppCompatActivity {
    boolean finalizar;
    String idUsuario,
            getUrlRegistrarSitio = "http://191.101.156.66/erp/ws_gps/ws_registro_lugar";
    SharedPreferences preferences;
    EditText eTitulo,eNombre,eApellidoP,eApellidoM,eCorreo,eCalle,eNumero,eTelefono,eCosto,eObservaciones;
    Button ok;
    ImageButton street;
    //ArrayList<Sitios> listaSitios;
    Double lu_latitud,lu_longitud;
    DBConnection dbc;
    SQLiteDatabase db;
    ProgressDialog progressDialog;
    //DecimalFormat df = new DecimalFormat("#.######");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_sitio);

        preferences = PreferenciasFragment.getPref(SitioEdit.this);

        if (getIntent().hasExtra("idUsuario")) {
            idUsuario = getIntent().getStringExtra("idUsuario");
            finalizar=true;
        } else {
            idUsuario = preferences.getString(getString(R.string.StringIDUsuarioGuardado), "ERROR");
        }

        if (idUsuario != "0" || idUsuario == null || idUsuario == "" || idUsuario.contains("ERROR")) {
            //Notify and out
        }

        progressDialog = new ProgressDialog(SitioEdit.this);
        progressDialog.setMax(100);
        progressDialog.setTitle("Espere . . .");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        dbc = new DBConnection(SitioEdit.this, "RegistrosLoc", null, 1);
        db = dbc.getWritableDatabase();

        db.execSQL("CREATE TABLE IF NOT EXISTS Sitios(id INTEGER PRIMARY KEY, titulo TEXT," +
                "nombre TEXT, apellidoP TEXT, apellidoM TEXT, calle TEXT, numero TEXT, telefono TEXT, " +
                "costo INTEGER, observaciones TEXT, idUsuario INTEGER, latitud REAL, longitud REAL," +
                " fechaHora DATETIME, arriba INTEGER, correo TEXT, colonia TEXT)");


        eNombre=(EditText)findViewById(R.id.nombreSi);
        eApellidoP=(EditText)findViewById(R.id.apellidoPSi);
        eApellidoM=(EditText)findViewById(R.id.apellidoMSi);
        eCorreo=(EditText)findViewById(R.id.correoSi);
        eCalle=(EditText)findViewById(R.id.calleSi);
        eNumero=(EditText)findViewById(R.id.numeroSi);
        eTelefono=(EditText)findViewById(R.id.telefonoSi);
        eCosto=(EditText)findViewById(R.id.costoSi);
        eObservaciones=(EditText)findViewById(R.id.observacionesSi);
        eTitulo=(EditText)findViewById(R.id.tituloSi);
        eTitulo.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == 66) {
                    eNombre.requestFocus();
                }
                return false;
            }
        });
        eTitulo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    eNombre.requestFocus();
                }
                return false;
            }
        });

        ok=(Button)findViewById(R.id.bOkSi);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (eNumero.getText().toString().trim().length()>0){
                        if (eCosto.getText().toString().trim().length()>0){

                            final String titulo=eTitulo.getText().toString().trim();
                            final String nombre=eNombre.getText().toString().trim();
                            final String apellidoP=eApellidoP.getText().toString().trim();
                            final String apellidoM=eApellidoM.getText().toString().trim();
                            final String correo=eCorreo.getText().toString().trim();
                            final String calle=eCalle.getText().toString().trim();
                            final int num=Integer.parseInt(eNumero.getText().toString().trim());
                            final String tel=eTelefono.getText().toString().trim();
                            final int costo=Integer.parseInt(eCosto.getText().toString().trim());
                            final String observaciones=eObservaciones.getText().toString().trim();

                            if (titulo.length()>1&&nombre.length()>1&&apellidoP.length()>1&&apellidoM.length()>1
                                    &&correo.length()>1&&calle.length()>1&&num>0&&tel.length()>6&&costo>0&&observaciones.length()>1){
                                if (validate(correo)){
                                    progressDialog.show();

                                    int lastID=0;
                                    Cursor ccc = db.rawQuery("SELECT id from Sitios", null);
                                    if (ccc.moveToLast()) { //Si hay registros, nos posicionamos en el ultimo (el mas nuevo)
                                        lastID = ccc.getInt(0);
                                    }
                                    ccc.close();

                                    final String dateTime = getDateTime();
                                    //listaSitios.clear();
                                    //Guardar dato tipo sitio
                                    Sitios sitio=new Sitios();
                                    //falta id
                                    sitio.setIdLugar(lastID+1);//samefix
                                    sitio.setTituloLu(titulo);
                                    sitio.setNombreLu(nombre);
                                    sitio.setApellidoPatLu(apellidoP);
                                    sitio.setApellidoMatLu(apellidoM);
                                    sitio.setCalleLu(calle);
                                    sitio.setNumeroLu(num);
                                    sitio.setTelefonoLu(tel);
                                    sitio.setCostoLu(costo);
                                    sitio.setObservacionesLu(observaciones);
                                    sitio.setIdUsuarioLu(idUsuario);
                                    sitio.setLatitudLu(String.valueOf(lu_latitud));
                                    sitio.setLongitudLu(String.valueOf(lu_longitud));
                                    sitio.setFechaHoraLu(dateTime);//samefix
                                    sitio.setArriba(3);
                                    sitio.setCorreoLu(correo);
                                    //listaSitios.add(sitio);

                                    //Faltahora de registro en servidor

                                    ok.setEnabled(false);

                                    //Subir y comprobar
                                    /**
                                     * $lu_titulo
                                     $lu_nombre_persona
                                     $lu_apellido_paterno_persona

                                     $lu_apellido_materno_persona

                                     $lu_nombre_calle
                                     $lu_numero_calle
                                     $lu_telefono_contacto
                                     $lu_costo
                                     $lu_observaciones
                                     $lu_fecha_hora_registro
                                     $lu_id_usuario
                                     $lu_latitud
                                     $lu_longitud
                                     --------------------------------
                                     Datos requeridos

                                     $lu_nombre_persona
                                     $lu_observaciones
                                     $lu_fecha_hora_registro
                                     $lu_id_usuario
                                     $lu_latitud
                                     $lu_longitud
                                     *
                                     lu_id_lugar *****************Retorned
                                     lu_titulo
                                     lu_nombre_persona
                                     lu_apellido_paterno_persona
                                     lu_apellido_materno_persona
                                     lu_nombre_calle
                                     lu_numero_calle
                                     lu_telefono_contacto
                                     lu_costo
                                     lu_observaciones
                                     lu_id_usuario
                                     lu_latitud
                                     lu_longitud
                                     lu_fecha_hora_registro
                                     lu_fecha_hora_registro_servidor ****Retorned
                                     */
                                    if (idUsuario != "0" || idUsuario == null || idUsuario == "" || idUsuario.contains("ERROR")) {
                                        AsyncHttpClient client = new AsyncHttpClient();
                                        client.setResponseTimeout(20000);
                                        client.setConnectTimeout(20000);
                                        client.setTimeout(20000);
                                        RequestParams params = new RequestParams();
                                        params.put("lu_titulo", titulo);
                                        params.put("lu_nombre_persona",nombre);
                                        params.put("lu_apellido_paterno_persona",apellidoP);
                                        params.put("lu_apellido_materno_persona",apellidoM);
                                        params.put("lu_nombre_calle",calle);
                                        params.put("lu_numero_calle",num);
                                        params.put("lu_telefono_contacto",tel);
                                        params.put("lu_costo",costo);
                                        params.put("lu_observaciones",observaciones);
                                        params.put("lu_id_usuario",idUsuario);
                                        params.put("lu_latitud",lu_latitud);
                                        params.put("lu_longitud",lu_longitud);
                                        params.put("lu_fecha_hora_registro",dateTime);
                                        params.put("lu_correo",correo);
                                        final int finalLastID = lastID;
                                        final int finalLastID1 = lastID;
                                        Log.e("BeforePost",params.toString());
                                        client.post(getUrlRegistrarSitio, params, new AsyncHttpResponseHandler() {
                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                boolean registrado = false;
                                                int idRegistroS = 0;
                                                String response = new String(responseBody), mensaje = "Error, sitio no registrado";
                                                Log.e("RegistroSitio", response);

                                                /**
                                                 {"lu_id_lugar":0,
                                                 "registrado":false,
                                                 "mensaje":"Los campos nombre de la persona,
                                                 observaciones,fecha de registro,latitud y longitud son requeridos ",
                                                 "array_lugar":""}
                                                 */

                                                try {
                                                    JSONObject jsonObject = new JSONObject(response);
                                                    registrado = (Boolean) (jsonObject).get("registrado");
                                                    idRegistroS = (Integer) (jsonObject).get("lu_id_lugar");
                                                    mensaje = (String) (jsonObject).get("mensaje");
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                if (registrado) {
                                                    ContentValues nuevoRegistro = new ContentValues();

                                                    nuevoRegistro.put("id", idRegistroS);
                                                    nuevoRegistro.put("titulo",titulo);
                                                    nuevoRegistro.put("nombre",nombre);
                                                    nuevoRegistro.put("apellidoP",apellidoP);
                                                    nuevoRegistro.put("apellidoM",apellidoM);
                                                    nuevoRegistro.put("calle",calle);
                                                    nuevoRegistro.put("numero",num);
                                                    nuevoRegistro.put("telefono",tel);
                                                    nuevoRegistro.put("costo",costo);
                                                    nuevoRegistro.put("observaciones",observaciones);
                                                    nuevoRegistro.put("idUsuario",idUsuario);
                                                    nuevoRegistro.put("latitud",lu_latitud);
                                                    nuevoRegistro.put("longitud",lu_longitud);
                                                    nuevoRegistro.put("fechaHora",dateTime);//corregir fecha
                                                    nuevoRegistro.put("arriba",1);//actualizar after

                                                    db.insert("Sitios", null, nuevoRegistro);

                                                    Toast.makeText(SitioEdit.this, "Sitio exitoso", Toast.LENGTH_SHORT).show();
                                                    Log.e("subirSitio:OK", idRegistroS+"");
                                                } else {
                                                    ContentValues nuevoRegistro = new ContentValues();

                                                    nuevoRegistro.put("id", finalLastID1 +1);//Corregir 0
                                                    nuevoRegistro.put("titulo",titulo);
                                                    nuevoRegistro.put("nombre",nombre);
                                                    nuevoRegistro.put("apellidoP",apellidoP);
                                                    nuevoRegistro.put("apellidoM",apellidoM);
                                                    nuevoRegistro.put("calle",calle);
                                                    nuevoRegistro.put("numero",num);
                                                    nuevoRegistro.put("telefono",tel);
                                                    nuevoRegistro.put("costo",costo);
                                                    nuevoRegistro.put("observaciones",observaciones);
                                                    nuevoRegistro.put("idUsuario",idUsuario);
                                                    nuevoRegistro.put("latitud",lu_latitud);
                                                    nuevoRegistro.put("longitud",lu_longitud);
                                                    nuevoRegistro.put("fechaHora",dateTime);//corregir fecha
                                                    nuevoRegistro.put("arriba",3);//actualizar after

                                                    db.insert("Sitios", null, nuevoRegistro);

                                                    Toast.makeText(SitioEdit.this, mensaje, Toast.LENGTH_SHORT).show();
                                                    Log.e("subirSitio:FAILED", idRegistroS+"");
                                                    //Agregar codigo de error
                                                }
                                                progressDialog.dismiss();
                                                finish();
                                            }

                                            @Override
                                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                                ContentValues nuevoRegistro = new ContentValues();

                                                nuevoRegistro.put("id", finalLastID1 +1);//Corregir 0
                                                nuevoRegistro.put("titulo",titulo);
                                                nuevoRegistro.put("nombre",nombre);
                                                nuevoRegistro.put("apellidoP",apellidoP);
                                                nuevoRegistro.put("apellidoM",apellidoM);
                                                nuevoRegistro.put("calle",calle);
                                                nuevoRegistro.put("numero",num);
                                                nuevoRegistro.put("telefono",tel);
                                                nuevoRegistro.put("costo",costo);
                                                nuevoRegistro.put("observaciones",observaciones);
                                                nuevoRegistro.put("idUsuario",idUsuario);
                                                nuevoRegistro.put("latitud",lu_latitud);
                                                nuevoRegistro.put("longitud",lu_longitud);
                                                nuevoRegistro.put("fechaHora",dateTime);//corregir fecha
                                                nuevoRegistro.put("arriba",3);//actualizar after

                                                db.insert("Sitios", null, nuevoRegistro);

                                                Toast.makeText(SitioEdit.this, "SITIO:FAILED", Toast.LENGTH_SHORT).show();
                                                Log.e("subirSitio:FAILED", "statusCode "+statusCode+" "+ Arrays.toString(responseBody));
                                                //Agregar codigo de error
                                                progressDialog.dismiss();
                                                finish();
                                            }
                                        });

                                        //POST here
                                        //If post right arriba=1

                                    }else {
                                        new AlertDialog.Builder(SitioEdit.this)
                                                .setMessage("Algo ha salido mal, vuelve a loguarte o intentalo mas tarde." +
                                                        "Error 002.")
                                                .setTitle("¡Huy!")
                                                .setCancelable(false)
                                                .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Intent intent = new Intent(SitioEdit.this, MainActivity.class);
                                                        startActivity(intent);
                                                        dialog.dismiss();
                                                        progressDialog.dismiss();
                                                        finish();
                                                    }
                                                })
                                                .show();
                                    }
                                }else {
                                    Toast.makeText(SitioEdit.this,"Por favor ingresa un correo electronico valido"
                                            ,Toast.LENGTH_SHORT).show();
                                }

                            }else {
                                Toast.makeText(SitioEdit.this,"Algun campo es incorrecto"
                                        ,Toast.LENGTH_SHORT).show();
                            }

                        }else {
                            Toast.makeText(SitioEdit.this,"El costo no puede ser 0"
                                    ,Toast.LENGTH_SHORT).show();
                        }
                }else {
                    Toast.makeText(SitioEdit.this,"El numero telefonico debe contener minimo 7 caracteres"
                            ,Toast.LENGTH_SHORT).show();
                }

            }
        });

        street=(ImageButton)findViewById(R.id.bGetStreetSi);
        street.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SitioEdit.this, SelectSite.class);
                startActivity(intent);
            }
        });

    }


    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent=getIntent();
        if (intent.hasExtra("StreetLat") && intent.hasExtra("StreetLog")){
            lu_latitud=getIntent().getDoubleExtra("StreetLat",0);
            lu_longitud=getIntent().getDoubleExtra("StreetLog",0);
            if (lu_latitud!=0 && lu_longitud!=0){
                street.setEnabled(false);
                street.setBackgroundColor(Color.GRAY);
            }
        }else if (intent.hasExtra("MapLog") && intent.hasExtra("MapLat")){
            lu_latitud=getIntent().getDoubleExtra("MapLat",0);
            lu_longitud=getIntent().getDoubleExtra("MapLog",0);

            if (lu_latitud!=0 && lu_longitud!=0){
                street.setEnabled(false);
                street.setBackgroundColor(Color.GRAY);
            }
        }else {
            street.callOnClick();
        }


    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

}
