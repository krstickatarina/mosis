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
import android.widget.RelativeLayout;
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
    boolean b = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_suggestions_of_a_place);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                deleteAllWiFiSuggestionsOnSameLocation();
            }
        });

    }

    private void LoadWiFiSuggestions()
    {
        //query = FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions").orderByChild("name").equalTo(nameOfAPlace.getText().toString());
        query = FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions");
        options = new FirebaseRecyclerOptions.Builder<WiFiPasswordSuggestion>().setQuery(query, WiFiPasswordSuggestion.class).build();
        adapter = new FirebaseRecyclerAdapter<WiFiPasswordSuggestion, WiFiSuggestionHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull WiFiSuggestionHolder holder, int position, @NonNull WiFiPasswordSuggestion model) {
                if(model.getLocation().getLatitude() == latitude && model.getLocation().getLongitude() == longitude)
                {
                    holder.wiFiPasswordSuggestion.setText(model.getWiFiPasswordSuggestion());
                    holder.wiFiPasswordSuggestionsID.setText(getRef(position).getKey());

                    holder.relativeLayoutAll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.relativeLayout.setVisibility(View.VISIBLE);
                        }
                    });

                    holder.denyBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions").child(holder.wiFiPasswordSuggestionsID.getText().toString()).removeValue();
                        }
                    });

                    holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference().child("WifiPasswords").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists())
                                    {
                                        for(DataSnapshot s:snapshot.getChildren())
                                        {
                                            if(s.getValue(WiFiPasswordSuggestion.class).getLocation().getLatitude() == latitude &&
                                            s.getValue(WiFiPasswordSuggestion.class).getLocation().getLongitude() == longitude)
                                            {
                                                acceptThisWiFiPassword(model, s.getKey());
                                                b = false;
                                            }
                                        }
                                    }
                                    if(b)
                                    {
                                        keyOfAPlace = FirebaseDatabase.getInstance().getReference().child("WifiPasswords").push().getKey();
                                        acceptThisWiFiPassword(model, keyOfAPlace);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists())
                                    {
                                        for(DataSnapshot d:snapshot.getChildren())
                                        {
                                            if(d.getValue(WiFiPasswordSuggestion.class).getLocation().getLatitude() == latitude &&
                                            d.getValue(WiFiPasswordSuggestion.class).getLocation().getLongitude() == longitude)
                                            {
                                                FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions").child(d.getKey()).removeValue();
                                            }
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
                else
                {
                    holder.relativeLayoutAll.setVisibility(View.GONE);
                    holder.relativeLayoutAll.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
                }
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

    private void deleteAllWiFiSuggestionsOnSameLocation()
    {
        FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot s:snapshot.getChildren())
                    {
                        if(s.getValue(WiFiPasswordSuggestion.class).getLocation().getLongitude() == longitude &&
                        s.getValue(WiFiPasswordSuggestion.class).getLocation().getLatitude() == latitude)
                        {
                            FirebaseDatabase.getInstance().getReference().child("WiFiSuggestions").child(s.getKey()).removeValue();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private WiFiPassword acceptThisWiFiPassword(WiFiPasswordSuggestion model, String id)
    {
        WiFiPassword wiFiPassword = new WiFiPassword(model.getName(), model.getLocation(), model.getWiFiPasswordSuggestion(), model.getUserSuggesterID());
        FirebaseDatabase.getInstance().getReference().child("Friendships").child(model.getUserSuggesterID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    //boolean p = true;
                    for(DataSnapshot d:snapshot.getChildren())
                    {
                        wiFiPassword.getUsersThatKnowsThisPasswordID().add(d.getKey());
                    }
                    //if(p)
                    //{
                        FirebaseDatabase.getInstance().getReference().child("WifiPasswords").child(id).setValue(wiFiPassword);
                    //    p=false;
                    //}
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return null;
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
        }
        return super.onOptionsItemSelected(item);
    }
}