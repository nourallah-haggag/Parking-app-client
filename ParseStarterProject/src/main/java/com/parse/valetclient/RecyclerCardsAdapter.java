package com.parse.valetclient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RecyclerCardsAdapter extends RecyclerView.Adapter<RecyclerCardsAdapter.CradsHolder> {

    Context context;
    List<CardModel> cardsList;

    //This method will filter the list
    //here we are passing the filtered data
    //and assigning it to the list with notifydatasetchanged method
    public void filterList(ArrayList<CardModel> filterdNames) {
        this.cardsList = filterdNames;
        notifyDataSetChanged();
    }

    // constructor
    public RecyclerCardsAdapter(Context context , List<CardModel> cardsList)
    {
        this.context = context;
        this.cardsList = cardsList;
    }
    @Override
    public CradsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row_cards , parent ,  false);
        CradsHolder holder = new CradsHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(CradsHolder holder, int position) {
        final CardModel card = cardsList.get(position);
        holder.idCard.setText(card.code);
        holder.statusCard.setText(card.status);

        if(card.status.equals("parked"))
        {
           // holder.cardView.setBackgroundColor(Color.RED);


                holder.statusImage.setBackgroundResource(R.drawable.parkingred);


        }
        else if(card.status.equals("free"))
        {
            //holder.cardView.setBackgroundColor(Color.parseColor("#F38222"));
            holder.statusImage.setBackgroundResource(R.drawable.open);
        }
        else {
            holder.statusImage.setBackgroundResource(R.drawable.key);
        }

        // go to the card toggle activity
        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Intent intent = new Intent(context , CardToggleActivity.class);
                // pass the info
                intent.putExtra("cardCode" , card.code);
                intent.putExtra("cardStatus" , card.status);
                context.startActivity(intent);
                */
               if(card.status.equals("free"))
               {
                   // go to the park activity
                   Intent intent = new Intent(context , ParkActivity.class);
                   ((HomeActivity)context).finish();
                   intent.putExtra("cardCode" , card.code);
                   intent.putExtra("cardStatus" , card.status);

                   context.startActivity(intent);

               }
               else if(card.status.equals("parked"))
               {
                   // go to the free activity
                   Intent intent = new Intent(context , FreeActivity.class);
                   ((HomeActivity)context).finish();
                   intent.putExtra("cardCode" , card.code);
                   intent.putExtra("cardStatus" , card.status);
                   context.startActivity(intent);
               }
               else
               {
                   // go to the caling activity
                   Intent intent = new Intent(context , CallingActivity.class);
                   ((HomeActivity)context).finish();
                   intent.putExtra("cardCode" , card.code);
                   intent.putExtra("cardStatus" , card.status);
                   context.startActivity(intent);

               }
            }
        });




    }

    @Override
    public int getItemCount() {
        return cardsList.size();
    }

    public class CradsHolder extends RecyclerView.ViewHolder
    {

        // declare the views
        TextView idCard;
        TextView statusCard;
        CardView cardView;
        ImageView statusImage;
        //TextView edit;
        View v ;

        public CradsHolder(View itemView) {
            super(itemView);


            // init the views
            v = itemView;
            idCard = (TextView)itemView.findViewById(R.id.ID_card);
            statusCard = (TextView)itemView.findViewById(R.id.status_card);
            cardView = (CardView)itemView.findViewById(R.id.card_layout);
            statusImage = (ImageView)itemView.findViewById(R.id.image_status);
            //edit = (TextView)itemView.findViewById(R.id.edit_toggle_card);
        }
    }
}
