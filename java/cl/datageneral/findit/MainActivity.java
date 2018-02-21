package cl.datageneral.findit;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cl.datageneral.db.DBHelper;
import cl.datageneral.db.DatabaseManager;
import cl.datageneral.db.Query;
import cl.datageneral.services.TimationService;
import cl.datageneral.utils.Sp;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, Constants {
    private GoogleMap mapa;
    ConstraintLayout mainLayout;
    ArrayList<Device> deviceList = null;
    ArrayList<HashMap<String, String>> traceList = null;
    String currDevice;
    Double currLat;
    Double currLong;
    Integer currColor;
    boolean tracertStatus;
    AlertDialog dialog;
    Polyline traceLine;
    HashMap<String,Marker> hashMapMarker = new HashMap<>(100);
    private Menu acMenu;
    private String currTracejob;
    private Snackbar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout = (ConstraintLayout)findViewById(R.id.mainLayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DBHelper db_temp = new DBHelper(this);
        db_temp.getDataBase();
        db_temp.close();
        DatabaseManager.initializeInstance(new DBHelper(getApplicationContext()));


        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Log.d("---Token","-"+Sp.get(CURR_USER_ITOKEN, getApplicationContext()));

        if (savedInstanceState == null) {
            currDevice = null;
            currLat = null;
            currLong = null;
            currColor = null;
            tracertStatus = false;
            currTracejob = null;
        } else {
            currDevice = (String) savedInstanceState.getSerializable("currDevice");
            currLat = savedInstanceState.getDouble("currLat");
            currLong = savedInstanceState.getDouble("currLong");
            currColor = savedInstanceState.getInt("currColor");
            tracertStatus = savedInstanceState.getBoolean("tracertStatus");
            currTracejob = savedInstanceState.getString("currTracejob");
        }
        setMainDevice();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("currTracejob", currTracejob);
        savedInstanceState.putString("currDevice", currDevice);
        if(currLat!=null)
            savedInstanceState.putDouble("currLat", currLat);
        if(currLong!=null)
            savedInstanceState.putDouble("currLong", currLong);
        if(currColor!=null)
            savedInstanceState.putInt("currColor", currColor);
        savedInstanceState.putBoolean("tracertStatus", tracertStatus);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mapa = map;  //-33.458618, -70.675131
        LatLng sydney = new LatLng(-33.458, -70.675);
        /*map.addMarker(new MarkerOptions().position(sydney)
                .title("Marker in Sydney"));*/
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if(currDevice!=null){
            loadPositionFor(currDevice);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.acMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        if(tracertStatus){
            /*Drawable yourdrawable = menu.getItem(0).getIcon(); // change 0 with 1,2 ...
            yourdrawable.mutate();
            yourdrawable.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);*/
            traceIconColor(R.color.colorAccent);
            loadTracertFor(currDevice, currTracejob);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        int dColor;
        if (id == R.id.linear) {
            // TRACE ROUTE DEVICE
            if(traceLine!=null){
                disableTracert();
                traceIconColor(R.color.white);
            }else {
                if(currDevice==null){
                    Toast.makeText(this,"Selecione un dispositivo",Toast.LENGTH_LONG).show();
                    return true;
                }
                setTracert();
                /*
                String url = SERVER_NAME + TRACERT + "&device="+currDevice;
                Log.d("URL:",url);
                //new Tracert().execute(url);
                Intent intent = new Intent(getApplicationContext(), TimationService.class);
                intent.putExtra(S_PROCESS,TimationService.TRACE_ROUTE);
                intent.putExtra("currDevice",currDevice);
                startService(intent);*/
            }
            /*
            Drawable yourdrawable = item.getIcon(); // change 0 with 1,2 ...
            yourdrawable.mutate();
            yourdrawable.setColorFilter(getResources().getColor(dColor), PorterDuff.Mode.SRC_IN);*/


        }else if (id == R.id.vehiculos) {
            // SELECT DEVICE
            disableTracert();
            selectDevice();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch(id){
            case R.id.nav_history:
                if(currDevice==null){
                    showToast("Seleccione un vehiculo",Toast.LENGTH_SHORT);
                    return true;
                }
                dialogHistorial();
                break;

            case R.id.nav_adddevice:
                addDeviceDialog();
                break;

            case R.id.nav_share:
                if(currDevice==null){
                    showToast("Seleccione un vehiculo",Toast.LENGTH_SHORT);
                    return true;
                }
                String sLat = String.valueOf(currLat);//.substring(0,8);
                String sLong = String.valueOf(currLong);//.substring(0,8);
                String uri = MAP_URI;
                uri = uri.replace("{LAT}",sLat);
                uri = uri.replace("{LONG}",sLong);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, uri);
                sendIntent.setType("text/plain");
                //startActivity(sendIntent);
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
                break;

            case R.id.nav_notifications:
                Intent not_act = new Intent(this,ActNotificaciones.class);
                startActivity(not_act);
                break;

            case R.id.nav_pay:
                Intent pay_act = new Intent(this,ActPagos.class);
                startActivity(pay_act);
                break;
        }

        //
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setMainDevice(){
        // ESTABLECE UN DISPOSITIVO POR DEFECTO
        HashMap<String, String> data = Query.selectDevice();
        if(data.size()>=1){
            currDevice = data.get("dispositivo");
            currColor = Integer.parseInt(data.get("value"));
            getCurrPosition();
        }
    }

    private void getCurrPosition(){
        SendSnackbar("Buscando", Snackbar.LENGTH_INDEFINITE, true);
        Intent intent = new Intent(getApplicationContext(), TimationService.class);
        intent.putExtra(S_PROCESS,TimationService.GET_CURR_POSITION);
        intent.putExtra("currDevice",currDevice);
        startService(intent);
    }

    private void traceIconColor(int color){
        Drawable yourdrawable = acMenu.getItem(0).getIcon(); // change 0 with 1,2 ...
        yourdrawable.mutate();
        yourdrawable.setColorFilter(getResources().getColor(color), PorterDuff.Mode.SRC_IN);
    }
    private void disableTracert(){
        if(traceLine!=null){
            traceLine.remove();
            traceLine = null;
            tracertStatus = false;
        }
        traceIconColor(R.color.white);
    }

    private LatLng strToLatLng(String latitude, String longitude, Boolean global){
        LatLng r = null;
        try{
            if(global) {
                currLat = Double.parseDouble(latitude);
                currLong = Double.parseDouble(longitude);
                r = new LatLng(currLat, currLong);
            }else{
                Double Lat = Double.parseDouble(latitude);
                Double Long = Double.parseDouble(longitude);
                r = new LatLng(Lat, Long);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return r;
    }

    private void loadTracertFor(String device, String job){
        HashMap<Integer,HashMap<String, String>> dev = Query.getTracertDevice(device, job);
        int sz = dev.size();
        int color;
        if(sz<2){
            return;
        }
        if(currColor!=null) {
            float[] rgb = new float[3];
            rgb[0] = currColor;
            rgb[1] = 1;
            rgb[2] = 1;
            color = Color.HSVToColor(rgb);
        }else{
            color = Color.BLACK;
        }
        PolylineOptions pOptions = new PolylineOptions();
        LatLng start = strToLatLng(dev.get(0).get("latitude"), dev.get(0).get("longitude"), false);
        LatLng end = strToLatLng(dev.get(1).get("latitude"), dev.get(1).get("longitude"), false);


        pOptions.add(start,end);
        // TRACE POINTS
        for(int i=2;i<sz;i++){
            HashMap<String, String> pos = dev.get(i);
            String latitude = pos.get("latitude");
            String longitude = pos.get("longitude");

            start = end;
            end = strToLatLng(latitude, longitude, false);
            pOptions.add(start,end);
        }
        start = end;
        end = new LatLng(currLat, currLong);
        pOptions.add(start,end).color(color);
        traceLine = mapa.addPolyline(pOptions);
    }

    private void loadPositionByID(String idPosicion){
        if(idPosicion.equals("0")){
            return;
        }
        HashMap<Integer,HashMap<String, String>> dev = Query.getPosicionByID(idPosicion);


        String nombre   = dev.get(0).get("nombre");
        String cvalue   = dev.get(0).get("value");
        String latitude = dev.get(0).get("latitude");
        String longitude= dev.get(0).get("longitude");
        String snippet  = "Hora: "+dev.get(0).get("server_time");

        LatLng llg = strToLatLng(latitude, longitude, true);
        setMarkerOnMap(llg, currDevice, nombre, snippet, Integer.parseInt(cvalue));
    }
    private void loadPositionFor(String device){
        if(traceLine!=null){
            traceLine.remove();
            traceLine = null;
        }
        HashMap<Integer,HashMap<String, String>> dev = Query.getPosicionDevice(device);
        Log.d("--","-"+device);
        Log.d("--","-"+dev);
        String nombre = dev.get(0).get("nombre");
        String cvalue = dev.get(0).get("value");
        String latitude = dev.get(0).get("latitude");
        String longitude = dev.get(0).get("longitude");
        String snippet = "Hora: "+dev.get(0).get("server_time");


        LatLng llg = strToLatLng(latitude, longitude, true);
        setMarkerOnMap(llg, device, nombre, snippet, Integer.parseInt(cvalue));
    }



    public void setMarkerOnMap(LatLng last_pos, String device, String Title, String snippet, Integer color){
        float cZoom = mapa.getCameraPosition().zoom;
        if(hashMapMarker.containsKey(device)){
            Marker tmarker = hashMapMarker.get(device);
            tmarker.remove();
        }

        if(last_pos==null){
            return;
        }

        //LatLng last_pos = new LatLng(pLatitude, pLongitude);
        Marker cmarker = mapa.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .position(last_pos)
                .snippet(snippet)
                .title(Title));
        mapa.moveCamera(CameraUpdateFactory.newLatLng(last_pos));
        if(cZoom<DEFAULT_ZOOM) {
            mapa.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        }

        hashMapMarker.put(device,cmarker);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mMessageReceiver, new IntentFilter(FCM_INTENT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMessageReceiver);
    }

    private void SendSnackbar(String msg, int duration, boolean loading){
        bar = Snackbar.make(mainLayout, msg, duration);
        if(loading) {
            ViewGroup contentLay = (ViewGroup) bar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
            ProgressBar item = new ProgressBar(getApplicationContext());
            contentLay.addView(item, 0);
        }
        bar.show();
    }

    private void showToast(String msg, int duration){
        Toast.makeText(getApplicationContext(),msg, duration).show();
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    private void selectDevice(){
        HashMap<Integer,HashMap<String, String>> data = Query.selectDevices();
        deviceList = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dg_devlist, null);
        builder.setView(dialogView);
        builder.setTitle("Seleccione Vehiculo");
        builder.setNegativeButton("Cerrar", null);
        final ListView listView = (ListView) dialogView.findViewById(R.id.listview);
        for(Integer i: data.keySet()){
            HashMap<String, String> contact = data.get(i);
            HashMap<String, String> cols = new HashMap<>();

            cols.put("_id", contact.get("_id"));
            cols.put("dispositivo", contact.get("name"));
            cols.put("nombre", contact.get("nombre"));
            Device d = new Device(
                    contact.get("_id"),
                    contact.get("nombre"),
                    contact.get("dispositivo"),
                    Integer.parseInt(contact.get("value"))
            );

            deviceList.add(d);

        }

        DevicesAdapter sa = new DevicesAdapter(getApplicationContext(),R.layout.row_contactos,deviceList);

        listView.setAdapter(sa);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Device attr = deviceList.get(position);

                currDevice = attr.getDevice();
                currColor = attr.getColor();
                getCurrPosition();
                loadPositionFor(attr.getDevice());

                Toast.makeText(getApplicationContext(),attr.getName()+" seleccionado" ,
                        Toast.LENGTH_LONG).show();
                dialog.hide();
            }

        });

        dialog = builder.create();
        dialog.show();
    }

    private void setTracert(){
        traceList = new ArrayList<>();
       /* HashMap<Integer,HashMap<String, String>> data = Query.selectDevices();
        deviceList = new ArrayList<>();*/

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dg_devlist, null);
        builder.setView(dialogView);
        builder.setTitle("Ver Recorrido de");
        builder.setNegativeButton("Cerrar", null);
        final ListView listView = (ListView) dialogView.findViewById(R.id.listview);

        HashMap<String, String> temp = new HashMap<>();
        temp.put("id","1");
        temp.put("minutos","30");
        temp.put("texto","30 Minutos");
        traceList.add(temp);

        temp = new HashMap<>();
        temp.put("id","2");
        temp.put("minutos","60");
        temp.put("texto","1 Hora");
        traceList.add(temp);

        temp = new HashMap<>();
        temp.put("id","3");
        temp.put("minutos","180");
        temp.put("texto","3 Horas");
        traceList.add(temp);

        temp = new HashMap<>();
        temp.put("id","4");
        temp.put("minutos","300");
        temp.put("texto","5 Horas");
        traceList.add(temp);

        SimpleAdapter sa = new SimpleAdapter(this, traceList, R.layout.row_trace,
                new String[]{"texto"},
                new int[]{R.id.tiempo});

        listView.setAdapter(sa);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String minutos = traceList.get(position).get("minutos");

                Intent intent = new Intent(getApplicationContext(), TimationService.class);
                intent.putExtra(S_PROCESS,TimationService.TRACE_ROUTE);
                intent.putExtra("currDevice",currDevice);
                intent.putExtra("minutes",minutos);
                startService(intent);
                SendSnackbar("Buscando...", Snackbar.LENGTH_INDEFINITE, true);
                tracertStatus = true;
                traceIconColor(R.color.colorAccent);
                dialog.hide();
            }

        });

        dialog = builder.create();
        dialog.show();
    }

    private void addDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.dg_adddevice, null);
        final EditText Edevice = (EditText) dialoglayout.findViewById(R.id.id_device);
        final EditText Eperson = (EditText) dialoglayout.findViewById(R.id.id_person);
        final EditText Ename = (EditText) dialoglayout.findViewById(R.id.dev_name);
        builder.setView(dialoglayout);
        builder.setTitle("Agregar Dispositivo")
                .setCancelable(false)
                .setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String device = Edevice.getText().toString().trim();
                String person = Eperson.getText().toString().trim();
                String name = Ename.getText().toString().trim();
                person = person.replace(".","");
                person = person.replace("-","");
                person = person.replace(",","");
                if(device.length()==0 && person.length()==0 && name.length()==0){
                    showToast("Ingrese todos los datos",Toast.LENGTH_SHORT);
                    return;
                }
                String params = "&device=" + device +
                        "&pers=" + person +
                        "&token=" + Sp.get(CURR_USER_ITOKEN, getApplicationContext());
                if(Sp.get(CURR_USER_ITOKEN, getApplicationContext()).length()==0){
                    Toast.makeText(getApplicationContext(),
                            "Terminando Instalacion, Reintente es unos segundos.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                SendSnackbar("Agregando...", Snackbar.LENGTH_INDEFINITE, true);
                Query.insDevice(person, device, name);
                Intent intent = new Intent(getApplicationContext(), TimationService.class);
                intent.putExtra(S_PROCESS,TimationService.ADD_DEVICE);
                intent.putExtra("params",params);
                intent.putExtra("name",name);
                startService(intent);
                alert.dismiss();
            }
        });
    }

    private void dialogHistorial() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.dg_historial, null);
        final EditText dgFecha = (EditText) dialoglayout.findViewById(R.id.dgFecha);
        final EditText dgHora  =(EditText) dialoglayout.findViewById(R.id.dgHora);

        dgFecha.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Calendar now = Calendar.getInstance();
                    final Calendar c = Calendar.getInstance();
                    DatePickerDialog dpd = new DatePickerDialog(MainActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    String dd = Integer.toString(dayOfMonth);
                                    String mm = Integer.toString(monthOfYear+1);
                                    if(dayOfMonth<10){
                                        dd = "0"+dd;
                                    }
                                    if(monthOfYear<9){
                                        mm = "0"+mm;
                                    }
                                    dgFecha.setText(year + "-" + mm + "-" + dd);

                                }
                            }, c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DATE));
                    dpd.show();
                }
            }
        );
        dgHora.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   //Calendar now = Calendar.getInstance();
                   final Calendar c = Calendar.getInstance();
                   TimePickerDialog dpd = new TimePickerDialog(MainActivity.this,
                           new TimePickerDialog.OnTimeSetListener() {
                               @Override
                               public void onTimeSet(TimePicker view, int hh, int nn) {
                                   dgHora.setText(
                                           new StringBuilder()
                                                   .append(pad(hh)).append(":")
                                                   .append(pad(nn)));

                               }
                           }, c.get(Calendar.HOUR),c.get(Calendar.MINUTE),false);
                   dpd.show();
               }
           }
        );

        builder.setView(dialoglayout);
        builder.setTitle("Historial")
                .setCancelable(false)
                .setPositiveButton("Buscar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String f = dgFecha.getText().toString();
                        String h = dgHora.getText().toString();
                        String h1 = f+" "+h+":59";
                        String h2 = f+" "+h+":00";

                        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        try {
                            Date date = sdf.parse(h1);

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            cal.add(Calendar.MINUTE, -3);
                            Date nDate = cal.getTime();

                            DateFormat time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                            h2 = time.format(nDate);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        SendSnackbar("Buscando", Snackbar.LENGTH_INDEFINITE, true);
                        Intent intent = new Intent(getApplicationContext(), TimationService.class);
                        intent.putExtra(S_PROCESS,TimationService.SEARCH_HISTORY);
                        intent.putExtra("date_start",h1);
                        intent.putExtra("date_end",h2);
                        intent.putExtra("currDevice",currDevice);
                        startService(intent);
                    }
                })
                .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int tMessage = intent.getIntExtra("message", 0);
            if(tMessage==TimationService.ADD_DEVICE){
                String r_msg = intent.getStringExtra("r_msg");
                String cDevice = intent.getStringExtra("currDevice");
                SendSnackbar(r_msg,Snackbar.LENGTH_LONG, false);
                if(cDevice!=null){
                    currDevice = cDevice;
                    loadPositionFor(cDevice);
                }else{
                    SendSnackbar("No se pudo agregar", Snackbar.LENGTH_SHORT, false);
                }
                return;
            }else if(tMessage==TimationService.TRACE_ROUTE){
                String cDevice = intent.getStringExtra("currDevice");
                currTracejob = intent.getStringExtra("job");
                int status = intent.getIntExtra("status", 0);
                if(status==1){
                    loadTracertFor(cDevice, currTracejob);
                    SendSnackbar("Recorrido Encontrado", Snackbar.LENGTH_SHORT, false);
                }else{
                    SendSnackbar("No disponible", Snackbar.LENGTH_SHORT, false);
                    //showToast("No disponible", Toast.LENGTH_SHORT);
                    disableTracert();
                }
                return;
            }else if(tMessage==TimationService.SEARCH_HISTORY){
                Log.d("BROADCAST","SEARCH_HISTORY");
                String cDevice = intent.getStringExtra("currDevice");
                String idPosicion = intent.getStringExtra("idPosicion");
                if(currDevice==null){
                    return;
                }
                if(idPosicion==null){
                    SendSnackbar("Fecha No Disponible", Snackbar.LENGTH_SHORT, false);
                    return;
                }

                if(cDevice.equals(currDevice)){
                    SendSnackbar("Ubicacion encontrada", Snackbar.LENGTH_SHORT, false);
                    disableTracert();
                    loadPositionByID(idPosicion);
                }else if(bar!=null){
                    bar.dismiss();
                }
                return;
            }else if(tMessage==TimationService.GET_CURR_POSITION){
                Log.d("BROADCAST","GET_CURR_POSITION");
                String idPosicion = intent.getStringExtra("idPosition");

                Log.d("BROADCAST","-"+idPosicion);
                if(idPosicion==null){
                    SendSnackbar("No Disponible", Snackbar.LENGTH_SHORT, false);
                    return;
                }
                loadPositionByID(idPosicion);
                if(bar!=null){
                    bar.dismiss();
                }
                return;
            }

            String latitude = intent.getStringExtra("latitude");
            String longitude = intent.getStringExtra("longitude");
            String device = intent.getStringExtra("device");
            String server_time = intent.getStringExtra("server_time");

            if(!device.equals(currDevice)){
                return;
            }


            HashMap<Integer,HashMap<String, String>> dev = Query.selectDevice(device);
            String nombre = dev.get(0).get("nombre");
            String cvalue = dev.get(0).get("value");
            String snippet  = "Hora: "+server_time;
            LatLng llg = strToLatLng(latitude, longitude, true);
            setMarkerOnMap(llg, device, nombre, snippet, Integer.parseInt(cvalue));

            // TRACERT
            if(tracertStatus && traceLine!=null) {
                Log.d("Trace Updated",latitude+" "+longitude);
                List<LatLng> points = traceLine.getPoints();
                points.add(llg);
                traceLine.setPoints(points);
            }
        }
    };
