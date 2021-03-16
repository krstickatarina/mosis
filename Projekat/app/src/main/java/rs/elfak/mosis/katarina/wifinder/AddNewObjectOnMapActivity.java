package rs.elfak.mosis.katarina.wifinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.type.LatLng;

public class AddNewObjectOnMapActivity extends AppCompatActivity {

    private EditText editTextLongitude, editTextLatitude, editTextName, editTextPassword;
    private Button btnAddObject;
    private DatabaseReference currentUserReference, newObjectReference;
    private FirebaseAuth fAuth;
    private String currentUserID;
    private User currentUser;

    private ImageButton goBack, logOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_object_on_map);

        editTextLatitude = findViewById(R.id.editText_latitudeOfObject);
        editTextLongitude = findViewById(R.id.editText_longitudeOfObject);
        editTextName = findViewById(R.id.editText_nameOfLocation);
        editTextPassword = findViewById(R.id.editText_passwordSuggestion);
        btnAddObject = findViewById(R.id.btn_addNewObject);

        btnAddObject.setBackgroundColor(Color.GRAY);

        fAuth = FirebaseAuth.getInstance();
        currentUserID = fAuth.getCurrentUser().getUid();
        currentUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        newObjectReference = FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions");

        currentUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                editTextLatitude.setText(snapshot.child("myLocation").child("latitude").getValue().toString());
                editTextLongitude.setText(snapshot.child("myLocation").child("longitude").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btnAddObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameOfLocationString = editTextName.getText().toString();
                double latitudeOfLocation = Double.valueOf(editTextLatitude.getText().toString());
                double longitudeOfLocation = Double.valueOf(editTextLongitude.getText().toString());
                String passwordSuggestionString = editTextPassword.getText().toString();

                if(!nameOfLocationString.isEmpty() && !passwordSuggestionString.isEmpty())
                {
                    String key = newObjectReference.push().getKey();
                    newObjectReference.child(key).setValue(new WiFiPasswordSuggestion(nameOfLocationString, new CurrentLocation(latitudeOfLocation, longitudeOfLocation), passwordSuggestionString, currentUserID));
                    Toast.makeText(AddNewObjectOnMapActivity.this, "Suggestion is added successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddNewObjectOnMapActivity.this, MapsActivity.class));
                    finish();
                }
                else
                {
                    if(nameOfLocationString.isEmpty())
                    {
                        editTextName.setError("Name of location is required!");
                        editTextName.requestFocus();
                    }
                    if(passwordSuggestionString.isEmpty())
                    {
                        editTextPassword.setError("Password suggestion is required!");
                        editTextPassword.requestFocus();
                    }
                    Toast.makeText(AddNewObjectOnMapActivity.this, "Please enter all requested fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        goBack = findViewById(R.id.imgBtn_goBackToMap);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddNewObjectOnMapActivity.this, MapsActivity.class));
            }
        });

        logOut = findViewById(R.id.addNewObject_logOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AddNewObjectOnMapActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}