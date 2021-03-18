package rs.elfak.mosis.katarina.wifinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditMyProfileInfoActivity extends AppCompatActivity {

    EditText newUsername, newFirstName, newLastName, newPhoneNumber, email, password;
    Button save, cancel;
    String currentUsersID;
    DatabaseReference usersReference;
    int numberOfTokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_profile_info);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newUsername = findViewById(R.id.edit_username);
        newFirstName = findViewById(R.id.edit_firstname);
        newLastName = findViewById(R.id.edit_lastname);
        newPhoneNumber = findViewById(R.id.edit_phone);
        email = findViewById(R.id.edit_email);
        password = findViewById(R.id.edit_password);
        save = findViewById(R.id.edit_profile_finished_button);
        cancel = findViewById(R.id.edit_profile_cancel_button);

        email.setEnabled(false);
        email.setFocusable(false);
        password.setEnabled(false);
        password.setFocusable(false);

        currentUsersID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUsersID);

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    newUsername.setText(snapshot.getValue(User.class).getUsername());
                    newFirstName.setText(snapshot.getValue(User.class).getFirstName());
                    newLastName.setText(snapshot.getValue(User.class).getLastName());
                    newPhoneNumber.setText(snapshot.getValue(User.class).getPhoneNumber());
                    email.setText(snapshot.getValue(User.class).getEmailAddress());
                    password.setText(snapshot.getValue(User.class).getPassword());
                    numberOfTokens = snapshot.getValue(User.class).getNumberOfTokens();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditMyProfileInfoActivity.this, MyProfileInfoActivity.class));
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User(newFirstName.getText().toString(), newLastName.getText().toString(), newUsername.getText().toString(),
                                    email.getText().toString(), password.getText().toString(), newPhoneNumber.getText().toString(),
                                    numberOfTokens, "Pocetnik");
                usersReference.setValue(user);
                Toast.makeText(EditMyProfileInfoActivity.this, "Changes successfully saved!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EditMyProfileInfoActivity.this, MyProfileInfoActivity.class));
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
            startActivity(new Intent(EditMyProfileInfoActivity.this, MyProfileInfoActivity.class));
        }
        else if(item.getItemId() == R.id.logOut_btn)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(EditMyProfileInfoActivity.this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}