package com.solucionestai.basiclocation;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.preference.PreferenceFragment;
import android.support.multidex.MultiDex;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    Button ingresar;
    EditText eUser,ePass;
    boolean sesionActiva;
    //int idUsuario;
    String sUser,sPass, nickUsuario,nombreUsuario,urlFotoUsuario,mensaje,idUsuario,
            urlLogin="http://191.101.156.66/erp/ws_gps/ws_login";
    ProgressDialog progressDialog;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences= PreferenciasFragment.getPref(MainActivity.this);

        if (preferences.getBoolean(PreferenciasFragment.getKeyMantenerSesionChk(),false)) {
            setContentView(R.layout.activity_main_logged);
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
            finish();
        } else{
            setContentView(R.layout.activity_main);

        setTitle("Login");

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMax(100);
        progressDialog.setTitle("Espere . . .");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        eUser = (EditText) findViewById(R.id.eUser);
        ePass = (EditText) findViewById(R.id.ePass);
        ingresar = (Button) findViewById(R.id.bLogin);
        ingresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!eUser.getText().toString().isEmpty() && !ePass.getText().toString().isEmpty()) {
                    progressDialog.show();
                    ingresar.setEnabled(false);

                    sUser = eUser.getText()
                            .toString()
                            .trim();

                    sPass = ePass.getText()
                            .toString()
                            .trim();

                    //No hay ningun cifrado de contraseñas

                    AsyncHttpClient client = new AsyncHttpClient();
                    RequestParams params = new RequestParams();
                    params.add("us_usuario", sUser);
                    params.add("us_contrasenia", sPass);
                    client.post(urlLogin, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String response = new String(responseBody);
                            Log.e("LoginResponse", response);

                            /**
                             "us_id_usuario"    = id del usuario
                             "us_usuario"       = nombre del usuario
                             "us_fotografia"    = url de la fotografia,
                             "us_nombre_persona"= nombre completo con apellidos
                             "us_sesion_activa" = true o false ,
                             "mensaje"          = mensaje ya sea de bienvenida o de error
                             */

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                idUsuario = (String) (jsonObject).get("us_id_usuario");
                                nickUsuario = (String) (jsonObject).get("us_usuario");
                                urlFotoUsuario = (String) (jsonObject).get("us_fotografia");
                                nombreUsuario = (String) (jsonObject).get("us_nombre_persona");
                                sesionActiva = (Boolean) (jsonObject).get("us_sesion_activa");
                                mensaje = (String) (jsonObject).get("mensaje");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (sesionActiva) {

                                new AlertDialog.Builder(MainActivity.this)
                                        .setMessage("¿Deseas mantener tu sesión activa?")
                                        .setCancelable(false)
                                        .setNegativeButton("No gracias", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                SharedPreferences.Editor editor = preferences.edit();
                                                editor.putString(getString(R.string.StringIDUsuarioGuardado), idUsuario);
                                                editor.putString(getString(R.string.StringContraseñaGuardada), sPass);
                                                editor.putString(getString(R.string.StringNombreUsuarioGuardado), nombreUsuario);
                                                editor.putString(getString(R.string.StringNickUsuarioGuardado), nickUsuario);
                                                editor.putString(getString(R.string.StringURLFotoUsuarioGuardada), urlFotoUsuario);
                                                editor.putBoolean(PreferenciasFragment.getKeyMantenerSesionChk(),false);
                                                editor.commit();

                                                Toast.makeText(getApplicationContext(), "Bienvenido", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                                                intent.addCategory("login_no_sesion");
                                                intent.putExtra("idUsuario", idUsuario);
                                                intent.putExtra("urlFotoUsuario", urlFotoUsuario);
                                                intent.putExtra("nombreUsuario", nombreUsuario);
                                                intent.putExtra("nickUsuario", nickUsuario);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //Guardar los datos para no cerrar sesion e iniciar automaticamente
                                                SharedPreferences.Editor editor = preferences.edit();
                                                editor.putString(getString(R.string.StringIDUsuarioGuardado), idUsuario);
                                                editor.putString(getString(R.string.StringContraseñaGuardada), sPass);
                                                editor.putString(getString(R.string.StringNombreUsuarioGuardado), nombreUsuario);
                                                editor.putString(getString(R.string.StringNickUsuarioGuardado), nickUsuario);
                                                editor.putString(getString(R.string.StringURLFotoUsuarioGuardada), urlFotoUsuario);
                                                editor.putBoolean(PreferenciasFragment.getKeyMantenerSesionChk(),true);
                                                editor.commit();

                                                Toast.makeText(getApplicationContext(), "Bienvenido", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                                                intent.putExtra("idUsuario", idUsuario);
                                                intent.putExtra("urlFotoUsuario", urlFotoUsuario);
                                                intent.putExtra("nombreUsuario", nombreUsuario);
                                                intent.putExtra("nickUsuario", nickUsuario);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Usuario o contraseña icorrecto.", Toast.LENGTH_LONG).show();
                                ePass.setText("");
                            }
                            progressDialog.dismiss();
                            ingresar.setEnabled(true);


                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            progressDialog.dismiss();
                            ingresar.setEnabled(true);
                            Toast.makeText(getApplicationContext(), "Error de conexion." +
                                    "Error 001. Codigo de status " + statusCode, Toast.LENGTH_LONG).show();
                        }
                    });

                    //checkLogin log = new checkLogin();
                    //log.execute();
                } else {
                    Toast.makeText(getApplicationContext(), "Algún campo esta vacío.", Toast.LENGTH_LONG).show();
                }
            }
        });

            ePass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        ingresar.callOnClick();
                    }
                    return false;
                }
            });

    }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

}
