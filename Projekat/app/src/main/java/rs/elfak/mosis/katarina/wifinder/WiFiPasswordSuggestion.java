package rs.elfak.mosis.katarina.wifinder;

import com.google.android.gms.maps.model.LatLng;

public class WiFiPasswordSuggestion {
    private String name;
    private CurrentLocation location;
    private String wiFiPasswordSuggestion;
    private String userSuggesterID;

    public WiFiPasswordSuggestion(){}

    public WiFiPasswordSuggestion(String name, CurrentLocation location, String wiFiPasswordSuggestion, String userSuggesterID)
    {
        this.name = name;
        this.location = location;
        this.wiFiPasswordSuggestion = wiFiPasswordSuggestion;
        this.userSuggesterID = userSuggesterID;
    }

    public String getName()
    {
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

    public String getWiFiPasswordSuggestion()
    {
        return this.wiFiPasswordSuggestion;
    }

    public void setWiFiPasswordSuggestion(String wiFiPasswordSuggestion)
    {
        this.wiFiPasswordSuggestion = wiFiPasswordSuggestion;
    }

    public String getUserSuggesterID()
    {
        return this.userSuggesterID;
    }

    public void setUserSuggesterID(String userSuggesterID)
    {
        this.userSuggesterID = userSuggesterID;
    }
}