/*
    private void showMakeDBDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.dg_makedb, null);
        builder.setView(dialoglayout);
        builder.setTitle("Extraer BD")
                .setCancelable(false)
                .setPositiveButton("Extraer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        final EditText clave = (EditText) dialoglayout.findViewById(R.id.clave);
                        final EditText nombre = (EditText) dialoglayout.findViewById(R.id.nombre);

                        getBD(clave.getText().toString(),nombre.getText().toString());
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void getBD(String key, String dbname){
        Log.d("clave:", key);
        Log.d("Nombre:", dbname);
        if(key.equals(MAKEDB_PASS)){
            try {
                File sd = Environment.getExternalStorageDirectory();
                //File data = Environment.getDataDirectory();
                if (sd.canWrite()) {
                    String currentDBPath = DB_PATH + DB_NAME;
                    String backupDBPath = "db_"+dbname+".db";
                    File currentDB = new File(currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    if (currentDB.exists()) {
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    }
                    Toast.makeText(getApplicationContext(), "BD copiada correctamente", Toast.LENGTH_SHORT).show();
                }else
                    Log.e("Error","No se puede escribir en SDCard");

            } catch (Exception e) {

            }
        }else
            Toast.makeText(getApplicationContext(), "Clave Incorrecta", Toast.LENGTH_SHORT).show();

    }*/

}
