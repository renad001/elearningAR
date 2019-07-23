package com.example.elearningar.Target;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.widget.VideoView;

import com.example.elearningar.Authentication.SignIn;
import com.example.elearningar.Module.Comment;
import com.example.elearningar.R;
import com.example.elearningar.Vuforia.VuforiaSamples.app.CloudRecognition.CloudReco;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CommentActivity extends YouTubeBaseActivity implements PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "CommentActivity";

    private String TargetID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent intent = getIntent();
        TargetID = intent.getStringExtra("TargetID");
        setupFirebaseAuth();
        setWidget();
        setRecyclerView();

    }

    private RecyclerView recyclerView;
    private CommentsRecyclerViewAdapter adapter;
    private ArrayList<Comment> Clist = new ArrayList<>() ;

    private ImageView Sort, SortType;
    private boolean isDateSort = true;
    private void setRecyclerView(){

        Sort = findViewById(R.id.sortt);
        SortType = findViewById(R.id.sortType);

        recyclerView = findViewById(R.id.CommentRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CommentsRecyclerViewAdapter(this, Clist , mAuth.getUid(), TargetID );
        recyclerView.setAdapter(adapter);
        fillList();


        SortType.setBackground(getResources().getDrawable(R.drawable.ic_today));

        Sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSort();
            }
        });

        SortType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSort();
            }
        });


    }

    private void changeSort(){
        isDateSort = ! isDateSort;

        if (isDateSort){
            SortType.setBackground(getResources().getDrawable(R.drawable.ic_today));
            Collections.sort(Clist, new Comparator<Comment>() {
                public int compare(Comment o1, Comment o2) {
                    return o2.getCommentDate().compareTo(o1.getCommentDate());
                }
            });

            adapter.notifyDataSetChanged();
        }else {
            SortType.setBackground(getResources().getDrawable(R.drawable.ic_red_heart));

            Collections.sort(Clist, new Comparator<Comment>() {
                public int compare(Comment o1, Comment o2) {
                    return o2.getLikes().compareTo(o1.getLikes());
                }
            });

            adapter.notifyDataSetChanged();
        }

    }





    private void fillList(){
        DatabaseReference CommentRef = FirebaseDatabase.getInstance().getReference().child("ImageTarget").child(TargetID).child("comment");

        CommentRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                setComment(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    String CommentID = dataSnapshot.getKey();

                    if (CommentID != null){

                        for (int i =0 ; i < Clist.size() ; i ++){
                            if ( Clist.get(i).getCommentId().equals(CommentID)){
                                Clist.set(i , setComment(dataSnapshot));
                                Clist.remove(Clist.size()-1);
                                adapter.notifyItemRangeChanged(i,Clist.size()-1);

                            }

                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Comment setComment(DataSnapshot dataSnapshot){

        final Comment comment = new Comment();

        comment.setCommentId(dataSnapshot.getKey());
        comment.setCommentUserId(dataSnapshot.child("commentUserId").getValue(String.class));
        comment.setCommentText(dataSnapshot.child("commentText").getValue(String.class));
        comment.setCommentDate(dataSnapshot.child("commentDate").getValue(String.class));
        comment.setAttachmentType(dataSnapshot.child("attachmentType").getValue(String.class));
        comment.setAttachmentUrl(dataSnapshot.child("attachmentUrl").getValue(String.class));
        comment.setLikes(String.valueOf(dataSnapshot.child("likes").getChildrenCount()));

        Clist.add(comment);
        adapter.notifyItemInserted(Clist.size());

        return comment;

    }

    public void setVideoView(final String VideoUrl , final CommentsRecyclerViewAdapter.ViewHolder holder){

        holder.progressBar.setVisibility(View.VISIBLE);

        StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(VideoUrl);
        String FileName = httpsReference.getName().substring(0, httpsReference.getName().indexOf(".mp4"));

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
                holder.progressBar.setVisibility(View.GONE);
                holder.videoView.setVideoPath(finalLocalFile.getPath());
                //holder.videoView.start();
                Log.e("adapter", String.valueOf(holder.videoView.isPlaying()) + finalLocalFile.getPath());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                holder.fail.setVisibility(View.VISIBLE);
            }
        });

        holder.fail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVideoView(VideoUrl, holder);
            }
        });
    }

    public void setYTVideo(final String VideoID , final CommentsRecyclerViewAdapter.ViewHolder holder){

        holder.youTubePlayerView.initialize(YoutubeConfig.getApiKey(),new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(VideoID);
                youTubePlayer.pause();

            }
            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
            }
        });
    }

    public void setPDFviewer(final String FileUrl){

        PDFRL.setVisibility(View.VISIBLE);

        StorageReference httpsReference = FirebaseStorage.getInstance().getReferenceFromUrl(FileUrl);
        //String FileName = httpsReference.getName().substring(0, httpsReference.getName().indexOf(".pdf"));

        File localFile = null;
        try {
            localFile = File.createTempFile("pdf", "pdf");

        } catch (IOException e) {
            e.printStackTrace();
        }

        final File finalLocalFile = localFile;
        httpsReference.getFile(finalLocalFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                pdfView.fromFile(finalLocalFile).load();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });


    }



    //widgets
    private ImageView backArrow, send, AttachPreView, AttachCancel, closePDf;
    private TextView ToolBarText, AttachName, AttachType;
    private EditText commentET;
    private RelativeLayout AttachmentLayout, PDFRL;
    private PDFView pdfView;

    private void setWidget(){
        backArrow = findViewById(R.id.backArraow);
        send = findViewById(R.id.send);
        ToolBarText = findViewById(R.id.title);
        commentET = findViewById(R.id.messageEditText);
        AttachmentLayout = findViewById(R.id.attachRL);
        AttachPreView = findViewById(R.id.attachpreview);
        AttachName = findViewById(R.id.attachName);
        AttachType = findViewById(R.id.attachType);
        AttachCancel = findViewById(R.id.attachCancel);
        pdfView = findViewById(R.id.pdfView);
        PDFRL = findViewById(R.id.pdfRL);
        closePDf = findViewById(R.id.close);

        AttachmentLayout.setVisibility(View.GONE);
        PDFRL.setVisibility(View.GONE);

        send.setBackground(getResources().getDrawable(R.drawable.ic_menu_send));

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ToolBarText.setText("Comments & Attachment");

        commentET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    comment();
                    Log.e(TAG, "  onTextChanged ");
                    send.setBackground(getResources().getDrawable(R.drawable.ic_menu_send_enable));
                } else {
                    Log.e(TAG, "  onTextChanged else");
                    send.setBackground(getResources().getDrawable(R.drawable.ic_menu_send));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        commentET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1000)});

        AttachCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileUrl = null;
                FileType = "none";
                YTvideoId = null;
                AttachmentLayout.setVisibility(View.GONE);
                send.setBackground(getResources().getDrawable(R.drawable.ic_menu_send));
                isAttach = false;
            }
        });

        closePDf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PDFRL.setVisibility(View.GONE);
            }
        });
    }

    private boolean isAttach = false;
    private void comment(){
        Log.e(TAG, "  comment");

        send.setBackground(getResources().getDrawable(R.drawable.ic_menu_send_enable));

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentString = commentET.getText().toString().trim();

                if (! FileType.equals("none")){
                    isAttach = true;
                }
                if (commentString.length() > 0 || isAttach){
                     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
                     sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
                     String date = sdf.format(new Date());

                     DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();

                     String CommentId = String.valueOf(myRef.push().getKey());

                     Comment mComment;
                     if(isAttach){

                         if( commentString.length() > 0 ){
                             mComment = new Comment(mAuth.getUid(), commentString, date,FileType);

                         }else {
                             mComment = new Comment(mAuth.getUid(), null, date,FileType);
                         }

                     } else
                         mComment = new Comment(mAuth.getUid(), commentString, date);

                     myRef.child("ImageTarget").child(TargetID).child("comment").child(CommentId).setValue(mComment);

                     if(isAttach){
                         if(FileType.startsWith("y")){
                             DatabaseReference myRef2 = FirebaseDatabase.getInstance().getReference();
                             myRef2.child("ImageTarget")
                                     .child(TargetID)
                                     .child("comment")
                                     .child(CommentId)
                                     .child("attachmentUrl").setValue(YTvideoId);
                         }else{
                             uploadFile(FileUrl, CommentId);
                         }
                     }


                     // Clear input box
                    AttachmentLayout.setVisibility(View.GONE);
                    isAttach = false;
                    FileUrl = null;
                    FileType = "none";
                    YTvideoId = null;
                    commentET.setText("");
                 }
            }
        });
    }


    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.attach_menu);
        popup.show();
    }

    private static final int RC_PHOTO_PICKER =  1;
    private static final int RC_PDF_PICKER =  2;
    private static final int RC_YoutubeSearchActivity = 3;

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.image:
                Intent imgintent = new Intent(Intent.ACTION_GET_CONTENT);
                imgintent.setType("image/*");
                imgintent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(imgintent, RC_PHOTO_PICKER);
                return true;

            case R.id.video:
                new VideoPicker.Builder(CommentActivity.this)
                        .mode(VideoPicker.Mode.GALLERY)
                        .directory(VideoPicker.Directory.DEFAULT)
                        .extension(VideoPicker.Extension.MP4)
                        .enableDebuggingMode(true)
                        .build();
                return true;

            case R.id.YTvideo:
                startActivityForResult( new Intent(CommentActivity.this, YoutubeSearchActivity.class), RC_YoutubeSearchActivity);
                return true;

            case R.id.pdf:
                Intent pdfintent = new Intent(Intent.ACTION_GET_CONTENT);
                pdfintent.setType("application/pdf");
                pdfintent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(pdfintent, RC_PDF_PICKER);
                return true;
            default:
                return false;
        }
    }

    private Uri FileUrl;
    private String FileType = "none";
    private String YTvideoId;
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {

            AttachmentLayout.setVisibility(View.VISIBLE);
            FileUrl = data.getData();
            FileType = "image";

            Picasso.get().load(FileUrl).into(AttachPreView);
            AttachType.setText(FileType);

            comment();
        }

        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {

            List<String> mPaths =  data.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH);
            AttachmentLayout.setVisibility(View.VISIBLE);
            FileUrl = (Uri.fromFile(new File(mPaths.get(0))));
            FileType = "video";

            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mPaths.get(0), MediaStore.Video.Thumbnails.MICRO_KIND);
            AttachPreView.setImageBitmap(bitmap);
            AttachType.setText(FileType);
            comment();
        }

        if (requestCode == RC_PDF_PICKER && resultCode == RESULT_OK) {
            AttachmentLayout.setVisibility(View.VISIBLE);
            FileUrl = data.getData();
            FileType = "pdf";

            AttachType.setText(FileType);
            AttachPreView.setImageDrawable(getResources().getDrawable(R.drawable.ic_file));
            AttachName.setText(FileUrl.getLastPathSegment());
            comment();
        }

        if (requestCode == RC_YoutubeSearchActivity && resultCode == RESULT_OK) {
            AttachmentLayout.setVisibility(View.VISIBLE);
            YTvideoId = data.getStringExtra("VideoId");
            FileUrl = null;
            FileType = "youtube";

            Uri thumbnail = Uri.parse("https://img.youtube.com/vi/"+YTvideoId+"/mqdefault.jpg");
            Picasso.get().load(thumbnail).into(AttachPreView);

            AttachType.setText(FileType);
            AttachName.setText("");
            comment();

        }

    }

    private UploadTask uploadTask;
    public void uploadFile(Uri selectedUri, final String CommentId){

        // Get a reference to store file
        final StorageReference upRef =  FirebaseStorage.getInstance().getReference()
                .child("ImageTarget")
                .child(TargetID)
                .child("Attachments")
                .child( CommentId + selectedUri.getLastPathSegment());

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

                    myRef.child("ImageTarget").child(TargetID).child("comment").child(CommentId).child("attachmentUrl")
                            .setValue(downloadURL);

                } else {
                    // Handle failures
                    // ...
                }
            }
        });

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
                    Intent intent = new Intent(CommentActivity.this, SignIn.class);
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
