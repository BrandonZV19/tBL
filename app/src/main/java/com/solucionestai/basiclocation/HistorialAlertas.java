package com.solucionestai.basiclocation;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class HistorialAlertas extends AppCompatActivity {
    ListView listView;
    Spinner spinner;
    DBConnection dbc;
    SQLiteDatabase db;
    ArrayList<Alertas> listaAlertas;
    String idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_alertas);

        setTitle("Historial de alertas");

        if (getIntent().hasExtra("idUsuario")) {
            idUsuario = getIntent().getStringExtra("idUsuario");
        } else {
            idUsuario = "ERROR";
        }

        listaAlertas = new ArrayList<>();

        dbc = new DBConnection(HistorialAlertas.this, "RegistrosLoc", null, 1);
        db = dbc.getWritableDatabase();

        db.execSQL("CREATE TABLE IF NOT EXISTS Alertas(id INTEGER PRIMARY KEY, latitud REAL," +
                "longitud REAL, fechaHora DATETIME, fechaHoraS DATETIME, idUsuario INTEGER," +
                "status INTEGER,arriba INTEGER, comentarios TEXT, usUsuario TEXT)");

        if (idUsuario != null && !idUsuario.contains("ERROR")) {
            db.execSQL("CREATE TABLE IF NOT EXISTS Usuario" + idUsuario + "(idRegistro INTEGER PRIMARY KEY, latitud REAL, longitud REAL," +
                    "fechaHora DATETIME, enviado INTEGER)");

            listaAlertas.clear();

            Cursor cc = db.rawQuery("SELECT id, latitud, longitud, fechaHora," +
                    "fechaHoraS, idUsuario, status, comentarios, usUsuario from Alertas ORDER BY fechaHoraS DESC", null);
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

        spinner=(Spinner)findViewById(R.id.spinnerHA);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opcionesHA, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0: //fecha

                        listView = ( ListView ) findViewById( R.id.alert_list);
                        listView.setAdapter( new AlertListAdapter(HistorialAlertas.this, R.layout.alert_row_item, listaAlertas, idUsuario ) );

                        break;

                    case 1: //usuario
                        Toast.makeText(HistorialAlertas.this,"Por el momento esta opciòn no esta disponible",
                                Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(HistorialAlertas.this,"No has seleccionado nada",Toast.LENGTH_SHORT).show();
            }
        });

    }

}
