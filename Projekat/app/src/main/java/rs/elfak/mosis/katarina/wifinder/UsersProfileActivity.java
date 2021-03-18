package rs.elfak.mosis.katarina.wifinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UsersProfileActivity extends AppCompatActivity {

    private TextView usersUsername, usersNumberOfTokens;
    private Button sendCancelFriendRequest, declineFriendRequest;
    private ImageView usersProfileImage;
    private String currentUsersID, usersID;
    private DatabaseReference friendRequestsReference, friendshipsReference, usersReference, wifiReference;
    private String sentFriendRequest="";
    private User currentUser, userProfile;
    private StorageReference storageReference;
    private String backActivity="";
    private String backID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#EEB245"));
        actionBar.setBackgroundDrawable(colorDrawable);

        Bundle extras = getIntent().getExtras();
        if(extras!=null)
        {
            usersID = extras.getString("usersID");
            backActivity = extras.getString("backActivity");
            if(backActivity.equals("FriendsProfileActivity"))
            {
                backID = extras.getString("backID");
            }
        }

        usersUsername = findViewById(R.id.usersProfile_usersUsername_textView);
        usersNumberOfTokens = findViewById(R.id.usersProfile_usersNumberOfTokens_textView);
        usersProfileImage = findViewById(R.id.usersProfile_usersProfileImage_imageView);
        sendCancelFriendRequest = findViewById(R.id.usersProfile_sendCancelFriendRequest_button);
        declineFriendRequest = findViewById(R.id.usersProfile_denyFriendRequest);

        currentUsersID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendRequestsReference = FirebaseDatabase.getInstance().getReference().child("Friend requests");
        friendshipsReference = FirebaseDatabase.getInstance().getReference().child("Friendships");
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child(usersID);
        wifiReference = FirebaseDatabase.getInstance().getReference().child("WifiPasswords");

        checkIfUsersSentFriendRequestToEachOther();

        getUsers();

        sendCancelFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sentFriendRequest.equals("Accept"))
                {
                    friendRequestsReference.child(currentUsersID).child(usersID).removeValue();
                    friendshipsReference.child(currentUsersID).child(usersID).setValue(userProfile);
                    friendshipsReference.child(usersID).child(currentUsersID).setValue(currentUser);
                    wifiReference.orderByChild("userThatDiscoveredThisPasswordID").equalTo(currentUsersID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists())
                            {
                                for(DataSnapshot s:snapshot.getChildren())
                                {
                                    WiFiPassword wiFiPassword = s.getValue(WiFiPassword.class);
                                    if(!wiFiPassword.getUsersThatKnowsThisPasswordID().contains(usersID))
                                    {
                                        wiFiPassword.getUsersThatKnowsThisPasswordID().add(usersID);
                                        wifiReference.child(s.getKey()).setValue(wiFiPassword);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    wifiReference.orderByChild("userThatDiscoveredThisPasswordID").equalTo(usersID).addValueEventListener(new ValueEventListener() {
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
                                        wifiReference.child(d.getKey()).setValue(wiFiPassword);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Intent intent = new Intent(UsersProfileActivity.this, FriendsProfileActivity.class);
                    intent.putExtra("usersID", usersID);
                    startActivity(intent);
                    finish();
                }
                else if(sentFriendRequest.equals("Cancel"))
                {
                    friendRequestsReference.child(usersID).child(currentUsersID).removeValue();
                    sendCancelFriendRequest.setText("Send friend request");
                    sentFriendRequest = "Send";
                }
                else
                {
                    friendRequestsReference.child(usersID).child(currentUsersID).setValue(currentUser);
                    sendCancelFriendRequest.setText("Cancel friend request");
                    sentFriendRequest = "Cancel";
                }
            }
        });
    }

    private void getUsers()
    {
        usersReference.orderByKey().equalTo(currentUsersID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    currentUser = snapshot.child(currentUsersID).getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        usersReference.orderByKey().equalTo(usersID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    userProfile = snapshot.child(usersID).getValue(User.class);
                    Toast.makeText(UsersProfileActivity.this, userProfile.getUsername(), Toast.LENGTH_SHORT).show();
                    usersUsername.setText("@"+userProfile.getUsername());
                    usersNumberOfTokens.setText(String.valueOf(-1*userProfile.getNumberOfTokens())+" tokens");
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(UsersProfileActivity.this)
                                    .asBitmap()
                                    .load(uri.toString())
                                    .listener(new RequestListener<Bitmap>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                            usersProfileImage.setImageBitmap(resource);
                                            return true;
                                        }
                                    })
                                    .centerCrop()
                                    .preload();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        declineFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendRequestsReference.child(currentUsersID).child(usersID).removeValue();
                sendCancelFriendRequest.setText("Send friend request");
                declineFriendRequest.setVisibility(View.GONE);
                sentFriendRequest = "Send";
            }
        });
    }

    private void checkIfUsersSentFriendRequestToEachOther()
    {
        friendRequestsReference.child(currentUsersID).orderByKey().equalTo(usersID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    sendCancelFriendRequest.setText("Accept");
                    sentFriendRequest = "Accept";
                    declineFriendRequest.setVisibility(View.VISIBLE);
                }
                else
                {
                    friendRequestsReference.child(usersID).orderByKey().equalTo(currentUsersID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists())
                            {
                                sendCancelFriendRequest.setText("Cancel friend request");
                                sentFriendRequest = "Cancel";
                            }
                            else
                            {
                                sendCancelFriendRequest.setText("Send friend request");
                                sentFriendRequest = "Send";
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
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
            if(backActivity.equals("RangListActivity"))
            {
                startActivity(new Intent(UsersProfileActivity.this, RangListActivity.class));
            }
            else if(backActivity.equals("FindFriendsActivity"))
            {
                startActivity(new Intent(UsersProfileActivity.this, FindFriendsActivity.class));
            }
            else if(backActivity.equals("Notifications"))
            {
                startActivity(new Intent(UsersProfileActivity.this, NotificationsFriendRequestsActivity.class));
            }
            else if(backActivity.equals("FriendsProfileActivity"))
            {
                Intent intent = new Intent(UsersProfileActivity.this, FriendsProfileActivity.class);
                intent.putExtra("usersID", backID);
                intent.putExtra("backActivity", "MyProfileFriends");
                startActivity(intent);
            }
        }
        else if(item.getItemId() == R.id.logOut_btn)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(UsersProfileActivity.this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}