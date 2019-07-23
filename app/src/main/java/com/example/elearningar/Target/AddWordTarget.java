package com.example.elearningar.Target;

import android.app.Dialog;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.elearningar.Authentication.SignIn;
import com.example.elearningar.Module.KeyWordTarget;
import com.example.elearningar.R;
import com.example.elearningar.YouTube.YoutubeConfig;
import com.example.elearningar.YouTube.YoutubeSearchActivity;
import com.github.barteksc.pdfviewer.PDFView;
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

import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.io.File;
import java.util.List;

public class AddWordTarget extends YouTubeBaseActivity {

    private static final String TAG = "AddWordTarget";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word_target);

        setupFirebaseAuth();
        getInput();
    }

    //widgets
    private EditText keyWord, Bookname, Comment;
    private RelativeLayout fileupload;
    private Button Add;
    private ProgressBar progressBar;
    private PDFView pdfView;
    private ImageView backArawo, uploadedPhoto, cancel;
    private VideoView videoView;
    private YouTubePlayerView youTubePlayerView;
    private MediaController mediaController;


    private static final int RC_PHOTO_PICKER =  1;
    private static final int RC_PDF_PICKER =  2;
    private static final int RC_YoutubeSearchActivity = 3;


    private void getInput(){
        keyWord = findViewById(R.id.keyword);
        Bookname = findViewById(R.id.bookName);
        Comment = findViewById(R.id.title);
        fileupload = findViewById(R.id.RL);
        pdfView = findViewById(R.id.pdfView);
        Add = findViewById(R.id.AddBtn);
        progressBar = findViewById(R.id.progressBar2);
        backArawo = findViewById(R.id.backArraow);
        videoView = findViewById(R.id.videoPlay);
        youTubePlayerView = findViewById(R.id.player_view);

        uploadedPhoto = findViewById(R.id.uploadedImage);
        cancel = findViewById(R.id.canel);

        progressBar.setVisibility(View.INVISIBLE);

        backArawo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mediaController = new MediaController(AddWordTarget.this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        uploadedPhoto.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
        youTubePlayerView.setVisibility(View.GONE);
        pdfView.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileType = "none";
                FileUrl = null;

                uploadedPhoto.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                youTubePlayerView.setVisibility(View.GONE);
                pdfView.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);

            }
        });
        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( !keyWord.getText().toString().equals("")
                        && ! Bookname.getText().toString().equals("")
                        && !Comment.getText().toString().equals("")){

                    progressBar.setVisibility(View.VISIBLE);

                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
                    String TargetID = String.valueOf(myRef.push().getKey());

                    KeyWordTarget keyWordTarget = new KeyWordTarget(mAuth.getUid(), keyWord.getText().toString(), Bookname.getText().toString(),
                            Comment.getText().toString(), FileType);

                    myRef.child("users").child(mAuth.getUid()).child("Targets").child("Keyword").child(TargetID).setValue(TargetID);

                    myRef.child("KeywordTarget")
                            .child(TargetID)
                            .setValue(keyWordTarget);

                    if (FileType.equals("image")){
                        uploadkeywordImage(FileUrl, TargetID);
                    }else
                        uploadkeywordFile(FileUrl, TargetID);


                }else
                    Toast.makeText(AddWordTarget.this, "you must complete form", Toast.LENGTH_LONG).show();
            }
        });
        fileupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(AddWordTarget.this);
                dialog.setContentView(R.layout.dialog_file_type);

                final TextView image = dialog.findViewById(R.id.imageIV);
                final TextView video = dialog.findViewById(R.id.video);
                final TextView YTvideo = dialog.findViewById(R.id.YTvideo);
                final TextView pdf = dialog.findViewById(R.id.pdf);


                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        image.setBackground(getResources().getDrawable(R.drawable.grey_sqr));
                        video.setBackground(getResources().getDrawable(R.drawable.clear_sqr));
                        YTvideo.setBackground(getResources().getDrawable(R.drawable.clear_sqr));
                        pdf.setBackground(getResources().getDrawable(R.drawable.clear_sqr));

                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        startActivityForResult(intent, RC_PHOTO_PICKER);

                        dialog.dismiss();
                    }
                });

                video.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        image.setBackground(getResources().getDrawable(R.drawable.clear_sqr));
                        video.setBackground(getResources().getDrawable(R.drawable.grey_sqr));
                        YTvideo.setBackground(getResources().getDrawable(R.drawable.clear_sqr));
                        pdf.setBackground(getResources().getDrawable(R.drawable.clear_sqr));

                        new VideoPicker.Builder(AddWordTarget.this)
                                .mode(VideoPicker.Mode.GALLERY)
                                .directory(VideoPicker.Directory.DEFAULT)
                                .extension(VideoPicker.Extension.MP4)
                                .enableDebuggingMode(true)
                                .build();

                        dialog.dismiss();

                    }
                });

                pdf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        image.setBackground(getResources().getDrawable(R.drawable.clear_sqr));
                        video.setBackground(getResources().getDrawable(R.drawable.clear_sqr));
                        YTvideo.setBackground(getResources().getDrawable(R.drawable.clear_sqr));
                        pdf.setBackground(getResources().getDrawable(R.drawable.grey_sqr));

                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/pdf");
                        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        startActivityForResult(intent, RC_PDF_PICKER);

                        dialog.dismiss();

                    }
                });

                YTvideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult( new Intent(AddWordTarget.this, YoutubeSearchActivity.class), RC_YoutubeSearchActivity);
                        dialog.dismiss();

                    }
                });

                dialog.show();
            }
        });

    }

    private UploadTask uploadTask;
    public void uploadkeywordImage(Uri selectedImageUri, final String TargetID){

        // Get a reference to store file
        final StorageReference upRef =  FirebaseStorage.getInstance().getReference()
                .child("KeywordImageTarget")
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

                    myRef.child("KeywordTarget")
                            .child(TargetID)
                            .child("ImageUrl")
                            .setValue(downloadURL);

                    Toast.makeText(AddWordTarget.this, "added", Toast.LENGTH_LONG).show();
                    finish();

                } else {
                    // Handle failures
                    // ...
                }
            }
        });

    }

    public void uploadkeywordFile(Uri selectedUri, final String TargetID){

        // Get a reference to store file
        final StorageReference upRef =  FirebaseStorage.getInstance().getReference()
                .child("keywordFiles")
                .child(TargetID)
                .child(selectedUri.getLastPathSegment());

        // Upload file to Firebase Storage
        uploadTask = upRef.putFile(selectedUri);

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

                    myRef.child("KeywordTarget")
                            .child(TargetID)
                            .child("FileUrl")
                            .setValue(downloadURL);


                    Toast.makeText(AddWordTarget.this, "added", Toast.LENGTH_LONG).show();
                    finish();

                } else {
                    // Handle failures
                    // ...
                }
            }
        });

    }


    private Uri FileUrl;
    private String FileType = "none";
    private String YTvideoId;
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {


            FileUrl = data.getData();
            FileType = "image";

            cancel.setVisibility(View.VISIBLE);
            uploadedPhoto.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            //uploadedPhoto.setVisibility(View.GONE);
            pdfView.setVisibility(View.GONE);

            Picasso.get().load(FileUrl).into(uploadedPhoto);

        }

        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {

            List<String> mPaths =  data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
            FileUrl = (Uri.fromFile(new File(mPaths.get(0))));
            FileType = "video";

            cancel.setVisibility(View.VISIBLE);
            uploadedPhoto.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            youTubePlayerView.setVisibility(View.GONE);
            pdfView.setVisibility(View.GONE);


            MediaMetadataRetriever ret = new  MediaMetadataRetriever();
            Bitmap bmp = null;
            int videoHeight, videoWidth, videoSize;

            ret.setDataSource(AddWordTarget.this, FileUrl);
            bmp = ret.getFrameAtTime();
            videoHeight=bmp.getHeight();
            videoWidth=bmp.getWidth();
            videoSize = bmp.getAllocationByteCount();


            videoView.getLayoutParams().height= videoHeight;
            videoView.setVideoPath(mPaths.get(0));
            videoView.canPause();
            videoView.start();
        }

        if (requestCode == RC_PDF_PICKER && resultCode == RESULT_OK) {
            FileUrl = data.getData();
            FileType = "pdf";

            Log.e(TAG, "fileUrl " + FileUrl);

            cancel.setVisibility(View.VISIBLE);
            uploadedPhoto.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            youTubePlayerView.setVisibility(View.GONE);
            pdfView.setVisibility(View.VISIBLE);

            pdfView.fromUri(FileUrl).load();

        }

        if (requestCode == RC_YoutubeSearchActivity && resultCode == RESULT_OK) {
            YTvideoId = data.getStringExtra("VideoId");
            FileUrl = null;
            FileType = "youtube";

            cancel.setVisibility(View.VISIBLE);
            uploadedPhoto.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            youTubePlayerView.setVisibility(View.VISIBLE);
            pdfView.setVisibility(View.GONE);


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
                    Intent intent = new Intent(AddWordTarget.this, SignIn.class);
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
