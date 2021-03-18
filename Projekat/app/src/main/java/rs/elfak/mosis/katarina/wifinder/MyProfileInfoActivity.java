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

public class MyProfileInfoActivity extends AppCompatActivity {

    Button profileInfo, profileFriends, editProfile;
    ImageView profileImage;
    TextView username, firstName, lastName, phoneNumber, emailAddress, password, numberOfTokens;
    String currentUsersID;
    DatabaseReference usersReference;
    StorageReference storageReference;
    String backActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile_info);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if(extras!=null)
        {
            backActivity = extras.getString("backActivity");
        }


        profileImage = findViewById(R.id.profile_image);
        profileInfo = findViewById(R.id.profile_info_button);
        profileFriends = findViewById(R.id.profile_friends_button);
        editProfile = findViewById(R.id.profile_edit_button);
        username = findViewById(R.id.profile_username);
        firstName = findViewById(R.id.profile_first_name);
        lastName = findViewById(R.id.profile_last_name);
        phoneNumber = findViewById(R.id.profile_phone);
        emailAddress = findViewById(R.id.profile_email);
        password = findViewById(R.id.profile_password);
        numberOfTokens = findViewById(R.id.profile_points);

        profileInfo.setBackgroundColor(Color.parseColor("#8A2BE2"));

        currentUsersID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUsersID);
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child(currentUsersID);

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(MyProfileInfoActivity.this)
                        .asBitmap()
                        .load(uri.toString())
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                profileImage.setImageBitmap(resource);
                                return true;
                            }
                        })
                        .centerCrop()
                        .preload();
            }
        });

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    username.setText("@"+snapshot.getValue(User.class).getUsername());
                    firstName.setText(snapshot.getValue(User.class).getFirstName());
                    lastName.setText(snapshot.getValue(User.class).getLastName());
                    phoneNumber.setText(snapshot.getValue(User.class).getPhoneNumber());
                    emailAddress.setText(snapshot.getValue(User.class).getEmailAddress());
                    password.setText(snapshot.getValue(User.class).getPassword());
                    numberOfTokens.setText(String.valueOf(-1*snapshot.getValue(User.class).getNumberOfTokens()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyProfileInfoActivity.this, EditMyProfileInfoActivity.class));
            }
        });

        profileFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyProfileInfoActivity.this, MyProfileFriendsActivity.class));
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
        if(item.getItemId() == android.R.id.home)
        {
            if(backActivity.equals("RangListActivity"))
            {
                startActivity(new Intent(MyProfileInfoActivity.this, RangListActivity.class));
            }
            else if(backActivity.equals("FindFriendsActivity"))
            {
                startActivity(new Intent(MyProfileInfoActivity.this, FindFriendsActivity.class));
            }
            else
            {
                startActivity(new Intent(MyProfileInfoActivity.this, HomeActivity.class));
            }
        }
        else if(item.getItemId() == R.id.logOut_btn)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MyProfileInfoActivity.this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}