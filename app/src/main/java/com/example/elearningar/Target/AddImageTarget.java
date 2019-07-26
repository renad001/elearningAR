package com.example.elearningar.Target;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.elearningar.Authentication.SignIn;
import com.example.elearningar.Module.ImageTarget;
import com.example.elearningar.R;
import com.example.elearningar.Vuforia.VuforiaSamples.app.CloudRecognition.VWS.PostNewTarget;
import com.example.elearningar.YouTube.YoutubeConfig;
import com.example.elearningar.YouTube.YoutubeSearchActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import net.alhazmy13.mediapicker.Image.ImagePicker;
import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.io.File;
import java.util.List;

public class AddImageTarget extends YouTubeBaseActivity {

    private static final String TAG = "AddImageTarget";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image_target);

        setupFirebaseAuth();
        getInPut();

    }

    //widgets
    private RelativeLayout uploadPhotoL, uploadVidL, uploadYTvid;
    private ImageView backArawo, uploadePhoto , videouploadIV;
    private VideoView videoView;
    private EditText BookName,Title, Description;
    private Button Add;
    private ProgressBar progressBar;
    private YouTubePlayerView youTubePlayerView;
    private MediaController mediaController;


    private static final int RC_YoutubeSearchActivity = 2;

    String TargetID;
    private void getInPut(){
        backArawo = findViewById(R.id.backArraow);
        uploadPhotoL = findViewById(R.id.photoRL);
        uploadePhoto = findViewById(R.id.uploadedImage);
        uploadVidL = findViewById(R.id.vidRL);
        uploadYTvid = findViewById(R.id.YTRL);
        videouploadIV = findViewById(R.id.vidIM);
        videoView = findViewById(R.id.videoPlay);
        BookName = findViewById(R.id.bookname);
        Title = findViewById(R.id.title);
        Description = findViewById(R.id.description);
        Add = findViewById(R.id.addTarget);
        progressBar = findViewById(R.id.progressBar2);
        youTubePlayerView = findViewById(R.id.player_view);

        mediaController = new MediaController(AddImageTarget.this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        videoView.setVisibility(View.INVISIBLE);
        youTubePlayerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        backArawo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        uploadPhotoL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePicker();
            }
        });

        uploadePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePicker();
            }
        });

        uploadVidL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoPicker();
            }
        });

        videouploadIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoPicker();
            }
        });

        uploadYTvid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult( new Intent(AddImageTarget.this, YoutubeSearchActivity.class), RC_YoutubeSearchActivity);
            }
        });

        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e(TAG, "add" + photoUrl + " " + videoUrl);

                if (videoUrl != null || ! YTvideoId.equals("") ){
                    if( photoUrl != null
                            && ! BookName.getText().toString().equals("")
                            && ! Title.getText().toString().equals("")
                            && ! Description.getText().toString().equals("")){

                        progressBar.setVisibility(View.VISIBLE);

                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                        TargetID = String.valueOf(myRef.push().getKey());


                        ImageTarget imageTarget = new ImageTarget(mAuth.getUid(), BookName.getText().toString(),
                                Title.getText().toString(), Description.getText().toString() );

                        myRef.child("users").child(mAuth.getUid()).child("Targets").child("Image").child(TargetID).setValue(TargetID);

                        myRef.child("ImageTarget")
                                .child(TargetID)
                                .setValue(imageTarget);

                        if (videoUrl == null){
                            myRef.child("ImageTarget")
                                    .child(TargetID)
                                    .child("videoUrl")
                                    .setValue(YTvideoId);

                        }
                        upload(TargetID, photoUrl, videoUrl);

                    }else{
                        Toast.makeText(AddImageTarget.this, "you must complete form", Toast.LENGTH_LONG).show();

                    }
                }else{
                    Toast.makeText(AddImageTarget.this, "you must complete form", Toast.LENGTH_LONG).show();

                }
            }
        });

    }

    private void videoPicker(){
        new VideoPicker.Builder(AddImageTarget.this)
                .mode(VideoPicker.Mode.GALLERY)
                .directory(VideoPicker.Directory.DEFAULT)
                .extension(VideoPicker.Extension.MP4)
                .enableDebuggingMode(true)
                .build();
    }

    private void imagePicker(){
        new ImagePicker.Builder(AddImageTarget.this)
                .mode(ImagePicker.Mode.CAMERA_AND_GALLERY)
                .directory(ImagePicker.Directory.DEFAULT)
                .extension(ImagePicker.Extension.PNG)
                .extension(ImagePicker.Extension.JPG)
                .allowMultipleImages(false)
                .enableDebuggingMode(true)
                .build();
    }

    private Uri photoUrl;
    private Uri videoUrl;
    private String YTvideoId= "";
    private String targetPath;

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH);
            targetPath = mPaths.get(0);
            photoUrl = (Uri.fromFile(new File(mPaths.get(0))));
            Picasso.get().load(photoUrl).into(uploadePhoto);

        }

        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {

            List<String> mPaths = data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
            videoUrl = (Uri.fromFile(new File(mPaths.get(0))));
            YTvideoId = "";

            videoView.setVisibility(View.VISIBLE);
            videouploadIV.setVisibility(View.INVISIBLE);
            youTubePlayerView.setVisibility(View.GONE);

            MediaMetadataRetriever ret = new MediaMetadataRetriever();
            Bitmap bmp = null;

            int videoHeight, videoWidth, videoSize;

            ret.setDataSource(mPaths.get(0));
            bmp = ret.getFrameAtTime();
            videoHeight = bmp.getHeight();
            videoWidth = bmp.getWidth();
            videoSize = bmp.getAllocationByteCount();

            Log.e(TAG, "h w s " + videoHeight + videoWidth + videoSize);

            //videoView.setLayoutParams(ConstraintLayout.LayoutParams());

            videoView.setVideoPath(mPaths.get(0));
            videoView.start();
        }

        if (requestCode == RC_YoutubeSearchActivity && resultCode == RESULT_OK) {
            YTvideoId = data.getStringExtra("VideoId");
            videoUrl = null;

            videoView.setVisibility(View.GONE);
            videouploadIV.setVisibility(View.INVISIBLE);
            youTubePlayerView.setVisibility(View.VISIBLE);

            youTubePlayerView.initialize(YoutubeConfig.getApiKey(),new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                    Log.e(TAG, "onInitializationSuccess");
                    youTubePlayer.loadVideo(YTvideoId);
                }
                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                }
            });

        }
    }


    private UploadTask uploadTask;
    String videoDownloadUrl;
    public void upload(final String TargetID, Uri selectedImageUri, Uri selectedvideoUri){


        // Get a reference to store file
        final StorageReference upRef =  FirebaseStorage.getInstance().getReference()
                .child("ImageTarget")
                .child(TargetID)
                .child(selectedImageUri.getLastPathSegment());

        // Upload file to Firebase Storage
        uploadTask = upRef.putFile(selectedImageUri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads

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

                    myRef.child("ImageTarget")
                            .child(TargetID)
                            .child("ImagaUrl")
                            .setValue(downloadURL);

                } else {
                    // Handle failures
                    // ...
                }
            }
        });


        if(selectedvideoUri != null){
            // Get a reference to store file
            final StorageReference upRef2 =  FirebaseStorage.getInstance().getReference()
                    .child("ImageTarget")
                    .child(TargetID)
                    .child(selectedvideoUri.getLastPathSegment());

            // Upload file to Firebase Storage
            uploadTask = upRef2.putFile(selectedvideoUri);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });

            // When the image has successfully uploaded, we get its download URL
            Task<Uri> urlTask2 = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return upRef2.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        videoDownloadUrl = String.valueOf(downloadUri);

                        // set download uri on profile
                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

                        myRef.child("ImageTarget")
                                .child(TargetID)
                                .child("videoUrl")
                                .setValue(videoDownloadUrl);

                        thread.start();
                        Toast.makeText(AddImageTarget.this, "added", Toast.LENGTH_LONG).show();

                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
        }else {
            thread.start();
            Toast.makeText(AddImageTarget.this, "added", Toast.LENGTH_LONG).show();
        }



    }

    Thread thread = new Thread(new Runnable(){

        @Override
        public void run() {
            try {
                PostNewTarget p = new PostNewTarget();

                p.setTargetName(TargetID);
                p.setImageLocation(targetPath);
                p.setVideoURL(videoDownloadUrl);
                p.postTargetThenPollStatus();

                finish();

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }



    });

    //firebase
    private FirebaseAuth mAuth;
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
                    Intent intent = new Intent(AddImageTarget.this, SignIn.class);
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
}
