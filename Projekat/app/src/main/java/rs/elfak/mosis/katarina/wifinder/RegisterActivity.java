package rs.elfak.mosis.katarina.wifinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.security.Permission;

public class RegisterActivity extends AppCompatActivity {

    private EditText firstName, lastName, username, emailAddress, password, phoneNumber;
    private Button registerBtn;
    private TextView loginTextView;
    private ImageView uploadImage;

    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fAuthStateListener;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private Uri filePath;
    private Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#EEB245"));
        actionBar.setBackgroundDrawable(colorDrawable);

        firstName = findViewById(R.id.register_firstname);
        lastName = findViewById(R.id.register_lastname);
        username = findViewById(R.id.register_username);
        emailAddress = findViewById(R.id.register_emailaddress);
        password = findViewById(R.id.register_password);
        phoneNumber = findViewById(R.id.register_phonenumber);

        registerBtn = findViewById(R.id.register_btn);
        loginTextView = findViewById(R.id.register_gotologin_textview);
        uploadImage = findViewById(R.id.register_imageView);

        registerBtn.setBackgroundColor(Color.parseColor("#EEB245"));

        fAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstNameString = firstName.getText().toString();
                String lastNameString = lastName.getText().toString();
                String usernameString = username.getText().toString();
                String emailAddressString = emailAddress.getText().toString();
                String passwordString = password.getText().toString();
                String phoneNumberString = phoneNumber.getText().toString();

                if(!firstNameString.isEmpty() && !lastNameString.isEmpty() && !usernameString.isEmpty() && !emailAddressString.isEmpty() &&
                    !passwordString.isEmpty() && !phoneNumberString.isEmpty())
                {
                    fAuth.createUserWithEmailAndPassword(emailAddressString, passwordString).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful())
                            {
                                Toast.makeText(RegisterActivity.this, "Registration isn't completed successfully!", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String id = fAuth.getCurrentUser().getUid();
                                databaseReference.child(id).setValue(new User(firstNameString, lastNameString, usernameString, emailAddressString, passwordString, phoneNumberString, 0, "Pocetnik"));
                                storeImage(id);
                                Toast.makeText(RegisterActivity.this, "You have registered successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                finish();
                            }
                        }
                    });
                }
                else
                {
                    if(firstNameString.isEmpty())
                    {
                        firstName.setError("First name is requested!");
                        firstName.requestFocus();
                    }
                    if(lastNameString.isEmpty())
                    {
                        lastName.setError("Last name is requested!");
                        lastName.requestFocus();
                    }
                    if(usernameString.isEmpty())
                    {
                        username.setError("Username is requested!");
                        username.requestFocus();
                    }
                    if(phoneNumberString.isEmpty())
                    {
                        phoneNumber.setError("Phone number is requested!");
                        phoneNumber.requestFocus();
                    }
                    if(emailAddressString.isEmpty())
                    {
                        emailAddress.setError("Email address is requested!");
                        emailAddress.requestFocus();
                    }
                    if(passwordString.isEmpty())
                    {
                        password.setError("Password is requested!");
                        password.requestFocus();
                    }
                    Toast.makeText(RegisterActivity.this, "Please enter all requested fields!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            photo = (Bitmap) data.getExtras().get("data");
            uploadImage.setImageBitmap(photo);
        }
    }

    private void storeImage(String id)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] b = stream.toByteArray();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child(id);
        storageReference.putBytes(b).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        final Uri downloadUri = uri;
                    }
                });
                Toast.makeText(RegisterActivity.this, "Image successfully uploaded!", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this,"Image upload failed!",Toast.LENGTH_LONG).show();
            }
        });
    }


}