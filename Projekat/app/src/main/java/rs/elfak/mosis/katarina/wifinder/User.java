package rs.elfak.mosis.katarina.wifinder;

import com.google.android.gms.maps.model.LatLng;

public class User {
    private String firstName;
    private String lastName;
    private String username;
    private String emailAddress;
    private String password;
    private String phoneNumber;
    private int numberOfTokens;
    private CurrentLocation myLocation;

    public User()
    {
    }

    public User(String firstName, String lastName, String username, String emailAddress, String password, String phoneNumber, int numberOfTokens)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.emailAddress = emailAddress;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.numberOfTokens = numberOfTokens;
        this.myLocation = new CurrentLocation();
    }

    public String getFirstName()
    {
        return this.firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return this.lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getPhoneNumber()
    {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getUsername()
    {
        return this.username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getEmailAddress()
    {
        return this.emailAddress;
    }

    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }

    public String getPassword()
    {
        return this.password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public int getNumberOfTokens()
    {
        return this.numberOfTokens;
    }

    public void setNumberOfTokens(int numberOfTokens)
    {
        this.numberOfTokens = numberOfTokens;
    }

    public CurrentLocation getMyLocation()
    {
        return this.myLocation;
    }

    public void setMyLocation(CurrentLocation myLocation)
    {
        this.myLocation = myLocation;
    }


}
