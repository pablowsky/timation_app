package cl.datageneral.findit;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static java.security.AccessController.getContext;

/**
 * Created by Pablo on 30/09/2017.
 */

public class DevicesAdapter extends ArrayAdapter<Device> {
    private Context mContext;
    private List<Device> Items;
    private int Resource;

    public DevicesAdapter(Context context, int resource, List<Device> items) {
        super(context, resource, items);
        //super(context, resource, items);
        Resource = resource;
        mContext = context;
        Items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        Device p = Items.get(position);
        //getting the view
        View view = layoutInflater.inflate(Resource, null, false);

        //getting the view elements of the list from the view
        ImageView imageView = (ImageView) view.findViewById(R.id.imarker);
        TextView textViewName = (TextView) view.findViewById(R.id.nombre);
        TextView textViewTeam2 = (TextView)view.findViewById(R.id.dispositivo);

        textViewName.setText(p.getName());
        textViewTeam2.setText(p.getDevice());
        imageView.setImageResource(R.drawable.ic_room_white_36dp);
        //imageView.setColorFilter(getContext().getResources().getColor(R.color.colorAccent));

        float[] rgb = new float[3];
        rgb[0] = p.getColor();
        rgb[1] = 1;
        rgb[2] = 1;
        //Log.d("RGB COLOR","-"+Color.HSVToColor(rgb));

        imageView.setColorFilter(Color.HSVToColor(rgb));
        return view;
    }
}
