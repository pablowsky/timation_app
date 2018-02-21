package cl.datageneral.findit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;

import cl.datageneral.db.Query;
import cl.datageneral.services.TimationService;

public class ActPagos extends AppCompatActivity implements Constants {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pagos);

        getdevicesInfo();



    }

    private void getdevicesInfo(){
        showLoading(View.VISIBLE);
        Intent intent = new Intent(getApplicationContext(), TimationService.class);
        intent.putExtra(S_PROCESS,TimationService.GET_PAY_INFO);
        //intent.putExtra("currDevice",currDevice);
        startService(intent);
    }

    private void showLoading(int view){
        RelativeLayout pb = (RelativeLayout) findViewById(R.id.pagos_loading);
        pb.setVisibility(view);
    }

    private void loadList(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        PagosFragment f = new PagosFragment();
        /*Bundle args = new Bundle();

        tFragment = param1;
        setMainTitle(_titles[param1]);
        args.putInt(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);

        f.setArguments(args);*/
        transaction.replace(R.id.framePrincipal,f);
        transaction.commit();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int tMessage = intent.getIntExtra("message", 0);
            if(tMessage==TimationService.GET_PAY_INFO){
                showLoading(View.GONE);
                loadList();
                String r_msg = intent.getStringExtra("r_msg");
                String cDevice = intent.getStringExtra("currDevice");
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mMessageReceiver, new IntentFilter(FCM_INTENT));
    }

    //Must unregister onPause()
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
    }
}
