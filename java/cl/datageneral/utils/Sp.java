package cl.datageneral.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static cl.datageneral.findit.Constants.SHARED_PREFERENCES;

/**
 * Created by Pablo on 22-12-2016.
 */

public class Sp {
    public static void set(String var, String value, Context c){
        SharedPreferences sharedPref= c.getSharedPreferences(SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor= sharedPref.edit();
        editor.putString(var, value);
        editor.commit();
    }

    public static String get(String var, Context c){
        String element;
        SharedPreferences sharedPref= c.getSharedPreferences(SHARED_PREFERENCES, 0);
        element = sharedPref.getString(var, "");
        return element;
    }
}
