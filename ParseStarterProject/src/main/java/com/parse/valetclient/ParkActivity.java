package com.parse.valetclient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ParkActivity extends AppCompatActivity {

 // declare the views
    TextView cardCode;
    // declare the values
    String code;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park);
        context = this;

        // declare the views
        cardCode =  (TextView)findViewById(R.id.card_code_park);
        // init the values
        code = getIntent().getStringExtra("cardCode");
        // set the text view
        cardCode.setText(code);

        // check if offline status matches the online one
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Checking card status...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        ParseQuery<ParseObject> cardQuery =ParseQuery.getQuery("Card");
        cardQuery.whereEqualTo("Code" , code);
        cardQuery.setLimit(1);
        cardQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null && objects.size()>0)
                {
                    progressDialog.cancel();
                    for(ParseObject object : objects)
                    {
                        if(!(object.getString("status").equals("free")))
                        {
                            // current and online status does not match
                           /* AlertDialog.Builder builder = new AlertDialog.Builder(ParkActivity.this);
                            builder.setTitle("Card is no longer free");
                            builder.setMessage("status updated by another employee");
                            builder.setPositiveButton("Return Home", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(ParkActivity.this , HomeActivity.class);
                                    startActivity(intent);
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();*/
                            Toast.makeText(context, "status updated by another employee", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(ParkActivity.this , HomeActivity.class);
                            finish();
                            startActivity(intent);
                        }
                        else
                        {
                            ImageView imageView = (ImageView)findViewById(R.id.imageView);
                            final Animation myAnim = AnimationUtils.loadAnimation(context, R.anim.bounce);
                            // Use bounce interpolator with amplitude 0.1 and frequency 15
                            MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 15);
                            myAnim.setInterpolator(interpolator);
                            imageView.startAnimation(myAnim);
                        }
                    }
                }

            }
        });


    }

    public void parkFunction(View view)
    {
        final ProgressDialog progressDialog = new ProgressDialog(ParkActivity.this);
        progressDialog.setTitle("please wait");
        progressDialog.setMessage("parking");
        progressDialog.setCancelable(false);
        progressDialog.show();
        /// set the start time in the parse server
        ParseQuery<ParseObject> cardQuery = ParseQuery.getQuery("Card");
        cardQuery.whereEqualTo("Code" , code);
        cardQuery.setLimit(1);
        cardQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if( e == null && objects.size()>0)
                {
                    //change the status and set the start time of the park
                    for(final ParseObject object : objects)
                    {
                        if (!(object.getString("status").equals("free"))) {
                            // current and online status does not match
                          /*  AlertDialog.Builder builder = new AlertDialog.Builder(ParkActivity.this);
                            builder.setTitle("Card is no longer free");
                            builder.setMessage("status updated by another employee");
                            builder.setPositiveButton("Return Home", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(ParkActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();*/
                            Toast.makeText(context, "status updated by another employee", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(ParkActivity.this, HomeActivity.class);
                            finish();
                            startActivity(intent);


                        }
                        else
                        {
                            final String startTime;
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                            startTime = format.format(calendar.getTime());
                            object.put("status" , "parked");
                            object.put("startTime" ,startTime );

                            progressDialog.cancel();

                            // save the updated values
                           /* AlertDialog.Builder alert = new AlertDialog.Builder(context);
                            alert.setTitle("Park car ?");
                            alert.setMessage("charges will be applied , operation cannot be undone !");
                            alert.setPositiveButton("park", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    progressDialog.show();
                                    object.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if(e == null)
                                            {
                                                progressDialog.cancel();
                                                Toast.makeText(ParkActivity.this, "car successfully parked in slot "+code, Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(getApplicationContext() , HomeActivity.class);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                }
                            });
                            alert.setNegativeButton("canel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            alert.show();*/
                            progressDialog.show();
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null)
                                    {
                                        progressDialog.cancel();
                                        Toast.makeText(ParkActivity.this, "car successfully parked in slot "+code, Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(getApplicationContext() , PrintActivity.class);
                                        finish();
                                        intent.putExtra("branch" , object.getString("branch"));
                                        intent.putExtra("code" , code);
                                        intent.putExtra("start" , startTime);
                                        intent.putExtra("amount" , object.getString("amount"));
                                        intent.putExtra("status" , object.getString("status"));
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                        }

                        }
                        else
                {
                    progressDialog.cancel();
                    Toast.makeText(context, "failed, please check internet connection and try again !", Toast.LENGTH_SHORT).show();
                }

                }
            });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        finish();
        startActivity(intent);
    }
}
