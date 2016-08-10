package com.solucionestai.basiclocation;

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

import java.util.List;

/**
 * Created by TAI on 29/07/2016.
 */
public class SitioListAdapter extends ArrayAdapter<Sitios> {

    private int				resource;
    private LayoutInflater inflater;
    private Context context;
    private String idUsuario;

    public SitioListAdapter ( Context ctx, int resourceId, List<Sitios> objects, String idUsuario) {

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
        final Sitios site = getItem( position );

		/* Take the TextView from layout and set the city's name */
        TextView txtName = (TextView) convertView.findViewById(R.id.sitioTitulo);
        txtName.setText(site.getTituloLu());

        /* Take the TextView from layout and set the city's name */
        TextView txtInfo = (TextView) convertView.findViewById(R.id.sitioInfo);
        txtInfo.setText(site.getTelefonoLu());

		/* Take the TextView from layout and set the city's name */
        TextView txtDate = (TextView) convertView.findViewById(R.id.sitioDate);
        txtDate.setText(site.getFechaHoraLu());

		/* Take the ImageView from layout and set the city's image */
        ImageView imageCity = (ImageView) convertView.findViewById(R.id.ImageSitio);
        String uri = "mipmap/" + "strv";
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
                                intent.addCategory("hSitio");
                                intent.putExtra("lat",site.getLatitudLu());
                                intent.putExtra("long", site.getLongitudLu());
                                intent.putExtra("titulo",site.getTituloLu());
                                intent.putExtra("tel",site.getTelefonoLu());
                                intent.putExtra("date",site.getFechaHoraLu());
                                intent.putExtra("idUsuario",idUsuario);
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton("Street View", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent=new Intent(context,StreetView.class);
                                intent.addCategory("hSitio");
                                intent.putExtra("lat",site.getLatitudLu());
                                intent.putExtra("long", site.getLongitudLu());
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
