package com.example.elearningar.Navigate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.elearningar.Authentication.SignIn;
import com.example.elearningar.Module.Comment;
import com.example.elearningar.Module.Favorite;
import com.example.elearningar.Module.ImageTarget;
import com.example.elearningar.R;
import com.example.elearningar.Target.CommentActivity;
import com.example.elearningar.Target.CommentsRecyclerViewAdapter;
import com.example.elearningar.YouTube.YoutubeConfig;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FavoriteActivity extends YouTubeBaseActivity {

    private static final String TAG = "FavoriteActivity";

    private ImageView backArrow, closePDf;
    private RelativeLayout PDFRL;
    private PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        backArrow = findViewById(R.id.backArraow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupFirebaseAuth();
        setRecyclerView();

        PDFRL = findViewById(R.id.pdfRL);
        PDFRL.setVisibility(View.GONE);
        pdfView = findViewById(R.id.pdfView);
        closePDf = findViewById(R.id.close);

        closePDf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PDFRL.setVisibility(View.GONE);
            }
        });
    }

    private RecyclerView recyclerView;
    private FavoriteRecyclerViewAdapter adapter;
    private ArrayList<Favorite> Flist = new ArrayList<>() ;

    private void setRecyclerView(){

        recyclerView = findViewById(R.id.FavoriteRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new FavoriteRecyclerViewAdapter(this, Flist , mAuth.getUid() );
        recyclerView.setAdapter(adapter);
        fillList();


    }


    private void fillList(){
        DatabaseReference FavRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()).child("favorite");

        FavRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){

                    final String TargetID = dataSnapshot.getKey();
                    final DatabaseReference TargetRef = FirebaseDatabase.getInstance().getReference().child("ImageTarget").child(dataSnapshot.getKey());

                    if (dataSnapshot.child("FavoriteType").exists()){

                        final Favorite favorite = new Favorite();
                        favorite.setTargetID(TargetID);

                        favorite.setFavoriteType(dataSnapshot.child("FavoriteType").getValue(String.class));
                        favorite.setOffline(dataSnapshot.child("offline").getValue(boolean.class));
                        favorite.setDateAdded(dataSnapshot.child("dateAdded").getValue(String.class));

                        TargetRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                ImageTarget imageTarget = new ImageTarget();

                                imageTarget.setTargetID(dataSnapshot.getKey());
                                imageTarget.setBookName(dataSnapshot.child("bookName").getValue(String.class));
                                imageTarget.setCreatorID(dataSnapshot.child("creatorID").getValue(String.class));
                                imageTarget.setDescription(dataSnapshot.child("description").getValue(String.class));
                                imageTarget.setTitle(dataSnapshot.child("title").getValue(String.class));
                                imageTarget.setVideoUrl(dataSnapshot.child("videoUrl").getValue(String.class));
                                imageTarget.setLikes(String.valueOf(dataSnapshot.child("likes").getChildrenCount()));

                                favorite.setImageTarget(imageTarget);
                                Flist.add(favorite);

                                Collections.sort(Flist, new Comparator<Favorite>() {
                                    public int compare(Favorite o1, Favorite o2) {
                                        return o2.getDateAdded().compareTo(o1.getDateAdded());
                                    }
                                });

                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }


                    if (dataSnapshot.child("comment").exists()){

                        DatabaseReference commFavRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()).child("favorite").child(TargetID).child("comment");

                        commFavRef.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                final Favorite favorite = new Favorite();
                                favorite.setTargetID(TargetID);

                                favorite.setFavoriteType(dataSnapshot.child("FavoriteType").getValue(String.class));
                                favorite.setOffline(dataSnapshot.child("offline").getValue(boolean.class));
                                favorite.setDateAdded(dataSnapshot.child("dateAdded").getValue(String.class));

                                DatabaseReference comment = FirebaseDatabase.getInstance().getReference().child("ImageTarget").child(TargetID).child("comment").child(dataSnapshot.getKey());

                                comment.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Comment comment = new Comment();

                                        comment.setCommentId(dataSnapshot.getKey());
                                        comment.setCommentUserId(dataSnapshot.child("commentUserId").getValue(String.class));
                                        comment.setCommentText(dataSnapshot.child("commentText").getValue(String.class));
                                        comment.setCommentDate(dataSnapshot.child("commentDate").getValue(String.class));
                                        comment.setAttachmentType(dataSnapshot.child("attachmentType").getValue(String.class));
                                        comment.setAttachmentUrl(dataSnapshot.child("attachmentUrl").getValue(String.class));
                                        comment.setLikes(String.valueOf(dataSnapshot.child("likes").getChildrenCount()));

                                        favorite.setComment(comment);


                                        Flist.add(favorite);

                                        Collections.sort(Flist, new Comparator<Favorite>() {
                                            public int compare(Favorite o1, Favorite o2) {
                                                return o2.getDateAdded().compareTo(o1.getDateAdded());
                                            }
                                        });

                                        adapter.notifyDataSetChanged();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
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


    public void setVideoView(final String VideoUrl , final FavoriteRecyclerViewAdapter.ViewHolder holder){

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

    public void setYTVideo(final String VideoID , final FavoriteRecyclerViewAdapter.ViewHolder holder){

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
                    Intent intent = new Intent(FavoriteActivity.this, SignIn.class);
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
