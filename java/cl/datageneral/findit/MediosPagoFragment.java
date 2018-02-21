package cl.datageneral.findit;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cl.datageneral.services.TimationService;


/**
 * A simple {@link Fragment} subclass.
 */
public class MediosPagoFragment extends Fragment implements Constants{
    String device;
    String monto;
    View rootView;
    boolean show_datos = false;
    LinearLayout ln1;
    RelativeLayout rl1;

    public MediosPagoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            device = getArguments().getString("device");
            monto = getArguments().getString("monto");
        }
        Log.d("medios",device);
        Log.d("medios",monto);


        /*
         * OBTENER DATOS DE CUENTA
         */
        show_datos = false;
        Intent intent = new Intent(getActivity(), TimationService.class);
        intent.putExtra(S_PROCESS,TimationService.GET_BANK_ACCOUNT);
        getActivity().startService(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mediospago, container, false);

        ln1 = (LinearLayout)rootView.findViewById(R.id.datos_transferencia);
        rl1 = (RelativeLayout) rootView.findViewById(R.id.datos_transferencia_loading);
        setTextViewText(R.id.monto, monto);
        if (show_datos){
            ln1.setVisibility(View.VISIBLE);
            rl1.setVisibility(View.GONE);
        }else{
            ln1.setVisibility(View.GONE);
            rl1.setVisibility(View.VISIBLE);
        }
        return rootView;
    }



    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int tMessage = intent.getIntExtra("message", 0);
            if(tMessage==TimationService.GET_BANK_ACCOUNT){
                String nombre = intent.getStringExtra("nombre");
                String rut = intent.getStringExtra("rut");
                String banco = intent.getStringExtra("banco");
                String tipo_cuenta = intent.getStringExtra("tipo_cuenta");
                String cuenta = intent.getStringExtra("cuenta");
                String email = intent.getStringExtra("email");
                String asunto = intent.getStringExtra("asunto");
                asunto = asunto.replace("{device}",device);

                setTextViewText(R.id.nombre,nombre);
                setTextViewText(R.id.rut,rut);
                setTextViewText(R.id.banco,banco);
                setTextViewText(R.id.tipo_cuenta,tipo_cuenta);
                setTextViewText(R.id.cuenta,cuenta);
                setTextViewText(R.id.email,email);
                setTextViewText(R.id.asunto,asunto);

                ln1.setVisibility(View.VISIBLE);
                rl1.setVisibility(View.GONE);
            }
        }
    };

    public void setTextViewText(int id, String text){
        TextView t = (TextView)rootView.findViewById(id);
        t.setText(text);
    }


    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mMessageReceiver, new IntentFilter(FCM_INTENT));
    }

    //Must unregister onPause()
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mMessageReceiver);
    }

}
