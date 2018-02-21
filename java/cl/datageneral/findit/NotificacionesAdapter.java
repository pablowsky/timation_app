package cl.datageneral.findit;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pablo on 04-07-2017.
 */

public class NotificacionesAdapter extends ArrayAdapter<Notificacion> {

    LayoutInflater inflater;
    private int resId;
    private SparseBooleanArray mSelectedItemsIds;
    private List<Notificacion> itemList;

    public NotificacionesAdapter(Context context, int resource, List<Notificacion> items) {
        super(context, resource, items);
        this.resId = resource;
        this.inflater = LayoutInflater.from(context);
        this.itemList = items;
        mSelectedItemsIds = new SparseBooleanArray();
    }

    @Override
    public Notificacion getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Notificacion notificacion = itemList.get(position);
        View rowView= inflater.inflate(resId, parent, false);
        TextView titulo      = (TextView) rowView.findViewById(R.id.titulo);
        TextView fecha       = (TextView) rowView.findViewById(R.id.fecha);
        TextView mensaje     = (TextView) rowView.findViewById(R.id.mensaje);
        ImageView imageView   = (ImageView) rowView.findViewById(R.id.img_stat);
        titulo.setText(notificacion.getTitulo());
        fecha.setText(notificacion.getFecha());
        mensaje.setText(notificacion.getMensaje());
        if(notificacion.getLeida().equals("0")){
            titulo.setTypeface(Typeface.DEFAULT_BOLD);
        }
        Log.d("Adapter",notificacion.toString());
        switch(notificacion.getTipo()){
            case "W":
                imageView.setImageResource(R.drawable.ic_warning_24dp);
                break;
            case "E":
                imageView.setImageResource(R.drawable.ic_error_24dp);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_info_24dp);
        }
        return rowView;
    }

}