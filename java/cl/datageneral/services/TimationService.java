package cl.datageneral.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.icu.util.TimeUnit;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cl.datageneral.db.Query;
import cl.datageneral.findit.Constants;
import cl.datageneral.findit.Pagos;
import cl.datageneral.findit.Posicion;
import cl.datageneral.utils.Json;

public class TimationService extends IntentService implements Constants{
    final static public int ADD_DEVICE = 1;
    final static public int TRACE_ROUTE = 2;
    final static public int SEARCH_HISTORY = 3;
    final static public int GET_PAY_INFO = 4;
    final static public int GET_BANK_ACCOUNT = 5;
    final static public int GET_CURR_POSITION = 6;
    private String STRING_SERVER_RESPONSE;
    private JSONObject JSON_SERVER_RESPONSE;
    private String JSON_MSG = "";


    public TimationService() {
        super("HelloIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        final int process = intent.getExtras().getInt(S_PROCESS);
        String device;

        switch(process){
            case GET_CURR_POSITION:
                device = intent.getExtras().getString("currDevice");
                currPosition(device);
                break;
            case ADD_DEVICE:
                String params = intent.getExtras().getString("params");
                //String name = intent.getExtras().getString("name");
                addDevice(params);
                break;
            case TRACE_ROUTE:
                device = intent.getExtras().getString("currDevice");
                String minutes = intent.getExtras().getString("minutes");
                traceRoute(device, minutes);
                break;
            case SEARCH_HISTORY:
                String date_start   = intent.getExtras().getString("date_start");
                String date_end     = intent.getExtras().getString("date_end");
                device      = intent.getExtras().getString("currDevice");
                History(device, date_start, date_end);
                break;
            case GET_PAY_INFO:
                Pays();
                break;
            case GET_BANK_ACCOUNT:
                Intent acc = new Intent(Constants.FCM_INTENT);
                acc.putExtra("message", GET_BANK_ACCOUNT);

                getJSON(SERVER_NAME + ACCDATA );
                if(isSuccess() ){
                    acc.putExtra("nombre", Json.text(JSON_SERVER_RESPONSE,"nombre"));
                    acc.putExtra("rut", Json.text(JSON_SERVER_RESPONSE,"rut"));
                    acc.putExtra("banco", Json.text(JSON_SERVER_RESPONSE,"banco"));
                    acc.putExtra("tipo_cuenta", Json.text(JSON_SERVER_RESPONSE,"tipo_cuenta"));
                    acc.putExtra("cuenta", Json.text(JSON_SERVER_RESPONSE,"cuenta"));
                    acc.putExtra("email", Json.text(JSON_SERVER_RESPONSE,"email"));
                    acc.putExtra("asunto", Json.text(JSON_SERVER_RESPONSE,"asunto"));
                }
                sendBroadcast(acc);
                break;
        }


    }

    private void currPosition(String cdevice) {
        int status = 0;
        String url = SERVER_NAME + CURR_POS + "&device=" + cdevice;
        long idPos = 0;

        getJSON(url);
        if( isSuccess() && isInJSON("position") ){
            status = 1;
            try {
                String npos = Json.text(JSON_SERVER_RESPONSE,"position");
                Posicion pos = jsonToPosicion(Json.toObject(npos));
                idPos = insertPosition(pos);
                Log.d("idPos",idPos+"-");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Log.e("Error","No se pudo realizar accion");
        }
        Intent intent = new Intent(Constants.FCM_INTENT);
        intent.putExtra("message", GET_CURR_POSITION);
        intent.putExtra("idPosition", Long.toString(idPos));
        intent.putExtra("status", status);
        sendBroadcast(intent);
    }

    private void Pays(){
        String url_devs = "";
        String param = "&devices=";
        JSONArray pagos;
        try {
            Thread.sleep(100);
            HashMap<Integer,HashMap<String, String>> devices = Query.selectDevices();
            for(Integer key:devices.keySet()){
                HashMap<String, String> dev = devices.get(key);
                url_devs += dev.get("dispositivo")+",";
            }
            url_devs = url_devs.replaceAll(",$", "");
            postJSON(SERVER_NAME + GET_PAYS, param+url_devs);
            if(isSuccess() && isInJSON("_data") ){
                pagos = Json.array(JSON_SERVER_RESPONSE,"_data");
                Query.truncate("pagos");
                if( pagos.length() > 0 ){
                    for(int i =0; i<pagos.length(); i++){
                        try {
                            Pagos npago = jsonToPago( Json.toObject(pagos.get(i).toString()) );
                            insertPago(npago);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            Intent pay = new Intent(Constants.FCM_INTENT);
            pay.putExtra("message", GET_PAY_INFO);
            sendBroadcast(pay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void History(String device, String date_start, String date_end){
        long idPos = 0;
        String url = SERVER_NAME + HISTORY;
        String params = "&device=" + device +
                "&start=" + date_start +
                "&end=" + date_end;

        postJSON(url,params);
        //Log.d("HISTORY POS",STRING_SERVER_RESPONSE);
        if(isSuccess()){
            /* INSERTAR ULTIMA POSICION */
            if( isInJSON("position")){
                try {
                    String npos = Json.text(JSON_SERVER_RESPONSE,"position");
                    Posicion pos = jsonToPosicion(Json.toObject(npos));
                    idPos = insertPosition(pos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Intent intent = new Intent(Constants.FCM_INTENT);
        intent.putExtra("message", SEARCH_HISTORY);
        intent.putExtra("idPosicion", Long.toString(idPos));
        intent.putExtra("currDevice", device);
        sendBroadcast(intent);
    }

    private void traceRoute(String cdevice, String minutes) {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        Log.d("TRACE_JOB","-"+ts);

        int status = 0;
        String url = SERVER_NAME + TRACERT + "&device="+cdevice+ "&minutes="+minutes;
        JSONArray tracert;

        getJSON(url);
        if(isSuccess() && isInJSON("tracerts") ){
            status = 1;
            tracert = Json.array(JSON_SERVER_RESPONSE,"tracerts");

            if( tracert.length() > 0 ){
                for(int i =0; i<tracert.length(); i++){
                    try {
                        Posicion pos = jsonToPosicion( Json.toObject(tracert.get(i).toString()) );
                        insertPositionJob(pos, ts);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
            Log.e("Error","No se pudo realizar accion");
        }
        /**/
        Long tsEnd = System.currentTimeMillis()/1000;
        Long rest = tsLong - tsEnd;
        Log.d("TIEMPO DE EJECUCUCION:",rest.toString());
        /**/
        Intent intent = new Intent(Constants.FCM_INTENT);
        intent.putExtra("message", TRACE_ROUTE);
        intent.putExtra("status", status);
        intent.putExtra("currDevice", cdevice);
        intent.putExtra("job", ts);
        sendBroadcast(intent);
    }

    private void addDevice(String params){
        String url = SERVER_NAME + S_ADD_DEVICE;
        String idVirtual = null;
        String token;
        postJSON(url,params);
        if(isSuccess()){
            /* INSERTAR DISPOSITIVO */
            if( isInJSON("id_virtual") && isInJSON("token") ) {
                idVirtual = Json.text(JSON_SERVER_RESPONSE,"id_virtual");
                token     = Json.text(JSON_SERVER_RESPONSE,"token");
                Query.updDevice(idVirtual, token);
                JSON_MSG = "Agregado Correctamente";
            }

            /* INSERTAR ULTIMA POSICION */
            if( isInJSON("position")){
                Posicion pos = jsonToPosicion(Json.toObject(Json.text(JSON_SERVER_RESPONSE,"position")));

                insertPosition(pos);
            }
        }else{
            Log.e("ADDEVICE","No se pudo realizar accion");
        }

        Intent intent = new Intent(Constants.FCM_INTENT);
        intent.putExtra("message", ADD_DEVICE);
        intent.putExtra("r_msg", JSON_MSG);
        intent.putExtra("currDevice", idVirtual);
        sendBroadcast(intent);
    }

    private Pagos jsonToPago(JSONObject jPosition){
        Pagos r = new Pagos();
        try {
            r.setDispositivo(jPosition.get("DEVICE").toString());
            r.setEstado(jPosition.get("ESTADO").toString());
            r.setVencimiento(jPosition.get("VENCIMIENTO").toString());
            r.setUltimo_pago(jPosition.get("ULTIMO_PAGO").toString());
            r.setComentario(jPosition.get("COMENTARIO").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return r;
    }
    private Posicion jsonToPosicion(JSONObject jPosition){
        Posicion r = new Posicion();
        try {
            r.setId(jPosition.get("ID_REGISTRO").toString());
            r.setDispositivo(jPosition.get("ID_DISPOSITIVO").toString());
            r.setServer_time(jPosition.get("SERVER_TIME").toString());
            r.setLatitude(jPosition.get("LATITUDE").toString());
            r.setLongitude(jPosition.get("LONGITUDE").toString());
            r.setBattery(jPosition.get("BATTERY_LEVEL").toString());
            r.setDistance(jPosition.get("DISTANCE").toString());
            r.setSpeed(jPosition.get("SPEED").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return r;
    }

    private void insertPago(Pagos jPago){
        //long insertedId = 0;
        ContentValues nValues = new ContentValues();
        //nValues.put("_id", jPosition.getId());
        nValues.put("id_device", jPago.getDispositivo());
        nValues.put("estado_pago", jPago.getEstado());
        nValues.put("fecha_vencimiento", jPago.getVencimiento());
        nValues.put("fecha_ultimo_pago", jPago.getUltimo_pago());
        nValues.put("comentario", jPago.getComentario());

        try {
            long insertedId = Query.insertOrThrow("pagos",nValues);
            //Log.d("Insertado","-"+insertedId);
        } catch (SQLException e) {
            e.printStackTrace();
        }/* finally {
            insertedId = Long.parseLong(jPosition.getId());
        }
        return insertedId;*/
    }

    private long insertPosition(Posicion jPosition){
        long insertedId;
        ContentValues nValues = new ContentValues();
        nValues.put("_id", jPosition.getId());
        nValues.put("dispositivo", jPosition.getDispositivo());
        nValues.put("server_time", jPosition.getServer_time());
        nValues.put("latitude", jPosition.getLatitude());
        nValues.put("longitude", jPosition.getLongitude());
        nValues.put("battery", jPosition.getBattery());
        nValues.put("distance", jPosition.getDistance());
        nValues.put("speed", jPosition.getSpeed());

        try {
            Query.insertOrThrow("posiciones",nValues);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        insertedId = Long.parseLong(jPosition.getId());

        return insertedId;
    }
    private void insertPositionJob(Posicion jPosition, String job){
        /*long insertedId = 0;
        try {
            Query.insFixPosition(jPosition, job);
        } catch (SQLException e) {
            e.printStackTrace();
        }
/**/
        ContentValues nValues = new ContentValues();
        nValues.put("_id", jPosition.getId());
        nValues.put("dispositivo", jPosition.getDispositivo());
        nValues.put("server_time", jPosition.getServer_time());
        nValues.put("latitude", jPosition.getLatitude());
        nValues.put("longitude", jPosition.getLongitude());
        nValues.put("battery", jPosition.getBattery());
        nValues.put("distance", jPosition.getDistance());
        nValues.put("speed", jPosition.getSpeed());
        nValues.put("job", job);

        try {
            Query.insertOrThrow("posiciones",nValues);
        } catch (SQLException e) {
            Query.updPosicion(jPosition.getId(), job);
            //e.printStackTrace();
        }/**/
    }

    private boolean isInJSON(String key){
        boolean r = false;
        boolean b = JSON_SERVER_RESPONSE.has(key);
        try {
            if(b && JSON_SERVER_RESPONSE.get(key)!=null){
                r = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return r;
    }

    protected boolean isSuccess(){
        JSON_MSG = "Ha ocurrido un error, reintente mas tarde.";
        if(JSON_SERVER_RESPONSE==null){
            return false;
        }
        if(!isInJSON("msg")) {
            JSON_MSG = Json.text(JSON_SERVER_RESPONSE,"msg");
        }
        if(!isInJSON("success")) {
            return false;
        }
        try {
            if (JSON_SERVER_RESPONSE.getInt("success")==R_SUCCESS) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected void getJSON(String url) {
        JSON_SERVER_RESPONSE = null;
        STRING_SERVER_RESPONSE = null;
        try {
            STRING_SERVER_RESPONSE = HttpConnection.get(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(STRING_SERVER_RESPONSE==null){
            return;
        }
        Log.d("RESPONSE","-"+STRING_SERVER_RESPONSE);
        try {
            JSON_SERVER_RESPONSE = new JSONObject(STRING_SERVER_RESPONSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    protected void postJSON(String... params) {
        JSON_SERVER_RESPONSE = null;
        STRING_SERVER_RESPONSE = null;
        try {
            STRING_SERVER_RESPONSE = HttpConnection.post(params[0], params[1]);
        } catch (Exception e) {
            JSON_MSG = "Problemas de Conexión";
            e.printStackTrace();
        }
        if(STRING_SERVER_RESPONSE==null){
            return;
        }
        //Log.d("RESPONSE","-"+STRING_SERVER_RESPONSE);
        try {
            JSON_SERVER_RESPONSE = new JSONObject(STRING_SERVER_RESPONSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
