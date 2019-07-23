package com.example.elearningar.Navigate;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.elearningar.Authentication.SignIn;
import com.example.elearningar.Module.User;
import com.example.elearningar.R;
import com.example.elearningar.Target.AddImageTarget;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {
    private static final String TAG = "Profile";

    private CircleImageView userImage;
    Button btn_signout;
    FirebaseAuth mAuth;
    TextView text_name, text_lastname, text_username, text_email;
    int Image_Request_Code = 7;
    Uri filePathUri;
    StorageReference storageReference;
    ProgressDialog progressDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupFirebaseAuth();

        text_name = findViewById(R.id.current_name);
        text_lastname =  findViewById(R.id.current_lastname);
        text_username =  findViewById(R.id.current_username);
        text_email =  findViewById(R.id.current_email);
        userImage = findViewById(R.id.user_image);
        progressDialog = new ProgressDialog(Profile.this);
        storageReference = FirebaseStorage.getInstance().getReference();


        DatabaseReference user = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid());

        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    User user =  dataSnapshot.getValue(User.class);

                    Picasso.get().load(user.getImageURL()).into(userImage);
                    text_name.setText(user.getFirstName());
                    text_lastname.setText(user.getLastName());
                    text_username.setText(user.getUserName());
                    text_email.setText(user.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Creating intent.
                Intent intent = new Intent();

                // Setting intent type as image to select image from phone storage.
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Please Select Image"), Image_Request_Code);


            }
        });
        btn_signout = (Button) findViewById(R.id.btn_signout);
        btn_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Profile.this, SignIn.class));
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Image_Request_Code && resultCode == RESULT_OK && data != null && data.getData() != null) {

            filePathUri = data.getData();

            UploadImageFileToFirebaseStorage();

        }
    }

    private UploadTask uploadTask;
    public void UploadImageFileToFirebaseStorage() {

        // Checking whether FilePathUri Is empty or not.
        if (filePathUri != null) {

            // Setting progressDialog Title.
            progressDialog.setTitle("Image is Uploading...");

            // Showing progressDialog.
            progressDialog.show();


            // Get a reference to store file
            final StorageReference upRef =  FirebaseStorage.getInstance().getReference()
                    .child("users")
                    .child(mAuth.getUid())
                    .child("profilePhoto")
                    .child(filePathUri.getLastPathSegment());

            // Upload file to Firebase Storage
            uploadTask = upRef.putFile(filePathUri);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Hiding the progressDialog.
                    progressDialog.dismiss();

                    // Showing exception erro message.
                    Toast.makeText(Profile.this, exception.getMessage(), Toast.LENGTH_LONG).show();

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });

            // When the image has successfully uploaded, we get its download URL
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return upRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String downloadURL = String.valueOf(downloadUri);
                        // set download uri on profile

                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

                        myRef.child("users")
                                .child(mAuth.getUid())
                                .child("imageURL")
                                .setValue(downloadURL);

                        progressDialog.dismiss();


                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });

        }
        else {

            Toast.makeText(Profile.this, "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show();

        }
    }


    //firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(Profile.this, SignIn.class);
                    startActivity(intent);
                    finish();
                }
                // ...
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

}
