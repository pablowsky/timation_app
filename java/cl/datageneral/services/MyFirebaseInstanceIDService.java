package cl.datageneral.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;

import cl.datageneral.db.Query;
import cl.datageneral.findit.Constants;
import cl.datageneral.utils.Sp;

//Class extending FirebaseInstanceIdService
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);

    }

    private void sendRegistrationToServer(String token) {
        Sp.set(Constants.CURR_USER_ITOKEN,token, getApplicationContext());
/*
        HashMap<Integer,HashMap<String, String>> devs = Query.selectDevices();
        for (Integer i: devs.keySet()){
            HashMap<String, String> t = devs.get(i);
            Log.d("IDT",t.get("idt"));
        }*/

        //You can implement this method to store the token on your server
        //Not required for current project
    }
}
