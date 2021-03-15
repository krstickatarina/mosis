package rs.elfak.mosis.katarina.wifinder;

import com.google.android.gms.maps.model.LatLng;

public class WiFiPassword {
    private String name;
    private CurrentLocation location;
    private String wifiPassword;

    public WiFiPassword(){}

    public WiFiPassword(String name, CurrentLocation location, String wifiPassword)
    {
        this.name = name;
        this.location = location;
        this.wifiPassword = wifiPassword;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public CurrentLocation getLocation()
    {
        return this.location;
    }

    public void setLocation(CurrentLocation location)
    {
        this.location = location;
    }

    public String getWifiPassword()
    {
        return this.wifiPassword;
    }

    public void setWifiPassword(String wifiPassword)
    {
        this.wifiPassword = wifiPassword;
    }
}
