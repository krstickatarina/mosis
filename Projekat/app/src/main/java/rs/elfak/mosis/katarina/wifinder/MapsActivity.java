package rs.elfak.mosis.katarina.wifinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.Maps;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private DatabaseReference currentUserReference, currentUserFriendshipsReference, wifiPasswordsReference, wifiPasswordSuggestionsReference;
    private StorageReference storageReference;
    private String currentUserID;
    private FirebaseAuth fAuth;
    private CurrentLocation currentUserLocation;
    private LocationManager locationManager;
    private static final int MIN_TIME = 1000;
    private static final int MIN_DISTANCE = 1;
    private HashMap<String, Marker> mapMarkers = new HashMap<String, Marker>();

    //Parameters for radius
    private String radiusString = "";
    private boolean drawRadius = false;
    private EditText editTextRadius;
    private Button btnRadius;
    private Circle radiusCircle;

    //Notifications
    private ArrayList<String> arrayListUserIds = new ArrayList<String>();
    private DatabaseReference geoFireReference = FirebaseDatabase.getInstance().getReference().child("Geofence");
    private GeoFire geoFire = new GeoFire(geoFireReference);
    private ImageButton getNotifications;
    private boolean doIGetNotifications = false;

    //Adding objects
    private ImageButton addObjectImageBtn;

    //Animate camera to my location
    private ImageButton showMyLocation;

    //Search objects
    private ImageButton searchObjectsOnMap;
    private RelativeLayout relativeLayoutForSearchingPlace;
    private EditText editTextNameOfAPlace;
    private Button findPlace;

    //Admin
    private String adminID = "npGXbgIrWsfyioefTknKcihx1Qc2";
    private RelativeLayout relativeLayoutForAcceptingSuggestion;
    private Button acceptSuggestion;
    private Button denySuggestion;
    private WiFiPasswordSuggestion wiFiPasswordSuggestionClicked;
    private String wiFiPasswordSuggestionClickedKey;
    private Marker markerClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fAuth = FirebaseAuth.getInstance();
        currentUserID = fAuth.getCurrentUser().getUid();

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.LTGRAY);
        actionBar.setBackgroundDrawable(colorDrawable);
        setTitle("WiFinder");


        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        currentUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        currentUserFriendshipsReference = FirebaseDatabase.getInstance().getReference().child("Friendships").child(currentUserID);
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");

        //Radius

        editTextRadius = findViewById(R.id.editText_radius);
        btnRadius = findViewById(R.id.btn_radius);
        btnRadius.setBackgroundColor(Color.LTGRAY);

        btnRadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formRadius();
            }
        });

        //Notifications
        createNotificationChannel();
        getNotifications = findViewById(R.id.imageBtn_notifications);

        getNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doIGetNotifications = !doIGetNotifications;
            }
        });

        //Adding object
        addObjectImageBtn = findViewById(R.id.imageBtn_addObject);
        addObjectImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, AddNewObjectOnMapActivity.class));
            }
        });

        //Reading objects
        wifiPasswordsReference = FirebaseDatabase.getInstance().getReference().child("WifiPasswords");

        //Animating camera to show my location
        showMyLocation = findViewById(R.id.imageBtn_showMyLocation);

        showMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), 16));
            }
        });


        //Search objects on map
        searchObjectsOnMap = findViewById(R.id.imageBtn_searchObjectsOnMap);
        relativeLayoutForSearchingPlace = findViewById(R.id.relativeLayout_searchPlace);
        editTextNameOfAPlace = findViewById(R.id.editText_insertNameOfAPlace);
        findPlace = findViewById(R.id.btn_findPlaceByName);
        findPlace.setBackgroundColor(Color.LTGRAY);

        searchObjectsOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayoutForSearchingPlace.setVisibility(View.VISIBLE);
            }
        });

        findPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameOfAPlaceString = editTextNameOfAPlace.getText().toString();
                if(nameOfAPlaceString.isEmpty())
                {
                    editTextNameOfAPlace.setError("You have to enter name of a place");
                    editTextNameOfAPlace.requestFocus();
                }
                else
                {
                    findPlaceByItsName(nameOfAPlaceString);
                }
            }
        });


        //Admin
        wifiPasswordSuggestionsReference = FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions");
        relativeLayoutForAcceptingSuggestion = findViewById(R.id.relativeLayout_suggestedPassword);
        acceptSuggestion = findViewById(R.id.btn_acceptSuggestion);
        denySuggestion = findViewById(R.id.btn_denySuggestion);

        if(!currentUserID.equals(adminID))
        {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        else
        {
            searchObjectsOnMap.setVisibility(View.GONE);
            showMyLocation.setVisibility(View.GONE);
            addObjectImageBtn.setVisibility(View.GONE);
            getNotifications.setVisibility(View.GONE);
            editTextRadius.setVisibility(View.GONE);
            btnRadius.setVisibility(View.GONE);
        }

    }

    private void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            CharSequence name = "channel";
            String description = "channel for notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifications", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(currentUserID.equals(adminID))
        {
            showAllWifiSuggestions();
        }
        else
        {
            checkPermissionForLocationUpdates();
            listenToUsersChanges();
            listenToWifiPasswordsChanges();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null)
        {
            currentUserReference.child("myLocation").setValue(new CurrentLocation(location.getLatitude(), location.getLongitude()));
            currentUserLocation = new CurrentLocation(location.getLatitude(), location.getLongitude());
            if(drawRadius)
            {
                if(radiusCircle != null)
                    radiusCircle.remove();
                addRadiusCircle(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), Float.valueOf(radiusString));
            }
            formQueryForSendingNotifications(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void checkPermissionForLocationUpdates()
    {
        if(locationManager!=null)
        {
            if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
                else
                    Toast.makeText(MapsActivity.this, "Provider isn't enabled!", Toast.LENGTH_SHORT).show();
            }
            else
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            if(requestCode == 44)
            {

            }
            else if(requestCode == 101)
                checkPermissionForLocationUpdates();
        }
        else
            Toast.makeText(MapsActivity.this, "Permission is required!", Toast.LENGTH_SHORT).show();
    }

    private void listenToUsersChanges()
    {
        currentUserReference.getParent().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists())
                {
                    try
                    {
                        showAllUsersOnTheMap(snapshot.getValue(User.class), snapshot.getKey());
                    }
                    catch(Exception e)
                    {
                        //Toast.makeText(MapsActivity.this, "Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists())
                {
                    try
                    {
                        Marker marker = mapMarkers.get(snapshot.getKey());
                        marker.remove();
                        mapMarkers.remove(snapshot.getKey());
                        showAllUsersOnTheMap(snapshot.getValue(User.class), snapshot.getKey());
                    }
                    catch (Exception e)
                    {
                        //Toast.makeText(MapsActivity.this, "Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showAllUsersOnTheMap(User value, String key)
    {
        currentUserFriendshipsReference.orderByKey().equalTo(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    showFriendOrMeOnTheMap(value, key, "friend");
                }
                else
                {
                    if(key.equals(currentUserID))
                        showFriendOrMeOnTheMap(value, key, "me");
                    else
                        showUserOnTheMap(value, key);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showUserOnTheMap(User value, String key)
    {
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(value.getMyLocation().getLatitude(), value.getMyLocation().getLongitude()))
                                        .icon(formBitmapFromVector(getApplicationContext(), R.drawable.ic_baseline_person_pin_24)));
        if(key.equals(currentUserID))
        {
            marker.setTitle("I am here!");
            marker.setSnippet("It is me!");
        }
        else
        {
            marker.setTitle(value.getUsername());
            marker.setSnippet(value.getFirstName()+" "+value.getLastName());
        }
        mMap.setOnMarkerClickListener(MapsActivity.this);
        mapMarkers.put(key, marker);
    }

    private void showFriendOrMeOnTheMap(User value, String key, String friendOrMe)
    {
        storageReference.child(key).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(MapsActivity.this)
                        .asBitmap()
                        .load(uri.toString())
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                Bitmap smallMarker = Bitmap.createScaledBitmap(resource, 100, 100, false);
                                Marker newMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(value.getMyLocation().getLatitude(), value.getMyLocation().getLongitude()))
                                                                                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                                if(friendOrMe.equals("me"))
                                {
                                    newMarker.setTitle("I am here!");
                                    newMarker.setSnippet("It's me!");
                                }
                                else
                                {
                                    newMarker.setTitle(value.getUsername());
                                    newMarker.setSnippet(value.getFirstName()+" "+value.getLastName());
                                }
                                mMap.setOnMarkerClickListener(MapsActivity.this);
                                mapMarkers.put(key, newMarker);
                                return true;
                            }
                        })
                        .centerCrop()
                        .preload();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showUserOnTheMap(value, key);
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    private BitmapDescriptor formBitmapFromVector(Context applicationContext, int id)
    {
        Drawable vectorDrawable = ContextCompat.getDrawable(applicationContext, id);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void formRadius()
    {
        if (radiusCircle != null)
        {
            radiusCircle.remove();
        }
        if(!editTextRadius.getText().toString().isEmpty())
        {
            if(!editTextRadius.getText().toString().equals(radiusString))
            {
                if(radiusString.equals(""))
                    drawRadius = !drawRadius;
                radiusString = editTextRadius.getText().toString();
                addRadiusCircle(new LatLng(currentUserLocation.getLatitude(), currentUserLocation.getLongitude()), Float.valueOf(radiusString));
            }
            else
            {
                drawRadius = !drawRadius;
                radiusString = "";
            }
        }
    }

    private void addRadiusCircle(LatLng latLng, float radius)
    {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        circleOptions.visible(true);
        radiusCircle = mMap.addCircle(circleOptions);
    }

    private void formQueryForSendingNotifications(Location location)
    {
        geoFire.setLocation(currentUserID, new GeoLocation(location.getLatitude(), location.getLongitude()));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 2);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!key.equals(currentUserID))
                {
                    arrayListUserIds.add(key);
                    if(doIGetNotifications)
                    {
                        Toast.makeText(MapsActivity.this, ""+doIGetNotifications, Toast.LENGTH_SHORT).show();
                        sendNotification();
                    }
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void sendNotification()
    {
        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notifications")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("SPOTTED!")
                .setContentText("User spotted nearby. \n Tap to see the map")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(100, builder.build());
    }

    private void listenToWifiPasswordsChanges()
    {
        wifiPasswordsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists())
                {
                    if(snapshot.getValue(WiFiPassword.class).getUsersThatKnowsThisPasswordID().contains(currentUserID))
                        showWifiPasswordAsKnownToCurrentUser(snapshot.getValue(WiFiPassword.class), snapshot.getKey());
                    else
                        showWifiPasswordAsUnknownToCurrentUser(snapshot.getValue(WiFiPassword.class), snapshot.getKey());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists())
                {
                    if(snapshot.getValue(WiFiPassword.class).getUsersThatKnowsThisPasswordID().contains(currentUserID))
                        showWifiPasswordAsKnownToCurrentUser(snapshot.getValue(WiFiPassword.class), snapshot.getKey());
                    else
                        showWifiPasswordAsUnknownToCurrentUser(snapshot.getValue(WiFiPassword.class), snapshot.getKey());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showWifiPasswordAsUnknownToCurrentUser(WiFiPassword wiFiPassword, String wiFiPasswordKey)
    {
        Marker marker1 = mapMarkers.get(wiFiPasswordKey);
        if(marker1 != null)
        {
            marker1.remove();
            mapMarkers.remove(wiFiPasswordKey);
        }
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(wiFiPassword.getLocation().getLatitude(), wiFiPassword.getLocation().getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .title("Costs 50 tokens"));
        mMap.setOnMarkerClickListener(MapsActivity.this);

        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                if(marker.getTitle().equals("Costs 50 tokens"))
                {
                    checkIfCurrentUserCanBuyThisPassword(wiFiPassword, wiFiPasswordKey);
                }
            }
        });

        mapMarkers.put(wiFiPasswordKey, marker);
    }

    private void showWifiPasswordAsKnownToCurrentUser(WiFiPassword wiFiPassword, String wiFiPasswordKey)
    {
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(wiFiPassword.getLocation().getLatitude(), wiFiPassword.getLocation().getLongitude()))
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                                        .title(wiFiPassword.getWifiPassword()));
        mMap.setOnMarkerClickListener(MapsActivity.this);
        mapMarkers.put(wiFiPasswordKey, marker);
    }

    private void checkIfCurrentUserCanBuyThisPassword(WiFiPassword wiFiPassword, String wiFiPasswordKey)
    {
        currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    if(snapshot.getValue(User.class).getNumberOfTokens()<=-50)
                    {
                        User newUser = new User(snapshot.getValue(User.class).getFirstName(),
                                snapshot.getValue(User.class).getLastName(),
                                snapshot.getValue(User.class).getUsername(),
                                snapshot.getValue(User.class).getEmailAddress(),
                                snapshot.getValue(User.class).getPassword(),
                                snapshot.getValue(User.class).getPhoneNumber(),
                                snapshot.getValue(User.class).getNumberOfTokens()+50);
                        currentUserReference.setValue(newUser);
                        addTokensToTheOtherUser(wiFiPassword, wiFiPasswordKey);
                        addUserToListOfUsersThatKnowsPassword(wiFiPassword, wiFiPasswordKey);
                        Toast.makeText(MapsActivity.this, "You have successfully unlocked this password!", Toast.LENGTH_SHORT).show();
                        Marker marker = mapMarkers.get(wiFiPasswordKey);
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        marker.setTitle(wiFiPassword.getWifiPassword());
                    }
                    else
                        Toast.makeText(MapsActivity.this, "You don't have enough tokens to unlock this password!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addUserToListOfUsersThatKnowsPassword(WiFiPassword wiFiPassword, String wiFiPasswordKey)
    {
        wiFiPassword.getUsersThatKnowsThisPasswordID().add(currentUserID);
        wifiPasswordsReference.child(wiFiPasswordKey).setValue(wiFiPassword);
    }

    private void addTokensToTheOtherUser(WiFiPassword wiFiPassword, String wiFiPasswordKey)
    {
        currentUserReference.getParent().child(wiFiPassword.getUserThatDiscoveredThisPasswordID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    User newUser = new User(snapshot.getValue(User.class).getFirstName(),
                            snapshot.getValue(User.class).getLastName(),
                            snapshot.getValue(User.class).getUsername(),
                            snapshot.getValue(User.class).getEmailAddress(),
                            snapshot.getValue(User.class).getPassword(),
                            snapshot.getValue(User.class).getPhoneNumber(),
                            snapshot.getValue(User.class).getNumberOfTokens()-50);
                    currentUserReference.getParent().child(wiFiPassword.getUserThatDiscoveredThisPasswordID()).setValue(newUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void findPlaceByItsName(String nameOfAPlace)
    {
        wifiPasswordsReference.orderByChild("name").equalTo(nameOfAPlace).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot s: snapshot.getChildren())
                    {
                        Toast.makeText(MapsActivity.this, ""+s, Toast.LENGTH_SHORT).show();
                        relativeLayoutForSearchingPlace.setVisibility(View.GONE);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(s.getValue(WiFiPassword.class).getLocation().getLatitude(), s.getValue(WiFiPassword.class).getLocation().getLongitude()), 16));
                    }
                }
                else
                {
                    Toast.makeText(MapsActivity.this, "Place you are searching for isn't discovered yet!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    //Admin

    private void showAllWifiSuggestions()
    {
        wifiPasswordSuggestionsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists())
                {
                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(snapshot.getValue(WiFiPasswordSuggestion.class).getLocation().getLatitude(), snapshot.getValue(WiFiPasswordSuggestion.class).getLocation().getLongitude()))
                            .title("Suggestion").snippet(snapshot.getValue(WiFiPasswordSuggestion.class).getWiFiPasswordSuggestion())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    mapMarkers.put(snapshot.getKey(), marker);
                    mMap.setOnMarkerClickListener(MapsActivity.this);
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            Intent intent = new Intent(MapsActivity.this, WiFiSuggestionsOfAPlaceActivity.class);
                            intent.putExtra("longitude", marker.getPosition().longitude);
                            intent.putExtra("latitude", marker.getPosition().latitude);
                            startActivity(intent);
                            //findThePlaceAndDeleteIt(marker, false);
                        }
                    });

                    mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener() {
                        @Override
                        public void onInfoWindowLongClick(Marker marker) {
                            //findThePlaceAndDeleteIt(marker, true);
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void findThePlaceAndDeleteIt(Marker marker, boolean justDeleteItOrNot)
    {
        wifiPasswordSuggestionsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    CurrentLocation locationOfMarker = new CurrentLocation(marker.getPosition().latitude, marker.getPosition().longitude);
                    for(DataSnapshot s: snapshot.getChildren())
                    {
                        if(s.getValue(WiFiPasswordSuggestion.class).getLocation().getLatitude() == locationOfMarker.getLatitude()
                        && s.getValue(WiFiPasswordSuggestion.class).getLocation().getLongitude() == locationOfMarker.getLongitude())
                        {
                            Toast.makeText(MapsActivity.this, "BU"+s.getKey(), Toast.LENGTH_SHORT).show();

                            wifiPasswordSuggestionsReference.child(s.getKey()).removeValue();
                            marker.remove();
                            mapMarkers.remove(s.getKey());
                            if(!justDeleteItOrNot)
                            {
                                String key = wifiPasswordsReference.push().getKey();
                                wifiPasswordsReference.child(key).setValue(new WiFiPassword(s.getValue(WiFiPasswordSuggestion.class).getName(),
                                                                                new CurrentLocation(s.getValue(WiFiPasswordSuggestion.class).getLocation().getLatitude(), s.getValue(WiFiPasswordSuggestion.class).getLocation().getLongitude()),
                                                                                s.getValue(WiFiPasswordSuggestion.class).getWiFiPasswordSuggestion(),
                                                                                s.getValue(WiFiPasswordSuggestion.class).getUserSuggesterID()));
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addTokensToUserSuggester(String userSuggesterID)
    {
        currentUserReference.getParent().child(userSuggesterID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    User newUser = new User(snapshot.getValue(User.class).getFirstName(),
                            snapshot.getValue(User.class).getLastName(),
                            snapshot.getValue(User.class).getUsername(),
                            snapshot.getValue(User.class).getEmailAddress(),
                            snapshot.getValue(User.class).getPassword(),
                            snapshot.getValue(User.class).getPhoneNumber(),
                            snapshot.getValue(User.class).getNumberOfTokens()-50);
                    currentUserReference.getParent().child(userSuggesterID).setValue(newUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.users_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(android.R.id.home==item.getItemId())
        {
            startActivity(new Intent(MapsActivity.this, HomeActivity.class));
        }
        else if(item.getItemId() == R.id.logOut_btn)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MapsActivity.this, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}