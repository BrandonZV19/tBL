package com.solucionestai.basiclocation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SitiosOption extends AppCompatActivity {
    ListView listView;
    boolean finalizar;
    String idUsuario;
    SharedPreferences preferences;
    DBConnection dbc;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sitios_option);

        preferences = PreferenciasFragment.getPref(SitiosOption.this);

        if (getIntent().hasExtra("idUsuario")) {
            idUsuario = getIntent().getStringExtra("idUsuario");
            finalizar=true;
        } else {
            idUsuario = preferences.getString(getString(R.string.StringIDUsuarioGuardado), "ERROR");
        }

        listView= (ListView)findViewById(R.id.listVSitiosOp);

        String[] values = new String[] {
                "Agregar nuevo sitio",
                "Ver sitios",
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        listView.setAdapter(adapter);

        dbc = new DBConnection(SitiosOption.this, "RegistrosLoc", null, 1);
        db = dbc.getWritableDatabase();

        db.execSQL("CREATE TABLE IF NOT EXISTS Sitios(id INTEGER PRIMARY KEY, titulo TEXT," +
                "nombre TEXT, apellidoP TEXT, apellidoM TEXT, calle TEXT, numero TEXT, telefono INTEGER, " +
                "costo INTEGER, observaciones TEXT, idUsuario INTEGER, latitud REAL, longitud REAL," +
                " fechaHora DATETIME, arriba INTEGER, correo TEXT, colonia TEXT)");

        if (idUsuario != "0" || idUsuario == null || idUsuario == "" || idUsuario.contains("ERROR")) {
            //Notify and out
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Intent intent1= new Intent(SitiosOption.this, SitioEdit.class);

                        intent1.putExtra("idUsuario",idUsuario);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(intent1);
                        break;

                    case 1:
                        Intent intent2= new Intent(SitiosOption.this, HistorialSitios.class);

                        intent2.putExtra("idUsuario",idUsuario);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(intent2);
                        break;


                    default:
                        Toast.makeText(SitiosOption.this,"Por favor elige una opciòn",Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });

    }



}
