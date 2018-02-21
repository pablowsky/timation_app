package cl.datageneral.findit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cl.datageneral.db.DBHelper;
import cl.datageneral.db.DatabaseManager;
import cl.datageneral.db.Query;

public class ActNotificaciones extends AppCompatActivity {
    List<Notificacion> items;
    NotificacionesAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Activity notificaciones","creada");
        setContentView(R.layout.act_notificaciones);
        DatabaseManager.initializeInstance(new DBHelper(getApplicationContext()));
        listView = (ListView) findViewById(R.id.list_notificaciones);
        loadRegistros();
    }

    private void loadRegistros() {
        HashMap<Integer,HashMap<String, String>> data2 = Query.getNotificaciones();
        items = new ArrayList<>();
        HashMap<String, String> columns;
        if (data2 == null) {
            return;
        }
        RelativeLayout nodata = (RelativeLayout) findViewById(R.id.empty);
        Map<Integer,HashMap<String, String>> data = new TreeMap<>(Collections.reverseOrder());
        data.putAll(data2);
        for(Integer key : data.keySet()){
            columns = data.get(key);
            Notificacion notif = new Notificacion();
            notif.setId(columns.get("_id"));
            notif.setLeida(columns.get("leida"));
            notif.setFecha(columns.get("fecha"));
            notif.setTitulo(columns.get("titulo"));
            notif.setMensaje(columns.get("mensaje"));
            notif.setTipo(columns.get("tipo"));

            items.add(notif);
        }
        adapter = new NotificacionesAdapter(this, R.layout.row_notificacion, items);
        listView.setAdapter(adapter);
        listView.setEmptyView(nodata);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Notificacion notif = items.get(position);
                Log.d("--",notif.getId());
                showNotificacion(notif);
            }
        });
    }

    private void showNotificacion(Notificacion notif) {
        Query.updNotificacion(notif.getId(),"1");
        loadRegistros();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(notif.getTitulo())
                .setMessage(notif.getMensaje())
                .setCancelable(true)
                .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
