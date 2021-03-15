package rs.elfak.mosis.katarina.wifinder;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class WiFiPassword {
    private String name;
    private CurrentLocation location;
    private String wifiPassword;
    private String userThatDiscoveredThisPasswordID;
    private ArrayList<String> usersThatKnowsThisPasswordID;

    public WiFiPassword(){}

    public WiFiPassword(String name, CurrentLocation location, String wifiPassword, String userThatDiscoveredThisPasswordID)
    {
        this.name = name;
        this.location = location;
        this.wifiPassword = wifiPassword;
        this.userThatDiscoveredThisPasswordID = userThatDiscoveredThisPasswordID;
        usersThatKnowsThisPasswordID = new ArrayList<String>();
        usersThatKnowsThisPasswordID.add(this.userThatDiscoveredThisPasswordID);
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

    public String getUserThatDiscoveredThisPasswordID()
    {
        return this.userThatDiscoveredThisPasswordID;
    }

    public void setUserThatDiscoveredThisPasswordID(String userThatDiscoveredThisPasswordID)
    {
        this.userThatDiscoveredThisPasswordID = userThatDiscoveredThisPasswordID;
    }

    public ArrayList<String> getUsersThatKnowsThisPasswordID()
    {
        return this.usersThatKnowsThisPasswordID;
    }

    public void setUsersThatKnowsThisPasswordID(ArrayList<String> usersThatKnowsThisPasswordID)
    {
        this.usersThatKnowsThisPasswordID = usersThatKnowsThisPasswordID;
    }
}
