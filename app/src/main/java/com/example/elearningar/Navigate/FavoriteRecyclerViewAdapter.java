package com.example.elearningar.Navigate;

import android.content.Context;
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
import com.example.elearningar.Module.Favorite;
import com.example.elearningar.R;
import com.example.elearningar.Target.CommentActivity;
import com.example.elearningar.Target.CommentsRecyclerViewAdapter;
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


public class FavoriteRecyclerViewAdapter extends RecyclerView.Adapter<FavoriteRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "CommentsRecyclerViewAdapter";

    //vars
    private ArrayList<Favorite> FavoriteList = new ArrayList<>();
    private String userID;
    private Context mContext;
    private boolean liked = false;

    public FavoriteRecyclerViewAdapter(Context context, ArrayList<Favorite> favorites, String uID) {
        FavoriteList = favorites;
        userID = uID;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_favorite_list, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return FavoriteList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView userName, TV1, TV2,offline, Nlike, FileName;
        public CircleImageView profilePhoto;
        public ImageView like, attachimage, fail ;
        public VideoView videoView;
        public YouTubePlayerView youTubePlayerView;
        public MediaController mediaController;
        public CardView fileCard;
        public RelativeLayout Attachment;
        public ProgressBar progressBar;



        public ViewHolder(View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.textView);
            TV1 = itemView.findViewById(R.id.bookname);
            TV2 = itemView.findViewById(R.id.Title);
            offline = itemView.findViewById(R.id.offline);
            Nlike = itemView.findViewById(R.id.likes);
            profilePhoto = itemView.findViewById(R.id.profile_photo);
            like = itemView.findViewById(R.id.heart);
            FileName = itemView.findViewById(R.id.filename);
            attachimage = itemView.findViewById(R.id.imageAttach);
            videoView = itemView.findViewById(R.id.videoPlay);
            youTubePlayerView = itemView.findViewById(R.id.player_view);
            fileCard = itemView.findViewById(R.id.fileCard);
            Attachment = itemView.findViewById(R.id.RLAttachment);
            progressBar = itemView.findViewById(R.id.progressBar);
            fail = itemView.findViewById(R.id.failll);

            mediaController = new MediaController(mContext);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);


        }
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        Log.e("Adapterrrr ", FavoriteList.get(position).toString());

        holder.Attachment.setVisibility(View.GONE);
        holder.progressBar.setVisibility(View.GONE);
        holder.fail.setVisibility(View.GONE);

        holder.like.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_empty_heart));

        String AttachType = null;
        String AttachURl = null;

        if(FavoriteList.get(position).getFavoriteType().startsWith("I")){

            if ( FavoriteList.get(position).getComment() == null){

                getProfile(FavoriteList.get(position).getImageTarget().getCreatorID(), holder);

                holder.TV1.setText(FavoriteList.get(position).getImageTarget().getBookName());
                holder.TV2.setText(FavoriteList.get(position).getImageTarget().getTitle());
                holder.Nlike.setText(String.valueOf(FavoriteList.get(position).getImageTarget().getLikes()));

                isLiked(FavoriteList.get(position).getImageTarget().getTargetID(), "imageTarget", null, holder );


                if (FavoriteList.get(position).getImageTarget().getVideoUrl().startsWith("https://firebasestorage")){
                    AttachType = "video";
                }else
                    AttachType = "youtube";

                AttachURl = FavoriteList.get(position).getImageTarget().getVideoUrl();
            }else {

                getProfile(FavoriteList.get(position).getComment().getCommentUserId(), holder);

                if (FavoriteList.get(position).getComment().getCommentText() !=null){
                    holder.TV1.setText(FavoriteList.get(position).getComment().getCommentText());
                    holder.TV2.setVisibility(View.GONE);
                }else {
                    holder.TV1.setVisibility(View.GONE);
                    holder.TV2.setVisibility(View.GONE);
                }

                if(FavoriteList.get(position).getComment().getAttachmentType() != null){
                    AttachType = FavoriteList.get(position).getComment().getAttachmentType();
                    AttachURl = FavoriteList.get(position).getComment().getAttachmentUrl();
                }

                holder.Nlike.setText(String.valueOf(FavoriteList.get(position).getComment().getLikes()));
                isLiked(FavoriteList.get(position).getTargetID(), "imageTarget", FavoriteList.get(position).getComment().getCommentId(), holder );

            }


        }


        final String URL = AttachURl;

        if(AttachType != null){

            holder.Attachment.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
            holder.fileCard.setVisibility(View.GONE);
            holder.youTubePlayerView.setVisibility(View.GONE);
            holder.attachimage.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.GONE);


            if (AttachURl!= null){

                if(AttachType.startsWith("i")){
                    holder.attachimage.setVisibility(View.VISIBLE);
                    Uri uri1 = Uri.parse(URL);
                    Picasso.get().load(uri1).into(holder.attachimage);

                }else if(AttachType.startsWith("v")){
                    holder.videoView.setVisibility(View.VISIBLE);
                    ((FavoriteActivity) mContext).setVideoView(URL, holder);

                }else if(AttachType.startsWith("y")){
                    holder.youTubePlayerView.setVisibility(View.VISIBLE);
                    ((FavoriteActivity) mContext).setYTVideo(URL, holder);

                }else if(AttachType.startsWith("p")){
                    holder.fileCard.setVisibility(View.VISIBLE);
                    holder.FileName.setText(URL);

                }
            }else {
                holder.progressBar.setVisibility(View.VISIBLE);
            }

        }


        holder.fileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FavoriteActivity) mContext).setPDFviewer(URL);
            }
        });

        holder.offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //((FavoriteActivity) mContext).setPDFviewer(URL);
            }
        });


    }


    public void isLiked(String TargetID, String TargetType, String CommentID, final ViewHolder holder){
        DatabaseReference likeref = FirebaseDatabase.getInstance().getReference();

        if (TargetType.startsWith("i")){
            likeref = likeref.child("ImageTarget").child(TargetID);

            if(CommentID != null){
                likeref = likeref.child("comment").child(CommentID).child("likes");

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

            }else {
                likeref = likeref.child("likes");

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


    }

    public void getProfile(String userID , final FavoriteRecyclerViewAdapter.ViewHolder holder ){

        DatabaseReference RR = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        RR.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Uri uri = Uri.parse(dataSnapshot.child("imageURL").getValue(String.class));
                Picasso.get().load(uri).into(holder.profilePhoto);

                holder.userName.setText(dataSnapshot.child("userName").getValue(String.class));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
