package com.originalstocksllc.himanshuraj.thenewschannel;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import static com.originalstocksllc.himanshuraj.thenewschannel.MainActivity.KEY_URL;


public class VerticalRecyclerAdapter extends RecyclerView.Adapter<VerticalRecyclerAdapter.VerticalViewHolder> {

    private Activity activity;
    private ArrayList<HashMap<String, String>> dataV;

    public VerticalRecyclerAdapter(Activity activity, ArrayList<HashMap<String, String>> data) {
        this.activity = activity;
        this.dataV = data;
    }

    @NonNull
    @Override
    public VerticalViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(activity).inflate(R.layout.horizontal_recycler_cont, viewGroup, false);
        return new VerticalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VerticalViewHolder verticalViewHolder, final int position) {
        HashMap<String, String> song = new HashMap<String, String>();
        song = dataV.get(position);

        try {
            verticalViewHolder.title.setText(song.get(MainActivity.KEY_TITLE));
            verticalViewHolder.time.setText(song.get(MainActivity.KEY_PUBLISHEDAT));
            verticalViewHolder.sdetails.setText(song.get(MainActivity.KEY_DESCRIPTION));
            Picasso.get()
                    .load(song.get(MainActivity.KEY_URLTOIMAGE))
                    .into(verticalViewHolder.galleryImage);
        } catch (Exception e) {
        }

        verticalViewHolder.newsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = verticalViewHolder.getAdapterPosition();

                final Intent detailsIntent = new Intent(activity, DetailsActivity.class);
                for (int i = 0; i <= pos; i++) {
                    detailsIntent.putExtra("url", dataV.get(pos).get(KEY_URL));

                }
                activity.startActivity(detailsIntent);
            }
        });

        verticalViewHolder.newsCard.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Intent shareLink = new Intent(Intent.ACTION_SEND);
                shareLink.setType("text/email");
                shareLink.putExtra(Intent.EXTRA_TEXT, dataV.get(+position).get(KEY_URL));
                activity.startActivity(Intent.createChooser(shareLink, "Share this News Card on:"));

                return true;
            }
        });

        verticalViewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareLink = new Intent(Intent.ACTION_SEND);
                shareLink.setType("text/email");
                shareLink.putExtra(Intent.EXTRA_TEXT, dataV.get(+position).get(KEY_URL));
                activity.startActivity(Intent.createChooser(shareLink, "Share this News Card on:"));
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataV.size();
    }

    public class VerticalViewHolder extends RecyclerView.ViewHolder {

        ImageView galleryImage;
        TextView title, sdetails, time;
        CardView newsCard;
        Button shareButton;


        public VerticalViewHolder(@NonNull View itemView) {
            super(itemView);

            newsCard = itemView.findViewById(R.id.newsCard);
            galleryImage = itemView.findViewById(R.id.galleryImage);
            title = itemView.findViewById(R.id.title);
            sdetails = itemView.findViewById(R.id.sdetails);
            time = itemView.findViewById(R.id.time);
            shareButton = itemView.findViewById(R.id.shareButton);
        }
    }
}
