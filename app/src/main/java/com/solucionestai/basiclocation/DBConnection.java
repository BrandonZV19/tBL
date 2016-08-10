package com.solucionestai.basiclocation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by BrandonZamudio on 6/15/2016.
 */
public class DBConnection extends SQLiteOpenHelper {
    public DBConnection(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Alertas(id INTEGER PRIMARY KEY, latitud REAL, longitud REAL, " +
                "fechaHora DATETIME, fechaHoraS DATETIME, idUsuario INTEGER, status INTEGER," +
                "arriba INTEGER, comentarios TEXT, usUsuario TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS Sitios(id INTEGER PRIMARY KEY, titulo TEXT," +
                "nombre TEXT, apellidoP TEXT, apellidoM TEXT, calle TEXT, numero TEXT, telefono INTEGER, " +
                "costo INTEGER, observaciones TEXT, idUsuario INTEGER, latitud REAL, longitud REAL," +
                " fechaHora DATETIME, arriba INTEGER, correo TEXT, colonia TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(DBConnection.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS Alertas");
        onCreate(db);
    }


}