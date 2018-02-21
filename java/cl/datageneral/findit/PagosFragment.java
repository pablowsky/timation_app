package cl.datageneral.findit;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cl.datageneral.db.Query;

public class PagosFragment extends Fragment {
    List<Pagos> items;
    PagosAdapter adapter;
    private ListView listView;
    ArrayList<HashMap<String, String>> filas = null;
    AlertDialog dialog;
    View rootView;

    public PagosFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_pagos, container, false);
        listView = (ListView) rootView.findViewById(R.id.listado_pagos);
        loadRecords();
        return rootView;
    }

    private void loadRecords() {

        HashMap<Integer,HashMap<String, String>> data2 = Query.getPagos();
        Log.d("loadRcords","-"+data2);
        items = new ArrayList<>();
        HashMap<String, String> columns;
        if (data2 == null) {
            return;
        }
        RelativeLayout nodata = (RelativeLayout) rootView.findViewById(R.id.empty);
        Map<Integer,HashMap<String, String>> data = new TreeMap<>(Collections.reverseOrder());
        data.putAll(data2);
        for(Integer key : data.keySet()){
            columns = data.get(key);
            //Log.d("cols","-"+columns);
            Pagos pago = new Pagos();
            pago.setId(columns.get("_id"));
            pago.setDispositivo(columns.get("id_device"));
            pago.setNombreDispositivo(columns.get("nombre"));
            pago.setEstado(columns.get("estado_pago"));
            pago.setVencimiento(columns.get("fecha_vencimiento"));
            pago.setUltimo_pago(columns.get("fecha_ultimo_pago"));
            pago.setComentario(columns.get("comentario"));

            items.add(pago);
        }
        adapter = new PagosAdapter(getContext(), R.layout.row_pago, items);
        listView.setAdapter(adapter);
        listView.setEmptyView(nodata);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Pagos nt = items.get(position);
                if(nt.getEstado().equals("P") || nt.getEstado().equals("V") ) {
                    selectAmount(nt.getDispositivo());
                }else{
                    Toast.makeText(getActivity(),"No es necesario realizar ningun pago.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void startMediosPago(String device, String monto){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        MediosPagoFragment f = new MediosPagoFragment();
        Bundle args = new Bundle();
        args.putString("device", device);
        args.putString("monto", monto);
        f.setArguments(args);


        ft.replace(R.id.framePrincipal, f, "NewFragmentTag");
        ft.addToBackStack(null);
        ft.commit();
    }

    private void selectAmount(final String device){

        filas = new ArrayList<>();
        HashMap<Integer,HashMap<String, String>> data2 = Query.selectAmount(device);
        Map<Integer,HashMap<String, String>> data = new TreeMap<>(Collections.reverseOrder());
        data.putAll(data2);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dg_devlist, null);
        builder.setView(dialogView);
        builder.setTitle("Seleccione un Monto");
        builder.setNegativeButton("Cerrar", null);
        final ListView listView = (ListView) dialogView.findViewById(R.id.listview);

        for(Integer key : data.keySet()){
            HashMap<String, String> montos = data.get(key);
            filas.add(montos);
        }
        SimpleAdapter sa = new SimpleAdapter(
                getActivity(),
                filas,
                R.layout.row_monto,
                new String[]{"nombre"},
                new int[]{R.id.monto});

        listView.setAdapter(sa);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                startMediosPago(device, filas.get(position).get("nombre"));
                dialog.hide();
            }

        });

        dialog = builder.create();
        dialog.show();
    }

}
