package cl.datageneral.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Pablo Molina C. on 11-01-2017.
 */

public class Json {
    // object
    //public static JSONObject getObjectJS(JSONObject mainObject, String key){
    public static JSONObject object(JSONObject mainObject, String key){
        JSONObject value = null;
        try {
            if(mainObject.has(key))
                value = toObject(mainObject.get(key).toString());
            //else
            //     return "";
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }
    // text
    //public static String getValueJS(JSONObject mainObject, String key){
    public static String text(JSONObject mainObject, String key){
        String value = "";
        try {
            if(mainObject.has(key))
                value = mainObject.get(key).toString();
            //else
            //     return "";
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }
    public static Integer number(JSONObject mainObject, String key){
        Integer value = null;
        try {
            if(mainObject.has(key))
                value = mainObject.getInt(key);
            //else
            //     return "";
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }
    // toObject
    //public static JSONObject getMainJS(String responseStr){
    public static JSONObject toObject(String responseStr){
        JSONObject mainObject = null;
        try {
            mainObject = new JSONObject(responseStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mainObject;
    }
    // array
    //public static JSONArray getArrayJS(JSONObject successIds, String key){
    public static JSONArray array(JSONObject successIds, String key){
        JSONArray r1 = null;
        try {
            if(successIds.has(key)){
                r1 = successIds.getJSONArray(key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return r1;
    }
}
