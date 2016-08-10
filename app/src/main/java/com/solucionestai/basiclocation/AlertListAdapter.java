package com.solucionestai.basiclocation;

/**
 * Created by TAI on 22/07/2016.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class AlertListAdapter extends ArrayAdapter<Alertas> {

    private int				resource;
    private LayoutInflater inflater;
    private Context context;
    private  String idUsuario;

    public AlertListAdapter ( Context ctx, int resourceId, List<Alertas> objects,String idUsuario) {

        super( ctx, resourceId, objects );
        resource = resourceId;
        inflater = LayoutInflater.from( ctx );
        context=ctx;
        this.idUsuario=idUsuario;
    }




    @Override
    public View getView (int position, View convertView, ViewGroup parent ) {

		/* create a new view of my layout and inflate it in the row */
        convertView = (RelativeLayout) inflater.inflate( resource, null );

		/* Extract the city's object to show */
        final Alertas alert = getItem( position );

		/* Take the TextView from layout and set the city's name */
        TextView txtName = (TextView) convertView.findViewById(R.id.alertName);
        txtName.setText(alert.getusUsuario());

        /* Take the TextView from layout and set the city's name */
        TextView txtInfo = (TextView) convertView.findViewById(R.id.alertInfo);
        txtInfo.setText(alert.getComentariosAlerta());

		/* Take the TextView from layout and set the city's name */
        TextView txtDate = (TextView) convertView.findViewById(R.id.alertDate);
        txtDate.setText(alert.getDateTimeServerAlerta());

		/* Take the ImageView from layout and set the city's image */
        ImageView imageCity = (ImageView) convertView.findViewById(R.id.ImageAlert);
        String uri = "drawable/" + "ic_launcher";
        int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
        Drawable image = context.getResources().getDrawable(imageResource);
        imageCity.setImageDrawable(image);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(context)
                        .setMessage("Deseas ver este sitio en...")
                        .setCancelable(false)
                        .setPositiveButton("Mapa", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent=new Intent(context,MapsActivity.class);
                                intent.addCategory("hAlert");
                                intent.putExtra("lat",alert.getLatitudAlerta());
                                intent.putExtra("long", alert.getLongitudAlerta());
                                intent.putExtra("us",alert.getusUsuario());
                                intent.putExtra("comen",alert.getComentariosAlerta());
                                intent.putExtra("date",alert.getDateTimeServerAlerta());
                                intent.putExtra("idUsuario",idUsuario);
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton("Street View", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent=new Intent(context,StreetView.class);
                                intent.addCategory("hAlert");
                                intent.putExtra("lat",alert.getLatitudAlerta());
                                intent.putExtra("long", alert.getLongitudAlerta());
                                intent.putExtra("idUsuario",idUsuario);
                                context.startActivity(intent);
                            }
                        })
                        .show();


            }
        });

        return convertView;

    }
}

