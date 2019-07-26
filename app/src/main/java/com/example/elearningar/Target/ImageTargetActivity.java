package com.example.elearningar.Target;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.elearningar.Authentication.SignIn;
import com.example.elearningar.MainActivity;
import com.example.elearningar.Module.ImageTarget;
import com.example.elearningar.Module.User;
import com.example.elearningar.R;
import com.example.elearningar.YouTube.YoutubeConfig;
import com.example.elearningar.YouTube.YoutubeSearchActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ImageTargetActivity extends YouTubeBaseActivity {
    private static final String TAG = "ImageTargetActivity";

    private String TargetID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_target);

        Intent intent = getIntent();
        TargetID = intent.getStringExtra("TargetID");
        setupFirebaseAuth();
        fillWidget();

    }


    //widgets
    private VideoView videoView;
    private YouTubePlayerView youTubePlayerView;
    private ImageView like, favorite, profilePhoto;
    private TextView username, BookName,Title, Description, loadAll, likeNum;
    private ProgressDialog mProgressDialog;
    private MediaController mediaController;

    private boolean liked = false;
    private boolean favorited = false;

    private void fillWidget(){
        youTubePlayerView = findViewById(R.id.player_view);
        videoView = findViewById(R.id.videoPlay);
        BookName = findViewById(R.id.bookName);
        Title = findViewById(R.id.Title);
        Description = findViewById(R.id.description);
        username = findViewById(R.id.username);
        profilePhoto = findViewById(R.id.profilephoto);
        like = findViewById(R.id.like);
        favorite = findViewById(R.id.favorate);
        loadAll = findViewById(R.id.loadAll);
        likeNum = findViewById(R.id.likenum);

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference likeref = FirebaseDatabase.getInstance().getReference().child("ImageTarget").child(TargetID).child("likes");
                if(liked){
                    likeref.child(mAuth.getUid()).removeValue();
                    liked = false;
                    like.setBackground(getResources().getDrawable(R.drawable.ic_like));
                }else {
                    likeref.child(mAuth.getUid()).setValue("true");
                    liked = true;
                    like.setBackground(getResources().getDrawable(R.drawable.ic_liked));
                }
            }
        });

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference favref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()).child("favorite");
                if(favorited){
                    favref.child(TargetID).removeValue();
                    favorited = false;
                    favorite.setBackground(getResources().getDrawable(R.drawable.ic_bookmark));
                }else {

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
                    sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
                    String date = sdf.format(new Date());

                    DatabaseReference favref2 = favref;
                    DatabaseReference favref3 = favref;

                    favref.child(TargetID).child("FavoriteType").setValue("ImageTarget");

                    boolean offline = false;
                    favref2.child(TargetID).child("offline").setValue(offline);
                    favref3.child(TargetID).child("dateAdded").setValue(date);


                    favorited = true;
                    favorite.setBackground(getResources().getDrawable(R.drawable.ic_bookmarked));
                }
             }
        });

        loadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImageTargetActivity.this, CommentActivity.class);
                intent.putExtra("TargetID", TargetID );
                startActivity(intent);
            }
        });

        mediaController = new MediaController(ImageTargetActivity.this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);


        mProgressDialog = new ProgressDialog(ImageTargetActivity.this);
        mProgressDialog.setMessage("video downloading... ");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();

        DatabaseReference target = FirebaseDatabase.getInstance().getReference().child("ImageTarget").child(TargetID);
        target.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    final ImageTarget imageTarget =new ImageTarget() ;

                    imageTarget.setBookName(dataSnapshot.child("bookName").getValue(String.class));
                    imageTarget.setCreatorID(dataSnapshot.child("creatorID").getValue(String.class));
                    imageTarget.setDescription(dataSnapshot.child("description").getValue(String.class));
                    imageTarget.setTitle(dataSnapshot.child("title").getValue(String.class));
                    imageTarget.setVideoUrl(dataSnapshot.child("videoUrl").getValue(String.class));
                    imageTarget.setLikes(String.valueOf(dataSnapshot.child("likes").getChildrenCount()));

                    BookName.setText("Book Name : " +imageTarget.getBookName());
                    Title.setText("Title : " + imageTarget.getTitle());
                    Description.setText(imageTarget.getDescription());
                    likeNum.setText(imageTarget.getLikes());

                    if (dataSnapshot.child("likes").hasChild(mAuth.getUid())){
                        liked = true;
                        like.setBackground(getResources().getDrawable(R.drawable.ic_liked));
                    } else {
                        like.setBackground(getResources().getDrawable(R.drawable.ic_like));
                    }

                    setVideoView(imageTarget.getVideoUrl());

                    if(imageTarget.getVideoUrl().startsWith("https://firebasestorage")){
                        videoView.setVisibility(View.VISIBLE);
                        youTubePlayerView.setVisibility(View.GONE);


                    }else {
                        videoView.setVisibility(View.GONE);
                        youTubePlayerView.setVisibility(View.VISIBLE);

                        youTubePlayerView.initialize(YoutubeConfig.getApiKey(),new YouTubePlayer.OnInitializedListener() {
                            @Override
                            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                                Log.e(TAG, "onInitializationSuccess");
                                youTubePlayer.loadVideo(imageTarget.getVideoUrl());
                            }
                            @Override
                            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                            }
                        });

                    }


                    DatabaseReference Creator = FirebaseDatabase.getInstance().getReference().child("users").child(imageTarget.getCreatorID());
                    Creator.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                User user = dataSnapshot.getValue(User.class);
                                Uri uri = Uri.parse(user.getImageURL());
                                Picasso.get().load(uri).into(profilePhoto);
                                username.setText(user.getUserName());
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        final DatabaseReference favoriteref = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()).child("favorite");
        favoriteref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(TargetID)) {
                    favorited = true;
                    favorite.setBackground(getResources().getDrawable(R.drawable.ic_bookmarked));
                } else {
                    favorite.setBackground(getResources().getDrawable(R.drawable.ic_bookmark));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    boolean once = true;
    private void setVideoView(String VideoUrl){
        if (once){
            StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(VideoUrl);
            Log.e(TAG, "StorageReference" + httpsReference.getPath());
            String FileName = httpsReference.getName().substring(0, httpsReference.getName().indexOf(".mp4"));
            Log.e(TAG, "StorageReference FileName  " + FileName);

            File localFile = null;
            try {
                localFile = File.createTempFile(FileName, "mp4");

            } catch (IOException e) {
                e.printStackTrace();
            }

            final File finalLocalFile = localFile;
            httpsReference.getFile(finalLocalFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    mProgressDialog.dismiss();
                    videoView.setVideoPath(finalLocalFile.getPath());
                    videoView.start();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG, "video download Fail");
                }
            });

            once = false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        videoView.pause();
        videoView.setVideoURI(null);
        finish();
    }

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
                    Intent intent = new Intent(ImageTargetActivity.this, SignIn.class);
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
