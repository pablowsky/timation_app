package cl.datageneral.findit;

/**
 * Created by Pablo on 30/09/2017.
 */

public class Device {
    private String ID;
    private String NAME;
    private String DEVICE;
    private Integer COLOR;

    public Device(String id, String name, String device, Integer color){
        ID = id;
        NAME = name;
        DEVICE = device;
        COLOR = color;
    }

    public Integer getColor(){
        return COLOR;
    }
    public String getId(){
        return ID;
    }
    public String getName(){
        return NAME;
    }

    public String getDevice() {
        return DEVICE;
    }
}
