package com.example.elearningar.Vuforia.VuforiaSamples.app.CloudRecognition;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.elearningar.Module.Comment;
import com.example.elearningar.R;
import com.example.elearningar.Target.CommentActivity;
import com.example.elearningar.Target.ImageTargetActivity;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;


public class SimpleCommentsRecyclerViewAdapter extends RecyclerView.Adapter<SimpleCommentsRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "SimpleCommentsRecyclerViewAdapter";

    //vars
    private ArrayList<Comment> CommentsList = new ArrayList<>();
    private String userID, targetID;
    private Context mContext;
    private boolean liked = false;
    private boolean favorited = false;

    public SimpleCommentsRecyclerViewAdapter(Context context, ArrayList<Comment> coments, String uID, String TargetId) {
        CommentsList = coments;
        userID = uID;
        targetID = TargetId;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_simple_comment_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return CommentsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView userName, comment, Nlike, Time;
        public CircleImageView profilePhoto;
        public ImageView like,favorite ;
        public RelativeLayout relativeLayout;



        public ViewHolder(View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.textView);
            comment = itemView.findViewById(R.id.textView1);
            Nlike = itemView.findViewById(R.id.likes);
            Time = itemView.findViewById(R.id.textView2);
            profilePhoto = itemView.findViewById(R.id.profile_photo);
            like = itemView.findViewById(R.id.heart);
            favorite = itemView.findViewById(R.id.favorite);
            relativeLayout =itemView.findViewById(R.id.comment);
        }
    }


    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {


        Log.e(TAG, CommentsList.get(position).toString());
        getProfile(CommentsList.get(position).getCommentUserId(), holder);

        if (CommentsList.get(position).getCommentText() != null){
            holder.comment.setText(CommentsList.get(position).getCommentText());
        }else
            holder.comment.setVisibility(View.INVISIBLE);


        holder.Nlike.setText(String.valueOf(CommentsList.get(position).getLikes()));

        holder.like.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_empty_heart));


        final String commentId = CommentsList.get(position).getCommentId();
        isLiked(commentId, holder );

        final DatabaseReference likeclickref = FirebaseDatabase.getInstance().getReference().child("ImageTarget").child(targetID).child("comment").child(commentId).child("likes");

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (liked){
                    likeclickref.child(userID).removeValue();
                    holder.like.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_empty_heart));
                    liked= false;
                }else {
                    likeclickref.child(userID).setValue("true");
                    holder.like.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_red_heart));
                    liked = true;
                }
            }
        });


        final DatabaseReference favoriteclickref = FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("favorite").child(targetID).child("comment");

        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favorited){
                    favoriteclickref.child(commentId).removeValue();
                    holder.favorite.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_bookmark));
                    favorited= false;
                }else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
                    sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));
                    String date = sdf.format(new Date());

                    DatabaseReference favoriteclickref2 = favoriteclickref;
                    DatabaseReference favoriteclickref3 = favoriteclickref;
                    DatabaseReference favoriteclickref4 = favoriteclickref;

                    favoriteclickref.child(commentId).child("FavoriteType").setValue("ImageTarget");
                    boolean offline = false;
                    favoriteclickref2.child(commentId).child("offline").setValue(offline);
                    favoriteclickref3.child(commentId).child("dateAdded").setValue(date);
                    favoriteclickref4.child(commentId).child("CommentId").setValue(CommentsList.get(position).getCommentId());
                    holder.favorite.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_bookmarked));
                    favorited = true;
                }
            }
        });


        time(CommentsList.get(position).getCommentDate(), holder);

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onSingleTapUp, setOnClickListener " );
                ((CloudReco) mContext).close();
                Intent intent = new Intent(mContext, ImageTargetActivity.class);
                intent.putExtra("TargetID", targetID );
                mContext.startActivity(intent);
            }
        });

        holder.profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }


    private void time( String date, ViewHolder holder ){

        if (date != null){
            // set date created

            String timeDiff = "";
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
            sdf.setTimeZone(TimeZone.getTimeZone("Canada/Pacific"));//google 'android list of timezones'
            Date today = c.getTime();
            sdf.format(today);
            Date timestamp;
            final String photoTimestamp = date;
            try{
                timestamp = sdf.parse(photoTimestamp);
                timeDiff = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
            }catch (ParseException e){
                timeDiff = "0";
            }

            if(!timeDiff.equals("0")){
                holder.Time.setText(timeDiff + " DAYS AGO");
            }else
                holder.Time.setText("Today");
        }

    }


    public void getProfile(String userID , final ViewHolder holder ){

        DatabaseReference RR = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        RR.addValueEventListener(new ValueEventListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Uri uri = Uri.parse(dataSnapshot.child("imageURL").getValue(String.class));
                Picasso.get().load(uri).into(holder.profilePhoto);

                Log.e(TAG, uri.toString());
                holder.userName.setText(dataSnapshot.child("userName").getValue(String.class));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void isLiked(String CommentId, final ViewHolder holder){

        DatabaseReference likeref = FirebaseDatabase.getInstance().getReference().child("ImageTarget").child(targetID).child("comment").child(CommentId).child("likes");

        likeref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(userID).exists()){
                   liked = true;
                    holder.like.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_red_heart));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
