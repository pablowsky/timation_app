package cl.datageneral.findit;

/**
 * Created by Pablo on 09/09/2017.
 */

public interface Constants {
    String IDENTIFIER   = "cl.datageneral.findit";

    String DB_PATH = "/data/data/"+IDENTIFIER+"/databases/";
    String DB_NAME = "database_1.db"; // 0.1


    String CURR_USER_ITOKEN= "CURR_USER_ITOKEN";
    String HASH_USER_ITOKEN= "HASH_USER_ITOKEN";

    String MAKEDB_PASS  = "9876";
    String SHARED_PREFERENCES = "prefInspNuevo";
    float  DEFAULT_ZOOM = 14.0f;
    String MIN_DISTANCE = "0";
    String TAG_XML      = "registro";
    String INPUT_PATH   = "/TempZip";
    int C_RESULT_OK=-1;
    int C_RESULT_CANCELED=0;
    String USER_PARAM = "login_usr";
    String PASS_PARAM = "pass";

    String FCM_INTENT = "fcm_intent";
    String S_PROCESS = "S_PROCESS";
    String MAP_URI = "https://www.google.com/maps/search/?api=1&query={LAT},{LONG}";

    /*
     * URLs
     */
    String SERVER_NAME  = "http://192.168.1.36/findit_api/";
    String URL_INDEX    = "index.php?";
    String S_ADD_DEVICE   = URL_INDEX+"service_adddevice";
    String TRACERT   = URL_INDEX+"service_tracert";
    String HISTORY   = URL_INDEX+"service_history";
    String GET_PAYS  = URL_INDEX+"service_accountstat";
    String ACCDATA   = URL_INDEX+"service_accdata";
    String CURR_POS  = URL_INDEX+"service_lastposition";

    /**
     * SERVER: JSON RESPONSES
     */
    int R_SUCCESS = 1;
    int R_ERROR = 0;

    String JRESP_SUCCESS= "success";
    String JRESP_STATUS= "status";
    String JRESP_SESSION= "session";

}
