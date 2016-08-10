package com.solucionestai.basiclocation;

/**
 * Created by BrandonZamudio on 6/27/2016.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class PreferenciasFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String KEY_MANTENER_SESION_CHK="mantener_sesion";

    private static final String KEY_ICONO_BARRA_CHK="icono_barra";
    private static final String KEY_SONIDO_CHK="sonido";
    private static final String KEY_ELEGIR_SONIDO_LST="elegir_sonido";

    private static final String KEY_SINCRONIZACION_CHK="sincronizacion";
    private static final String KEY_FREC_SINCRONIZACION_LST="frec_sincronizacion";

    private static final String KEY_BORRAR_BD="borrar_bd";

    private static final String KEY_TOAST="toast";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences shaPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        getActivity().setTitle(shaPref.getString(getString(R.string.StringNickUsuarioGuardado),"Preferencias"));

        /**
         * CARGAR LAYOUT
         */

        addPreferencesFromResource(R.xml.preferencias);

        Preference button=(Preference)findPreference(KEY_BORRAR_BD);
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DBConnection dbc;
                SQLiteDatabase db;
                dbc = new DBConnection(getActivity().getApplicationContext(), "RegistrosLoc", null, 1);
                db = dbc.getWritableDatabase();
                db.execSQL("DROP TABLE IF EXISTS Alertas");
                db.execSQL("DROP TABLE IF EXISTS Sitios");
                db.execSQL("CREATE TABLE IF NOT EXISTS Alertas(id INTEGER PRIMARY KEY, latitud REAL," +
                        "longitud REAL, fechaHora DATETIME, fechaHoraS DATETIME, idUsuario INTEGER," +
                        "status INTEGER,arriba INTEGER, comentarios TEXT, usUsuario TEXT)");
                Toast.makeText(getActivity().getApplicationContext(), "Registros eliminados",
                        Toast.LENGTH_LONG).show();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        /**
         * encender el escuchador de eventos por si alguna clave de preferencias cambia
         */
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // apagar el escuchador de eventos de preferencias
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    /**
     * OBTENER Y MODIFICAR LAS PREFRENCIAS DESDE FUERA DEL FRAGMENT
     */

    /**
     * String Preferences
     *
     * @param context
     * @param key
     * @return
     */
    public static String getString(Context context, final String key) {
        SharedPreferences shaPref = PreferenceManager.getDefaultSharedPreferences(context);
        return shaPref.getString(key, "");
    }

    public static void setString(Context context, final String key, final String value) {
        SharedPreferences shaPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = shaPref.edit();
        editor.putString(key, value);
        editor.commit();
    }



    public static SharedPreferences getPref(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Boolean Preferences
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean getBoolean(Context context, final String key, final boolean defaultValue) {

        SharedPreferences shaPref = PreferenceManager.getDefaultSharedPreferences(context);
        return shaPref.getBoolean(key, defaultValue);
    }

    public static void setBoolean(Context context, final String key, final boolean value) {

        SharedPreferences shaPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = shaPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }


    /**
     * MOSTRAR PREFERENCIAS
     */

    public static void showUserSettings(Context ctx) {

        SharedPreferences shaPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        /**
         * obtener pref
         */

        StringBuilder builder = new StringBuilder();

        builder.append("\n Mantener Sesión:"
                + shaPref.getBoolean(KEY_MANTENER_SESION_CHK, false));

        builder.append("\n Icono Barra:"
                + shaPref.getBoolean(KEY_ICONO_BARRA_CHK, false));

        builder.append("\n Sonido: "
                + shaPref.getBoolean(KEY_SONIDO_CHK, false));

        builder.append("\n Elegir Sonido: "
                + shaPref.getString(KEY_ELEGIR_SONIDO_LST,"NULL"));

        builder.append("\n Sincronización: "
                + shaPref.getBoolean(KEY_SINCRONIZACION_CHK,false));

        builder.append("\n Frecuencia de actualizacion: "
                + shaPref.getString(KEY_FREC_SINCRONIZACION_LST, "NULL"));


        Toast.makeText(ctx, builder.toString(), Toast.LENGTH_LONG).show();

    }


    /**
     * Get & set claves
     *
     * @return
     */

    public static String getKeyElegirSonidoLst() {
        return KEY_ELEGIR_SONIDO_LST;
    }

    public static String getKeyFrecSincronizacionLst() {
        return KEY_FREC_SINCRONIZACION_LST;
    }

    public static String getKeyIconoBarraChk() {
        return KEY_ICONO_BARRA_CHK;
    }

    public static String getKeyMantenerSesionChk() {
        return KEY_MANTENER_SESION_CHK;
    }

    public static String getKeySincronizacionChk() {
        return KEY_SINCRONIZACION_CHK;
    }

    public static String getKeySonidoChk() {
        return KEY_SONIDO_CHK;
    }
    public static String getKeyToast() {
        return KEY_TOAST;
    }







    /**
     * Cuando se cambia algo desde el menu de preferencias
     *
     * @param shaPref
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences shaPref, String key) {

        shaPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor=shaPref.edit();

        switch (key) {

            case KEY_MANTENER_SESION_CHK:
                if (!shaPref.getBoolean(KEY_MANTENER_SESION_CHK,false)){
                    editor.putBoolean(PreferenciasFragment.getKeyMantenerSesionChk(),false);
                    editor.remove(getString(R.string.StringIDUsuarioGuardado));
                    editor.remove(getString(R.string.StringContraseñaGuardada));
                    editor.remove(getString(R.string.StringNombreUsuarioGuardado));
                    editor.remove(getString(R.string.StringNickUsuarioGuardado));
                    editor.remove(getString(R.string.StringURLFotoUsuarioGuardada));
                    editor.commit();

                    Toast.makeText(getActivity().getApplicationContext(),
                            "Los datos de sesión no se guardarán",
                            Toast.LENGTH_SHORT).show();
                }else {
                    editor.putBoolean(PreferenciasFragment.getKeyMantenerSesionChk(),true);
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Tus datos de usuario se guardaron correctamente",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case KEY_ICONO_BARRA_CHK:
                if (shaPref.getBoolean(KEY_ICONO_BARRA_CHK,false)){
                    editor.putBoolean(PreferenciasFragment.getKeyIconoBarraChk(),true);
                    editor.commit();
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Se mostrarán alertas en la barra de notificaciones.",
                            Toast.LENGTH_SHORT).show();
                }else {
                    editor.putBoolean(PreferenciasFragment.getKeyIconoBarraChk(),false);
                    editor.commit();
                    Toast.makeText(getActivity().getApplicationContext(),
                            "No se mostrarán alertas en la barra de notificaciones.",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case KEY_SONIDO_CHK:
                if(!shaPref.getBoolean(KEY_SONIDO_CHK, false)){
                    editor.putBoolean(PreferenciasFragment.getKeySonidoChk(),false);
                    editor.commit();

                    Toast.makeText(getActivity().getApplicationContext(),
                            "Sonido de alertas desactivado.",
                            Toast.LENGTH_SHORT).show();
                }else {
                    editor.putBoolean(PreferenciasFragment.getKeySonidoChk(),true);
                    editor.commit();

                    Toast.makeText(getActivity().getApplicationContext(),
                            "Sonido de alertas activado",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case KEY_ELEGIR_SONIDO_LST:
                String valor_elegir_sonido_lst = shaPref.getString(KEY_ELEGIR_SONIDO_LST, "NULL");
                Toast.makeText(getActivity().getApplicationContext(),
                        "El sonido de notificación se ha cambiado correctamente. " + valor_elegir_sonido_lst,
                        Toast.LENGTH_SHORT).show();
                break;

            case KEY_TOAST:
                if(!shaPref.getBoolean(KEY_TOAST, false)){
                    editor.putBoolean(PreferenciasFragment.getKeyToast(),false);
                    editor.commit();

                    Toast.makeText(getActivity().getApplicationContext(),
                            "Toasts desactivados",
                            Toast.LENGTH_SHORT).show();
                }else {
                    editor.putBoolean(PreferenciasFragment.getKeyToast(),true);
                    editor.commit();

                    Toast.makeText(getActivity().getApplicationContext(),
                            "Toasts activados",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case KEY_SINCRONIZACION_CHK:
                if (shaPref.getBoolean(KEY_SINCRONIZACION_CHK, false)){
                    editor.putBoolean(PreferenciasFragment.getKeySincronizacionChk(),true);
                    editor.commit();

                    Toast.makeText(getActivity().getApplicationContext(),
                            "Las ubicaciones se sincronizaran en la nube.",
                            Toast.LENGTH_SHORT).show();
                }else {
                    editor.putBoolean(PreferenciasFragment.getKeySincronizacionChk(),false);
                    editor.commit();

                    Toast.makeText(getActivity().getApplicationContext(),
                            "Las ubicaciones se almacenarán localmente.",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case KEY_FREC_SINCRONIZACION_LST:
                String valor_frec_sincronizacion_lst = shaPref.getString(KEY_FREC_SINCRONIZACION_LST, "NULL");
                switch (valor_frec_sincronizacion_lst){
                    case "0":
                        editor.putLong(getString(R.string.StringFrecMetros),1);
                        editor.putInt(getString(R.string.StringFrecSegundos),10);
                        editor.commit();
                        Toast.makeText(getActivity().getApplicationContext(),
                                "La frecuencia se ha cambiado correctamente. ", Toast.LENGTH_SHORT).show();
                        break;
                    case "1":
                        editor.putLong(getString(R.string.StringFrecMetros),2);
                        editor.putInt(getString(R.string.StringFrecSegundos),20);
                        editor.commit();
                        Toast.makeText(getActivity().getApplicationContext(),
                                "La frecuencia se ha cambiado correctamente. ", Toast.LENGTH_SHORT).show();
                        break;
                    case "2":
                        editor.putLong(getString(R.string.StringFrecMetros),5);
                        editor.putInt(getString(R.string.StringFrecSegundos),40);
                        editor.commit();
                        Toast.makeText(getActivity().getApplicationContext(),
                                "La frecuencia se ha cambiado correctamente. ", Toast.LENGTH_SHORT).show();
                        break;
                    case "3":
                        editor.putLong(getString(R.string.StringFrecMetros),10);
                        editor.putInt(getString(R.string.StringFrecSegundos),60);
                        editor.commit();
                        Toast.makeText(getActivity().getApplicationContext(),
                                "La frecuencia se ha cambiado correctamente. ", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        editor.putLong(getString(R.string.StringFrecMetros),1);
                        editor.putInt(getString(R.string.StringFrecSegundos),15);
                        editor.commit();
                        Toast.makeText(getActivity().getApplicationContext(),
                                "La frecuencia se ha cambiado correctamente. ", Toast.LENGTH_SHORT).show();
                        break;
                }

        }
    }


}
