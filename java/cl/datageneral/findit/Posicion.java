package cl.datageneral.findit;

/**
 * Created by Pablo on 12/10/2017.
 */

public class Posicion {
    private String id;
    private String dispositivo;
    private String server_time;
    private String latotude;
    private String logitude;
    private String battery;
    private String distance;
    private String speed;

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public Posicion(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public String getServer_time() {
        return server_time;
    }

    public void setServer_time(String server_time) {
        this.server_time = server_time;
    }

    public String getLatitude() {
        return latotude;
    }

    public void setLatitude(String latotude) {
        this.latotude = latotude;
    }

    public String getLongitude() {
        return logitude;
    }

    public void setLongitude(String logitude) {
        this.logitude = logitude;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
