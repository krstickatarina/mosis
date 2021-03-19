package rs.elfak.mosis.katarina.wifinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class NotificationsFriendRequestsActivity extends AppCompatActivity {

    RelativeLayout relativeLayout;
    RecyclerView recyclerView;
    FirebaseRecyclerAdapter<User, NotificationsHolder> adapter;
    FirebaseRecyclerOptions<User> options;
    Query query;
    StorageReference storageReference;
    String currentUsersID;
    DatabaseReference friendshipsReference, friendRequestsReference, wiFiPasswords;
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_friend_requests);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        relativeLayout = findViewById(R.id.notifications_secondRelativeLayout);
        relativeLayout.setBackgroundColor(Color.parseColor("#FFFF66"));
        recyclerView = findViewById(R.id.recyclerViewNotifications);

        currentUsersID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
        query = FirebaseDatabase.getInstance().getReference().child("Friend requests").child(currentUsersID);
        friendRequestsReference = FirebaseDatabase.getInstance().getReference().child("Friend requests").child(currentUsersID);
        friendshipsReference = FirebaseDatabase.getInstance().getReference().child("Friendships");
        wiFiPasswords = FirebaseDatabase.getInstance().getReference().child("WifiPasswords");
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseDatabase.getInstance().getReference().child("Users").child(currentUsersID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    currentUser = snapshot.getValue(User.class);
                    LoadAllFriendRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void LoadAllFriendRequests()
    {
        options = new FirebaseRecyclerOptions.Builder<User>().setQuery(query, User.class).build();
        adapter = new FirebaseRecyclerAdapter<User, NotificationsHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull NotificationsHolder holder, int position, @NonNull User model) {
                holder.usersUsername.setText(model.getFirstName()+" "+model.getLastName()+"    @"+model.getUsername());
                holder.usersID.setText(getRef(position).getKey());
                String user2ID = getRef(position).getKey();
                User user2 = model;
                storageReference.child(getRef(position).getKey()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(NotificationsFriendRequestsActivity.this)
                                .asBitmap()
                                .load(uri.toString())
                                .listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        holder.usersProfileImage.setImageBitmap(resource);
                                        return true;
                                    }
                                })
                                .centerCrop()
                                .preload();
                    }
                });

                holder.usersProfileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NotificationsFriendRequestsActivity.this, UsersProfileActivity.class);
                        intent.putExtra("usersID", getRef(position).getKey());
                        intent.putExtra("backActivity", "Notifications");
                        startActivity(intent);
                    }
                });

                holder.usersUsername.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NotificationsFriendRequestsActivity.this, UsersProfileActivity.class);
                        intent.putExtra("usersID", getRef(position).getKey());
                        intent.putExtra("backActivity", "Notifications");
                        startActivity(intent);
                    }
                });

                holder.declineFriendRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        friendRequestsReference.child(getRef(position).getKey()).removeValue();
                    }
                });

                holder.acceptFriendRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        friendRequestsReference.child(user2ID).removeValue();
                        friendshipsReference.child(currentUsersID).child(user2ID).setValue(user2);
                        friendshipsReference.child(user2ID).child(currentUsersID).setValue(currentUser);
                        exchangeWiFiPasswords(user2ID);
                    }
                });
            }

            @NonNull
            @Override
            public NotificationsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notifications_one_friend_request_layout, parent, false);
                return new NotificationsHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void exchangeWiFiPasswords(String user2ID)
    {
        wiFiPasswords.orderByChild("userThatDiscoveredThisPasswordID").equalTo(currentUsersID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot d:snapshot.getChildren())
                    {
                        WiFiPassword wiFiPassword = d.getValue(WiFiPassword.class);
                        if(!wiFiPassword.getUsersThatKnowsThisPasswordID().contains(user2ID))
                        {
                            wiFiPassword.getUsersThatKnowsThisPasswordID().add(user2ID);
                            wiFiPasswords.child(d.getKey()).setValue(wiFiPassword);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        wiFiPasswords.orderByChild("userThatDiscoveredThisPasswordID").equalTo(user2ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    for(DataSnapshot d:snapshot.getChildren())
                    {
                        WiFiPassword wiFiPassword = d.getValue(WiFiPassword.class);
                        if(!wiFiPassword.getUsersThatKnowsThisPasswordID().contains(currentUsersID))
                        {
                            wiFiPassword.getUsersThatKnowsThisPasswordID().add(currentUsersID);
                            wiFiPasswords.child(d.getKey()).setValue(wiFiPassword);
                        }
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
            startActivity(new Intent(NotificationsFriendRequestsActivity.this, HomeActivity.class));
        }
        else if(item.getItemId() == R.id.logOut_btn)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(NotificationsFriendRequestsActivity.this, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}