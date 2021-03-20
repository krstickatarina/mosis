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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class FindFriendsActivity extends AppCompatActivity {

    FirebaseRecyclerAdapter<User, FindFriendsHolder> adapter;
    FirebaseRecyclerOptions<User> options;
    RecyclerView recyclerView;
    Query query;
    DatabaseReference frienshipsReference;
    StorageReference storageReference;
    String currentUsersID;
    EditText editTextNameOfAPerson;
    ImageView searchPersonImageView;
    String personsUserNameString = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextNameOfAPerson = findViewById(R.id.findFriends_searchFriend_editText);
        searchPersonImageView = findViewById(R.id.findFriends_searchFriend_imageButton);

        currentUsersID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        frienshipsReference = FirebaseDatabase.getInstance().getReference().child("Friendships").child(currentUsersID);
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
        recyclerView = findViewById(R.id.recyclerViewFindFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        LoadUsersThatHaveWantedUsername();

        searchPersonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextNameOfAPerson.getText().toString().isEmpty())
                {
                    personsUserNameString = "";
                }
                else
                {
                    personsUserNameString = editTextNameOfAPerson.getText().toString();
                }
                LoadUsersThatHaveWantedUsername();
            }
        });
    }

    private void LoadUsersThatHaveWantedUsername()
    {
        query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("username")
                .startAt(personsUserNameString)
                .endAt(personsUserNameString+"\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<User>().setQuery(query, User.class).build();
        adapter = new FirebaseRecyclerAdapter<User, FindFriendsHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsHolder holder, int position, @NonNull User model) {
                holder.usersName.setText(model.getFirstName()+" "+model.getLastName());
                holder.usersUsername.setText(model.getUsername());
                storageReference.child(getRef(position).getKey()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(FindFriendsActivity.this)
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

                holder.oneUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(currentUsersID.equals(getRef(position).getKey()))
                        {
                            Intent intent = new Intent(FindFriendsActivity.this, MyProfileInfoActivity.class);
                            intent.putExtra("backActivity", "FindFriendsActivity");
                            startActivity(intent);
                        }
                        else
                        {
                            frienshipsReference.orderByKey().equalTo(getRef(position).getKey()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists())
                                    {
                                        Intent intent = new Intent(FindFriendsActivity.this, FriendsProfileActivity.class);
                                        intent.putExtra("usersID", getRef(position).getKey());
                                        intent.putExtra("backActivity", "FindFriendsActivity");
                                        startActivity(intent);
                                    }
                                    else
                                    {
                                        Intent intent = new Intent(FindFriendsActivity.this, UsersProfileActivity.class);
                                        intent.putExtra("usersID", getRef(position).getKey());
                                        intent.putExtra("backActivity", "FindFriendsActivity");
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends_one_user_layout, parent, false);
                return new FindFriendsHolder(view);
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
            startActivity(new Intent(FindFriendsActivity.this, HomeActivity.class));
        }
        else if(item.getItemId() == R.id.logOut_btn)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(FindFriendsActivity.this, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}