package rs.elfak.mosis.katarina.wifinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class MyProfileFriendsActivity extends AppCompatActivity {

    Button profileInfo, profileFriends;
    ImageView searchFriends;
    EditText nameToSearch;
    RelativeLayout relativeLayout;
    String nameToSearchString = "";
    RecyclerView recyclerView;
    FirebaseRecyclerAdapter<User, MyFriendsHolder> adapter;
    FirebaseRecyclerOptions<User> options;
    DatabaseReference friendshipsReference, wifiPasswords;
    Query query;
    StorageReference storageReference;
    String usersID, currentUsersID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile_friends);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileInfo = findViewById(R.id.profile_friends_info_button);
        profileFriends = findViewById(R.id.profile_friends_friends_button);
        nameToSearch = findViewById(R.id.profile_friends_search_friends);
        searchFriends = findViewById(R.id.profile_friends_search_button);
        relativeLayout = findViewById(R.id.profile_friends_relativeLayout);

        profileInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyProfileFriendsActivity.this, MyProfileInfoActivity.class));
            }
        });

        profileFriends.setBackgroundColor(Color.parseColor("#8A2BE2"));

        currentUsersID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendshipsReference = FirebaseDatabase.getInstance().getReference().child("Friendships").child(currentUsersID);
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
        wifiPasswords = FirebaseDatabase.getInstance().getReference().child("WifiPasswords");
        recyclerView = findViewById(R.id.recyclerViewMyFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LoadAllMyFriends();

        searchFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameToSearch.getText().toString().isEmpty())
                {
                    nameToSearchString = "";
                }
                else
                {
                    nameToSearchString = nameToSearch.getText().toString();
                }
                LoadAllMyFriends();
            }
        });
    }

    private void LoadAllMyFriends()
    {
        query = friendshipsReference.orderByChild("username").startAt(nameToSearchString).endAt(nameToSearchString+"\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<User>().setQuery(query, User.class).build();
        adapter = new FirebaseRecyclerAdapter<User, MyFriendsHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyFriendsHolder holder, int position, @NonNull User model) {
                usersID = getRef(position).getKey();
                holder.friendsID.setText(usersID);
                holder.friendsName.setText(model.getFirstName()+" "+model.getLastName());
                holder.friendsUsername.setText("@"+model.getUsername());
                storageReference.child(usersID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(MyProfileFriendsActivity.this)
                                .asBitmap()
                                .load(uri.toString())
                                .listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        holder.profileImage.setImageBitmap(resource);
                                        return true;
                                    }
                                })
                                .centerCrop()
                                .preload();
                    }
                });

                holder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        holder.relativeLayout2.setVisibility(View.VISIBLE);
                        return true;
                    }
                });

                holder.viewProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.relativeLayout2.setVisibility(View.GONE);
                        //Toast.makeText(MyProfileFriendsActivity.this, holder.friendsID.getText().toString(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MyProfileFriendsActivity.this, FriendsProfileActivity.class);
                        intent.putExtra("usersID", holder.friendsID.getText().toString());
                        intent.putExtra("backActivity", "MyProfileFriends");
                        startActivity(intent);
                    }
                });

                holder.deleteFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        friendshipsReference.child(holder.friendsID.getText().toString()).removeValue();
                        friendshipsReference.getParent().child(holder.friendsID.getText().toString()).child(currentUsersID).removeValue();
                        wifiPasswords.orderByChild("userThatDiscoveredThisPasswordID").equalTo(currentUsersID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    for(DataSnapshot d:snapshot.getChildren())
                                    {
                                        WiFiPassword wiFiPassword = d.getValue(WiFiPassword.class);
                                        wiFiPassword.getUsersThatKnowsThisPasswordID().remove(usersID);
                                        wifiPasswords.child(d.getKey()).setValue(wiFiPassword);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        wifiPasswords.orderByChild("userThatDiscoveredThisPasswordID").equalTo(holder.friendsID.getText().toString()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    for(DataSnapshot d:snapshot.getChildren())
                                    {
                                        WiFiPassword wiFiPassword = d.getValue(WiFiPassword.class);
                                        wiFiPassword.getUsersThatKnowsThisPasswordID().remove(currentUsersID);
                                        wifiPasswords.child(d.getKey()).setValue(wiFiPassword);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        holder.relativeLayout2.setVisibility(View.GONE);
                    }
                });
            }

            @NonNull
            @Override
            public MyFriendsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_profile_one_friend_layout, parent, false);
                return new MyFriendsHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
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
            startActivity(new Intent(MyProfileFriendsActivity.this, HomeActivity.class));
        }
        else if(item.getItemId() == R.id.logOut_btn)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MyProfileFriendsActivity.this, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}