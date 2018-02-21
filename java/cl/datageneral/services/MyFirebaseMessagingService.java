package cl.datageneral.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.SyncStateContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import cl.datageneral.db.DBHelper;
import cl.datageneral.db.DatabaseManager;
import cl.datageneral.db.Query;
import cl.datageneral.findit.ActNotificaciones;
import cl.datageneral.findit.Constants;
import cl.datageneral.findit.MainActivity;
import cl.datageneral.findit.Notificacion;
import cl.datageneral.findit.R;

import static android.R.id.message;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String msg = null;
        //Log.d(TAG, "From: " + remoteMessage.getFrom());
        //Log.d(TAG, "Notification Message Body2: " + remoteMessage.getData().toString());
        if(remoteMessage.getData().containsKey("msg")){
            msg = remoteMessage.getData().get("msg");
        }else{
            msg = "position"; // REVISAR DESPUES
        }
        //Log.d(TAG,"Message:"+msg);
        if(msg.equals("notification")){
            Notificacion n = new Notificacion();
            n.setId(remoteMessage.getData().get("ID"));
            n.setLeida("0");
            n.setTipo(remoteMessage.getData().get("TIPO"));
            n.setFecha(remoteMessage.getData().get("FECHA"));
            n.setMensaje(remoteMessage.getData().get("MENSAJE"));
            n.setTitulo(remoteMessage.getData().get("TITULO"));
            saveNotification(n);
            sendNotification(n.getTitulo(),n.getMensaje());
        }else if(msg.equals("position")){
            Intent intent = new Intent(Constants.FCM_INTENT);
            intent.putExtra("latitude", remoteMessage.getData().get("latitude"));
            intent.putExtra("longitude", remoteMessage.getData().get("longitude"));
            intent.putExtra("device", remoteMessage.getData().get("device"));
            intent.putExtra("server_time", remoteMessage.getData().get("server_time"));
            sendBroadcast(intent);
            savePosition(
                    remoteMessage.getData().get("id"),
                    remoteMessage.getData().get("device"),
                    remoteMessage.getData().get("server_time"),
                    remoteMessage.getData().get("latitude"),
                    remoteMessage.getData().get("longitude"),
                    remoteMessage.getData().get("battery"),
                    remoteMessage.getData().get("distance"),
                    remoteMessage.getData().get("speed")
            );
        }
    }

    private void saveNotification(Notificacion notif){
        DatabaseManager.initializeInstance(new DBHelper(getApplicationContext()));
        ContentValues nValues = new ContentValues();
        nValues.put("_id", notif.getId());
        nValues.put("leida", notif.getLeida());
        nValues.put("tipo", notif.getTipo());
        nValues.put("fecha", notif.getFecha());
        nValues.put("titulo", notif.getTitulo());
        nValues.put("mensaje", notif.getMensaje());

        try {
            long r = Query.insertOrThrow("notificaciones",nValues);
            //Log.d("Insertado","-"+r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePosition(
            String id,
            String dispositivo,
            String server_time,
            String latitude,
            String longitude,
            String battery,
            String distance,
            String speed){

        DatabaseManager.initializeInstance(new DBHelper(getApplicationContext()));
        ContentValues nValues = new ContentValues();
        nValues.put("_id", id);
        nValues.put("dispositivo", dispositivo);
        nValues.put("server_time", server_time);
        nValues.put("latitude", latitude);
        nValues.put("longitude", longitude);
        nValues.put("battery", battery);
        nValues.put("distance", distance);
        nValues.put("speed", speed);

        try {
            long r = Query.insertOrThrow("posiciones",nValues);
            //Log.d("Insertado","-"+r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, ActNotificaciones.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.timation_logo)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
