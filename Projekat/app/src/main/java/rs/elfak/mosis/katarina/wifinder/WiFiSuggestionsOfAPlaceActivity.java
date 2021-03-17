package rs.elfak.mosis.katarina.wifinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class WiFiSuggestionsOfAPlaceActivity extends AppCompatActivity {

    FirebaseRecyclerAdapter<WiFiPasswordSuggestion, WiFiSuggestionHolder> adapter;
    FirebaseRecyclerOptions<WiFiPasswordSuggestion> options;
    RecyclerView recyclerView;
    Query query;
    TextView nameOfAPlace, longitudeOfAPlace, latitudeOfAPlace;
    double latitude, longitude;
    CurrentLocation location;
    String keyOfAPlace;
    Button denyAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_suggestions_of_a_place);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#EEB245"));
        actionBar.setBackgroundDrawable(colorDrawable);

        Bundle extras = getIntent().getExtras();
        if(extras!=null)
        {
            latitude = extras.getDouble("latitude");
            longitude = extras.getDouble("longitude");
            location = new CurrentLocation(latitude, longitude);
        }

        nameOfAPlace = findViewById(R.id.wiFiSuggestions_nameOfAPlace);
        latitudeOfAPlace = findViewById(R.id.wiFiSuggestions_latitudeOfAPlace);
        longitudeOfAPlace = findViewById(R.id.wiFiSuggestions_longitudeOfAPlace);

        recyclerView = findViewById(R.id.recyclerViewWiFiSuggestions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot s:snapshot.getChildren())
                    {
                        if(s.getValue(WiFiPasswordSuggestion.class).getLocation().getLatitude() == latitude
                        && s.getValue(WiFiPasswordSuggestion.class).getLocation().getLongitude() == longitude)
                        {
                            nameOfAPlace.setText(s.getValue(WiFiPasswordSuggestion.class).getName());
                            latitudeOfAPlace.setText(String.valueOf(s.getValue(WiFiPasswordSuggestion.class).getLocation().getLatitude()));
                            longitudeOfAPlace.setText(String.valueOf(s.getValue(WiFiPasswordSuggestion.class).getLocation().getLongitude()));
                            LoadWiFiSuggestions();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        denyAll = findViewById(R.id.wiFiSuggestions_denyAllButton);
        denyAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllWiFiSuggestionsWithGivenName();
            }
        });

    }

    private void LoadWiFiSuggestions()
    {
        query = FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions").orderByChild("name").equalTo(nameOfAPlace.getText().toString());
        options = new FirebaseRecyclerOptions.Builder<WiFiPasswordSuggestion>().setQuery(query, WiFiPasswordSuggestion.class).build();
        adapter = new FirebaseRecyclerAdapter<WiFiPasswordSuggestion, WiFiSuggestionHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull WiFiSuggestionHolder holder, int position, @NonNull WiFiPasswordSuggestion model) {
                holder.wiFiPasswordSuggestion.setText(model.getWiFiPasswordSuggestion());
                holder.relativeLayoutAll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.relativeLayout.setVisibility(View.VISIBLE);
                    }
                });

                holder.denyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions").child(getRef(position).getKey()).removeValue();
                    }
                });

                holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseDatabase.getInstance().getReference().child("WifiPasswords").orderByChild("name").equalTo(nameOfAPlace.getText().toString()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    for(DataSnapshot s:snapshot.getChildren())
                                    {
                                        FirebaseDatabase.getInstance().getReference().child("WifiPasswords").child(s.getKey()).setValue(new WiFiPassword(model.getName(), model.getLocation(), model.getWiFiPasswordSuggestion(), model.getUserSuggesterID()));
                                    }
                                }
                                else
                                {
                                    keyOfAPlace = FirebaseDatabase.getInstance().getReference().child("WifiPasswords").push().getKey();
                                    Toast.makeText(WiFiSuggestionsOfAPlaceActivity.this, keyOfAPlace, Toast.LENGTH_SHORT).show();
                                    FirebaseDatabase.getInstance().getReference().child("WifiPasswords").child(keyOfAPlace).setValue(new WiFiPassword(model.getName(), model.getLocation(), model.getWiFiPasswordSuggestion(), model.getUserSuggesterID()));
                                    deleteAllWiFiSuggestionsWithGivenName();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions").orderByChild("name").equalTo(nameOfAPlace.getText().toString()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    for(DataSnapshot d:snapshot.getChildren())
                                    {
                                        FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions").child(d.getKey()).removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public WiFiSuggestionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_wi_fi_suggestion, parent, false);
                return new WiFiSuggestionHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void deleteAllWiFiSuggestionsWithGivenName()
    {
        FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions").orderByChild("name").equalTo(nameOfAPlace.getText().toString()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot s:snapshot.getChildren())
                    {
                        FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions").child(s.getKey()).removeValue();
                    }
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
            startActivity(new Intent(WiFiSuggestionsOfAPlaceActivity.this, MapsActivity.class));
        }
        else if(item.getItemId() == R.id.logOut_btn)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(WiFiSuggestionsOfAPlaceActivity.this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}