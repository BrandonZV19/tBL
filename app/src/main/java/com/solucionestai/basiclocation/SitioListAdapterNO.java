package com.solucionestai.basiclocation;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by TAI on 02/08/2016.
 */
public class SitioListAdapterNO extends ArrayAdapter<Sitios> {
    private int				resource;
    private LayoutInflater inflater;
    private Context context;
    private String idUsuario;
    DBConnection dbc;
    SQLiteDatabase db;
    ProgressDialog progressDialog;
    String getUrlRegistrarSitio = "http://191.101.156.66/erp/ws_gps/ws_registro_lugar";

    int id;
    String titulo;
    String nombre;
    String apellidoP;
    String apellidoM;
    String correo;
    String calle;
    int num;
    String tel;
    int costo;
    String observaciones;
    Double lu_latitud,lu_longitud;
    String dateTime;

    public SitioListAdapterNO ( Context ctx, int resourceId, List<Sitios> objects, String idUsuario) {

        super( ctx, resourceId, objects );
        resource = resourceId;
        inflater = LayoutInflater.from( ctx );
        context=ctx;
        this.idUsuario=idUsuario;

        dbc = new DBConnection(context, "RegistrosLoc", null, 1);
        db = dbc.getWritableDatabase();

        progressDialog = new ProgressDialog(context);
        progressDialog.setMax(100);
        progressDialog.setTitle("Espere . . .");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

    }




    @Override
    public View getView (int position, View convertView, ViewGroup parent ) {

		/* create a new view of my layout and inflate it in the row */
        convertView = (RelativeLayout) inflater.inflate( resource, null );

		/* Extract the city's object to show */
        final Sitios site = getItem( position );

		/* Take the TextView from layout and set the city's name */
        TextView txtName = (TextView) convertView.findViewById(R.id.sitioTituloNO);
        txtName.setText(site.getTituloLu());

        /* Take the TextView from layout and set the city's name */
        TextView txtInfo = (TextView) convertView.findViewById(R.id.sitioInfoNO);
        txtInfo.setText(site.getTelefonoLu());

		/* Take the TextView from layout and set the city's name */
        TextView txtDate = (TextView) convertView.findViewById(R.id.sitioDateNO);
        txtDate.setText(site.getFechaHoraLu());

		/* Take the ImageView from layout and set the city's image */
        ImageView imageCity = (ImageView) convertView.findViewById(R.id.ImageSitioNO);
        String uri = "mipmap/" + "cloud0";
        int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
        Drawable image = context.getResources().getDrawable(imageResource);
        imageCity.setImageDrawable(image);

        id=site.getIdLugar();
        titulo=site.getTituloLu();
        nombre=site.getNombreLu();
        apellidoP=site.getApellidoPatLu();
        apellidoM=site.getApellidoMatLu();
        correo=site.getCorreoLu();
        calle=site.getCalleLu();
        num=site.getNumeroLu();
        tel=site.getTelefonoLu();
        costo=site.getCostoLu();
        observaciones=site.getObservacionesLu();
        lu_latitud=Double.valueOf(site.getLatitudLu());
        lu_longitud=Double.valueOf(site.getLongitudLu());
        dateTime=site.getFechaHoraLu();

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                //Subir sitio-------
                //get parrams from sitios
                //put and post this params
                //Si se hace correctamente, borrar dicho registro y agregar uno nuevo con el ID y datos retornados

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
                    params.put("lu_correo", correo);

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

                                db.execSQL("DELETE FROM Sitios WHERE ID = "+id);

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

                                Toast.makeText(context, "Sitio exitoso", Toast.LENGTH_SHORT).show();
                                Log.e("subirSitio:OK", idRegistroS+"");
                            } else {
                                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(context, "SITIO:FAILED", Toast.LENGTH_SHORT).show();
                            Log.e("subirSitio:FAILED", responseBody.toString()+"");
                            //Agregar codigo de error
                            progressDialog.dismiss();
                        }
                    });

                    //POST here
                    //If post right arriba=1

                }else {
                    new AlertDialog.Builder(context)
                            .setMessage("Algo ha salido mal, vuelve a loguarte o intentalo mas tarde." +
                                    "Error 002.")
                            .setTitle("¡Huy!")
                            .setCancelable(false)
                            .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(context, MainActivity.class);
                                    context.startActivity(intent);
                                    dialog.dismiss();
                                    progressDialog.dismiss();
                                }
                            })
                            .show();
                }

            }
        });

        return convertView;

    }
}
