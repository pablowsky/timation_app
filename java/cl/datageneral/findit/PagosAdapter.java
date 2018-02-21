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
 * Created by Pablo on 15/10/2017.
 */

public class PagosAdapter extends ArrayAdapter<Pagos> {
    LayoutInflater inflater;
    private int resId;
    private SparseBooleanArray mSelectedItemsIds;
    private List<Pagos> itemList;

    public PagosAdapter(Context context, int resource, List<Pagos> items) {
        super(context, resource, items);
        this.resId = resource;
        this.inflater = LayoutInflater.from(context);
        this.itemList = items;
    }

    @Override
    public Pagos getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Pagos npago = itemList.get(position);
        View rowView= inflater.inflate(resId, parent, false);
        TextView nombre       = (TextView) rowView.findViewById(R.id.nombre_dispositivo);
        TextView iddispositivo= (TextView) rowView.findViewById(R.id.id_dispositivo);
        TextView comentario   = (TextView) rowView.findViewById(R.id.comentario);
        ImageView imageView   = (ImageView) rowView.findViewById(R.id.img_stat);
        nombre.setText(npago.getNombreDispositivo());
        iddispositivo.setText(npago.getDispositivo());
        comentario.setText(npago.getComentario());

        if(npago.getEstado().equals("V")){
            nombre.setTypeface(Typeface.DEFAULT_BOLD);
        }
        Log.d("Adapter",npago.toString());
        switch(npago.getEstado()){
            case "P":
                imageView.setImageResource(R.drawable.ic_warning_24dp);
                break;
            case "V":
                imageView.setImageResource(R.drawable.ic_error_24dp);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_info_24dp);
        }
        return rowView;
    }

}
