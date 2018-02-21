package cl.datageneral.services;

import android.webkit.CookieManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import cl.datageneral.findit.Constants;

public class HttpConnection {
    static final String COOKIES_HEADER = "Set-Cookie";
    static final String COOKIE = "Cookie";
    static CookieManager msCookieManager = CookieManager.getInstance();
    private static int responseCode;
    static int TimeOut = 20000;

    public static String post(String requestURL, String urlParameters)  throws Exception {
        URL url = new URL(requestURL);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //conn.setDoInput(true);
        conn.setDoOutput(true);

        conn.setRequestMethod("POST");
        conn.setConnectTimeout(TimeOut);
        conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty( "charset", "utf-8");

        conn = getCookies(conn);

        Writer wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
        wr.write(urlParameters);
        wr.flush();
        wr.close();



        setCookies(conn);

        return getResponse(conn);
    }
    // HTTP GET request
    public static String get(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("GET");
        con.setConnectTimeout(TimeOut);
        con.setRequestProperty("User-Agent", "Mozilla");
        con = getCookies(con);
        setCookies(con);

        return getResponse(con);
    }

    private static String getResponse(HttpURLConnection con){
        String response = "";
        try {
            setResponseCode(con.getResponseCode());
            if (getResponseCode() == HttpsURLConnection.HTTP_OK) {

                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
                br.close();
                con.disconnect();
            } else {
                response = "";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }



    private static HttpURLConnection getCookies(HttpURLConnection con){

        if(msCookieManager.hasCookies()){
            con.setRequestProperty(COOKIE , msCookieManager.getCookie(Constants.SERVER_NAME) );
        }
        return con;
    }
    private static void setCookies(HttpURLConnection con){
        Map<String, List<String>> headerFields = con.getHeaderFields();
        List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                //msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                msCookieManager.setCookie(Constants.SERVER_NAME,HttpCookie.parse(cookie).get(0).toString());

            }
        }
    }

    private static void setResponseCode(int responseCode) {
        HttpConnection.responseCode = responseCode;
        //Log.i("Status Code", ": " + responseCode);
    }


    private static int getResponseCode() {
        return responseCode;
    }
}

