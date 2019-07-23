package com.example.elearningar.YouTube;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.elearningar.Authentication.SignIn;
import com.example.elearningar.R;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class YoutubeSearchActivity extends AppCompatActivity implements YoutubePlayerFragment.OnFragmentInteractionListener {

    private static final String TAG = "YoutubeSearchActivity";

    private String SearchTxt;
    private String VideoId;

    private YoutubeAdapter youtubeAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_search);

        setupFirebaseAuth();
        initWidgets();

    }


    //widgets
    private EditText videourl, searchInput;
    private ImageView search, searchUrl , close, done, backArawo;
    private RelativeLayout playerlayout;
    private TextView loadMore;


    private void initWidgets(){
        videourl = findViewById(R.id.YTurl);
        searchInput = findViewById(R.id.input);
        search = findViewById(R.id.search);
        searchUrl = findViewById(R.id.searchUrl);
        close = findViewById(R.id.close);
        done = findViewById(R.id.done);
        playerlayout = findViewById(R.id.playerRL);
        loadMore = findViewById(R.id.loadmore);
        backArawo= findViewById(R.id.backArraow);
        loadMore.setVisibility(View.GONE);

        backArawo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        playerlayout.setVisibility(View.GONE);

        searchUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SearchTxt = SearchInput.removeSpaces(videourl.getText().toString());

                if (SearchTxt.equals("")){
                    Toast.makeText(YoutubeSearchActivity.this, "empty input", Toast.LENGTH_LONG).show();

                }else{
                    String id = SearchInput.getYouTubeId(SearchTxt);
                    if (id.equals("error")){
                        Toast.makeText(YoutubeSearchActivity.this, "error", Toast.LENGTH_LONG).show();
                    }else
                        OpenPlayer(id);
                }


            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchTxt = SearchInput.removeSpaces(searchInput.getText().toString());

                if (SearchTxt.equals("")){
                    Toast.makeText(YoutubeSearchActivity.this, "empty input", Toast.LENGTH_LONG).show();

                }else
                    RecyclerView();
            }
        });

    }

    public void OpenPlayer(String videoId){
        VideoId = videoId;
        playerlayout.setVisibility(View.VISIBLE);

        OpenplayerFragment(videoId);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("VideoId", VideoId);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void OpenplayerFragment(String id){

        YoutubePlayerFragment fragment = YoutubePlayerFragment.newInstance(id);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.addToBackStack(null);
        transaction.remove(fragment);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }


    private RecyclerView mRecyclerView;
    private ProgressDialog mProgressDialog;
    private Handler handler;

    private void RecyclerView(){

        mRecyclerView = (RecyclerView) findViewById(R.id.videoRV);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(YoutubeSearchActivity.this));
        handler = new Handler();

        mProgressDialog = new ProgressDialog(YoutubeSearchActivity.this);
        mProgressDialog.setMessage("Finding videos for "+ SearchTxt);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        searchOnYoutube(SearchTxt, false);

        loadMore.setVisibility(View.VISIBLE);
        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchOnYoutube(SearchTxt, true);
            }
        });

    }

    private List<VideoItem> searchResults;
    private int count = 1 ;
    //custom search method which takes argument as the keyword for which videos is to be searched
    private void searchOnYoutube(final String keywords, final boolean more){
        Log.e(TAG, "searchOnYoutube: " + "Start" );
        mProgressDialog.show();

        final LinearLayoutManager layoutManager = new LinearLayoutManager(YoutubeSearchActivity.this);
        mRecyclerView.setLayoutManager(layoutManager);
        //A thread that will execute the searching and inflating the RecyclerView as and when
        //results are found
        new Thread(){

            public void run(){

                YoutubeSearch yc = new YoutubeSearch(YoutubeSearchActivity.this);
                if (!more){
                    searchResults = yc.search(keywords, 0);
                    Log.e(TAG, "searchOnYoutube: " + "connect" );

                    // doing changes in the UI
                    handler.post(new Runnable(){

                        public void run(){
                            fillYoutubeVideos();
                            mProgressDialog.dismiss();
                        }
                    });
                }else {
                    final int i =5;
                    searchResults = yc.search(keywords, i * count);
                    Log.e(TAG, "searchOnYoutube: " + "connect" );

                    // doing changes in the UI
                    handler.post(new Runnable(){

                        public void run(){
                            fillYoutubeVideos();
                            layoutManager.scrollToPosition( i * count -6);
                            mProgressDialog.dismiss();

                        }
                    });

                    count ++;

                }


            }
            //starting the thread
        }.start();
    }

    //method for creating adapter and setting it to recycler view
    private void fillYoutubeVideos(){

        Log.e(TAG, "fillYoutubeVideos: " + "Start" );

        if (searchResults != null ){
            youtubeAdapter = new YoutubeAdapter(YoutubeSearchActivity.this,searchResults);
            mRecyclerView.setAdapter(youtubeAdapter);
            youtubeAdapter.notifyDataSetChanged();
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
                    Intent intent = new Intent(YoutubeSearchActivity.this, SignIn.class);
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
    public void onFragmentInteraction(Uri uri) {

    }
}
