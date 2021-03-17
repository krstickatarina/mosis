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
import android.view.View;
import android.view.ViewGroup;

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

public class RangListActivity extends AppCompatActivity {

    FirebaseRecyclerAdapter<User, RangListHolder> adapter;
    FirebaseRecyclerOptions<User> options;
    RecyclerView recyclerView;
    Query query;
    StorageReference storageReference;
    String currentUsersID;
    DatabaseReference friendshipsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rang_list);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#EEB245"));
        actionBar.setBackgroundDrawable(colorDrawable);

        query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("numberOfTokens");
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
        recyclerView = findViewById(R.id.recyclerViewRangList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        currentUsersID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        friendshipsReference = FirebaseDatabase.getInstance().getReference().child("Friendships").child(currentUsersID);
        showUsersInOrder();
    }

    private void showUsersInOrder()
    {
        options = new FirebaseRecyclerOptions.Builder<User>().setQuery(query, User.class).build();
        adapter = new FirebaseRecyclerAdapter<User, RangListHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RangListHolder holder, int position, @NonNull User model) {
                holder.usersName.setText(model.getFirstName()+" "+model.getLastName());
                holder.usersUsername.setText(model.getUsername());
                holder.usersId.setText(getRef(position).getKey());
                holder.usersNumberOfTokens.setText(String.valueOf(-1*model.getNumberOfTokens()));
                String usersId = getRef(position).getKey();
                storageReference.child(usersId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(RangListActivity.this)
                                .asBitmap()
                                .load(uri.toString())
                                .listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        holder.usersProfilePhotoImageView.setImageBitmap(resource);
                                        return true;
                                    }
                                })
                                .centerCrop()
                                .preload();
                    }
                });

                holder.usersLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(currentUsersID.equals(getRef(position).getKey()))
                        {
                            Intent intent = new Intent(RangListActivity.this, MyProfileInfoActivity.class);
                            startActivity(intent);
                        }
                        else
                        {
                            friendshipsReference.orderByKey().equalTo(usersId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists())
                                    {
                                        Intent intent = new Intent(RangListActivity.this, FriendsProfileActivity.class);
                                        intent.putExtra("usersID", usersId);
                                        startActivity(intent);
                                    }
                                    else
                                    {
                                        Intent intent = new Intent(RangListActivity.this, UsersProfileActivity.class);
                                        intent.putExtra("usersID", usersId);
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
            public RangListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rang_list_one_user_layout, parent, false);
                return new RangListHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }
}