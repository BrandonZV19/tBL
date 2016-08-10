package com.solucionestai.basiclocation;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class HistorialSitios extends AppCompatActivity {
    String idUsuario;
    DBConnection dbc;
    SQLiteDatabase db;
    ArrayList<Sitios> listaSitios;
    ListView listView;
    Spinner spinner;
    TextView nothing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_sitios);

        setTitle("Historial de sitios");

        if (getIntent().hasExtra("idUsuario")) {
            idUsuario = getIntent().getStringExtra("idUsuario");
        } else {
            idUsuario = "ERROR";
        }

        listaSitios=new ArrayList<>();

        dbc = new DBConnection(HistorialSitios.this, "RegistrosLoc", null, 1);
        db = dbc.getWritableDatabase();

        db.execSQL("CREATE TABLE IF NOT EXISTS Sitios(id INTEGER PRIMARY KEY, titulo TEXT," +
                "nombre TEXT, apellidoP TEXT, apellidoM TEXT, calle TEXT, numero TEXT, telefono INTEGER, " +
                "costo INTEGER, observaciones TEXT, idUsuario INTEGER, latitud REAL, longitud REAL," +
                " fechaHora DATETIME, arriba INTEGER, correo TEXT, colonia TEXT)");

        if (idUsuario != null && !idUsuario.contains("ERROR")) {

        }else {
            //Agregar codigo de error
            Toast.makeText(getApplicationContext(), "Por favor inicia sesión.", Toast.LENGTH_LONG)
                    .show();
            finish();
        }

        nothing=(TextView)findViewById(R.id.nadaHS);

        spinner=(Spinner)findViewById(R.id.spinnerHS);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.opcionesHS, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0: //fecha

                        listaSitios.clear();

                        Cursor cc = db.rawQuery("SELECT id, titulo, nombre, apellidoP, apellidoM, calle, numero," +
                                "telefono, costo, observaciones, latitud, longitud, fechaHora, arriba, correo" +
                                " from Sitios WHERE arriba = 1 ORDER BY fechaHora DESC", null);
                        if (cc.moveToFirst()) { //Si hay registros, nos posicionamos en el primero (el mas viejo)
                            do {
                                Sitios sitio=new Sitios();
                                //falta id
                                sitio.setIdLugar(cc.getInt(0));//samefix
                                sitio.setTituloLu(cc.getString(1));
                                sitio.setNombreLu(cc.getString(2));
                                sitio.setApellidoPatLu(cc.getString(3));
                                sitio.setApellidoMatLu(cc.getString(4));
                                sitio.setCalleLu(cc.getString(5));
                                sitio.setNumeroLu(cc.getInt(6));
                                sitio.setTelefonoLu(cc.getString(7));
                                sitio.setCostoLu(cc.getInt(8));
                                sitio.setObservacionesLu(cc.getString(9));
                                sitio.setIdUsuarioLu(idUsuario);
                                sitio.setLatitudLu(cc.getString(10));
                                sitio.setLongitudLu(cc.getString(11));
                                sitio.setFechaHoraLu(cc.getString(12));//samefix
                                sitio.setArriba(cc.getInt(13));
                                sitio.setCorreoLu(cc.getString(14));
                                listaSitios.add(sitio);
                                Log.e("onCreate", "idSitio: " + cc.getInt(0));
                            } while (cc.moveToNext());
                        }
                        cc.close();

                        if (listaSitios.size()>0) {
                            listView = (ListView) findViewById(R.id.sitio_list);
                            listView.setAdapter(new SitioListAdapter(HistorialSitios.this, R.layout.sitio_row_item, listaSitios, idUsuario));
                            nothing.setVisibility(View.GONE);
                        }else {
                            nothing.setVisibility(View.VISIBLE);
                        }

                        break;

                    case 1: //no enviados
                        listaSitios.clear();

                        Cursor ccc = db.rawQuery("SELECT id, titulo, nombre, apellidoP, apellidoM, calle, numero," +
                                "telefono, costo, observaciones, latitud, longitud, fechaHora, arriba, correo" +
                                " from Sitios WHERE arriba = 3 OR arriba = 2 ORDER BY fechaHora DESC", null);
                        if (ccc.moveToFirst()) { //Si hay registros, nos posicionamos en el primero (el mas viejo)
                            do {
                                Sitios sitio=new Sitios();
                                //falta id
                                sitio.setIdLugar(ccc.getInt(0));//samefix
                                sitio.setTituloLu(ccc.getString(1));
                                sitio.setNombreLu(ccc.getString(2));
                                sitio.setApellidoPatLu(ccc.getString(3));
                                sitio.setApellidoMatLu(ccc.getString(4));
                                sitio.setCalleLu(ccc.getString(5));
                                sitio.setNumeroLu(ccc.getInt(6));
                                sitio.setTelefonoLu(ccc.getString(7));
                                sitio.setCostoLu(ccc.getInt(8));
                                sitio.setObservacionesLu(ccc.getString(9));
                                sitio.setIdUsuarioLu(idUsuario);
                                sitio.setLatitudLu(ccc.getString(10));
                                sitio.setLongitudLu(ccc.getString(11));
                                sitio.setFechaHoraLu(ccc.getString(12));//samefix
                                sitio.setArriba(ccc.getInt(13));
                                sitio.setCorreoLu(ccc.getString(14));
                                listaSitios.add(sitio);
                                Log.e("onCreate", "idSitio: " + ccc.getInt(0));
                            } while (ccc.moveToNext());
                        }
                        ccc.close();

                        if (listaSitios.size()>0) {
                            listView = (ListView) findViewById(R.id.sitio_list);
                            listView.setAdapter(new SitioListAdapterNO(HistorialSitios.this, R.layout.sitio_row_item_no, listaSitios,idUsuario));
                            nothing.setVisibility(View.GONE);
                        }else {
                            nothing.setVisibility(View.VISIBLE);
                        }
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(HistorialSitios.this,"No has seleccionado nada",Toast.LENGTH_SHORT).show();
            }
        });

    }

}
