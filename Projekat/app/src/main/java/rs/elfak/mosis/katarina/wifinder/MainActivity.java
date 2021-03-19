package rs.elfak.mosis.katarina.wifinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText emailAddress, password;
    private Button loginBtn;
    private TextView registerTextView;

    private FirebaseAuth fAuth=FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener fAuthStateListener;
    private String adminID = "npGXbgIrWsfyioefTknKcihx1Qc2";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = fAuth.getCurrentUser();
                if(mFirebaseUser != null)
                {
                    if(mFirebaseUser.getUid()!=adminID)
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    else
                        startActivity(new Intent(MainActivity.this, MapsActivity.class));
                }
            }
        };

        emailAddress = findViewById(R.id.login_email_address);
        password = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_btn);
        registerTextView = findViewById(R.id.login_gotoregistration_textview);

        loginBtn.setBackgroundColor(Color.parseColor("#EEB245"));

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddressString = emailAddress.getText().toString();
                String passwordString = password.getText().toString();
                fAuth.signInWithEmailAndPassword(emailAddressString, passwordString).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, "Login unsuccessful!", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            if(emailAddressString.equals("admin123@gmail.com"))
                            {
                                startActivity(new Intent(MainActivity.this, MapsActivity.class));
                            }
                            else
                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        }
                    }
                });
            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        fAuth.addAuthStateListener(fAuthStateListener);
    }
}