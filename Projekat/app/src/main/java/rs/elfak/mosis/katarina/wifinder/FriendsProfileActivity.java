package rs.elfak.mosis.katarina.wifinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

public class FriendsProfileActivity extends AppCompatActivity {

    ImageView friendsProfileImage, searchFriends;
    TextView friendsUsername, friendsFirstName, friendsLastName, friendsNumberOfTokens;
    CheckBox friendsOrNot;
    EditText nameForSearchFriends;
    String currentUsersID, usersID, backActivity;
    DatabaseReference usersReference, friendshipsReference, wifiPasswords;
    StorageReference storageReference;
    String backID;

    //List of friends
    FirebaseRecyclerAdapter<User, FriendsOfAFriendHolder> adapter;
    FirebaseRecyclerOptions<User> options;
    RecyclerView recyclerView;
    Query query;
    String nameForSearchFriendsString = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_profile);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        friendsProfileImage = findViewById(R.id.friends_profile_user_image);
        friendsUsername = findViewById(R.id.friends_profile_username);
        friendsFirstName = findViewById(R.id.friends_profile_first_name);
        friendsLastName = findViewById(R.id.friends_profile_last_name);
        friendsNumberOfTokens = findViewById(R.id.friends_profile_points);
        friendsOrNot = findViewById(R.id.friends_profile_checkbox);
        nameForSearchFriends = findViewById(R.id.friends_profile_search_friends);
        searchFriends = findViewById(R.id.friends_profile_search_button);

        currentUsersID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Bundle extras = getIntent().getExtras();
        if(extras != null)
        {
            usersID = extras.getString("usersID");
            backActivity = extras.getString("backActivity");
            if(backActivity.equals("FriendsProfileActivity"))
            {
                backID = extras.getString("backID");
            }
        }

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(usersID);
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child(usersID);
        friendshipsReference = FirebaseDatabase.getInstance().getReference().child("Friendships").child(usersID);
        wifiPasswords = FirebaseDatabase.getInstance().getReference().child("WifiPasswords");
        recyclerView = findViewById(R.id.recyclerViewFriendsProfile);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    friendsUsername.setText("@"+snapshot.getValue(User.class).getUsername());
                    friendsFirstName.setText(snapshot.getValue(User.class).getFirstName());
                    friendsLastName.setText(snapshot.getValue(User.class).getLastName());
                    friendsNumberOfTokens.setText(String.valueOf(snapshot.getValue(User.class).getNumberOfTokens()*-1));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(FriendsProfileActivity.this)
                        .asBitmap()
                        .load(uri.toString())
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                friendsProfileImage.setImageBitmap(resource);
                                return true;
                            }
                        })
                        .centerCrop()
                        .preload();
            }
        });

        LoadFriendsOfAFriend();

        searchFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameForSearchFriends.getText().toString().isEmpty())
                {
                    nameForSearchFriendsString = "";
                    Toast.makeText(FriendsProfileActivity.this, "Please enter username of a friend you are searching for!", Toast.LENGTH_SHORT).show();
                    LoadFriendsOfAFriend();
                }
                else
                {
                    nameForSearchFriendsString = nameForSearchFriends.getText().toString();
                    LoadFriendsOfAFriend();
                }
            }
        });

        friendsOrNot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendshipsReference.child(currentUsersID).removeValue();
                friendshipsReference.getParent().child(currentUsersID).child(usersID).removeValue();
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

                wifiPasswords.orderByChild("userThatDiscoveredThisPasswordID").equalTo(usersID).addValueEventListener(new ValueEventListener() {
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

                Intent intent = new Intent(FriendsProfileActivity.this, UsersProfileActivity.class);
                intent.putExtra("backActivity", "MyProfileFriends");
                intent.putExtra("usersID", usersID);
                startActivity(intent);
            }
        });
    }

    private void LoadFriendsOfAFriend()
    {
        query = friendshipsReference.orderByChild("username").startAt(nameForSearchFriendsString).endAt(nameForSearchFriendsString+"\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<User>().setQuery(query, User.class).build();
        adapter = new FirebaseRecyclerAdapter<User, FriendsOfAFriendHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendsOfAFriendHolder holder, int position, @NonNull User model) {
                holder.username.setText("@"+model.getUsername());
                String thisUsersID = getRef(position).getKey();
                storageReference.getParent().child(thisUsersID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(FriendsProfileActivity.this)
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

                holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(currentUsersID.equals(thisUsersID))
                        {
                            startActivity(new Intent(FriendsProfileActivity.this, MyProfileInfoActivity.class));
                        }
                        friendshipsReference.getParent().child(currentUsersID).orderByKey().equalTo(thisUsersID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    for(DataSnapshot d:snapshot.getChildren())
                                    {
                                        Intent intent = new Intent(FriendsProfileActivity.this, FriendsProfileActivity.class);
                                        intent.putExtra("usersID", thisUsersID);
                                        intent.putExtra("backActivity", "FriendsProfileActivity");
                                        startActivity(intent);
                                    }
                                }
                                else
                                {
                                    Intent intent = new Intent(FriendsProfileActivity.this, UsersProfileActivity.class);
                                    intent.putExtra("usersID", thisUsersID);
                                    intent.putExtra("backID", usersID);
                                    intent.putExtra("backActivity", "FriendsProfileActivity");
                                    startActivity(intent);
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
            public FriendsOfAFriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_profile_one_friend_layout, parent, false);
                return new FriendsOfAFriendHolder(view);
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
            if(backActivity.equals("RangListActivity"))
            {
                startActivity(new Intent(FriendsProfileActivity.this, RangListActivity.class));
            }
            else if(backActivity.equals("FindFriendsActivity"))
            {
                startActivity(new Intent(FriendsProfileActivity.this, FindFriendsActivity.class));
            }
            else if(backActivity.equals("MyProfileFriends"))
            {
                startActivity(new Intent(FriendsProfileActivity.this, MyProfileFriendsActivity.class));
            }
            else if(backActivity.equals("FriendsProfileActivity"))
            {
                Intent intent = new Intent(FriendsProfileActivity.this, FriendsProfileActivity.class);
                intent.putExtra("usersID", backID);
                intent.putExtra("backActivity", "MyProfileFriends");
                startActivity(intent);
            }
        }
        else if(item.getItemId() == R.id.logOut_btn)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(FriendsProfileActivity.this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}