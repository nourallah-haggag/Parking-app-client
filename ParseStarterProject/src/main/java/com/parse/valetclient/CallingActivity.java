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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class CallingActivity extends AppCompatActivity {

    // declare the views
    TextView cardCode;
    // declare the values
    String code;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        context = this;
        // declare the views
        cardCode = (TextView) findViewById(R.id.card_code_free);
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
        ParseQuery<ParseObject> cardQuery = ParseQuery.getQuery("Card");
        cardQuery.whereEqualTo("Code", code);
        cardQuery.setLimit(1);
        cardQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    progressDialog.cancel();
                    for (ParseObject object : objects) {
                        if (!(object.getString("status").equals("calling"))) {
                            // current and online status does not match
                          /*  AlertDialog.Builder builder = new AlertDialog.Builder(CallingActivity.this);
                            builder.setTitle("Card is no longer calling");
                            builder.setMessage("status updated by another employee");
                            builder.setPositiveButton("Return Home", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(CallingActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();*/
                            Toast.makeText(context, "status updated by another employee", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CallingActivity.this, HomeActivity.class);
                            finish();
                            startActivity(intent);
                        } else {
                            ImageView callCar = (ImageView) findViewById(R.id.call_car_image);
                            final Animation myAnim = AnimationUtils.loadAnimation(context, R.anim.bounce);
                            // Use bounce interpolator with amplitude 0.1 and frequency 15
                            MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 15);
                            myAnim.setInterpolator(interpolator);
                            callCar.startAnimation(myAnim);

                        }
                    }
                }

            }
        });


    }

    public void freeFunction(View view) {
        // check if offline status matches the online one
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Checking card status...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        ParseQuery<ParseObject> cardQuery = ParseQuery.getQuery("Card");
        cardQuery.whereEqualTo("Code", code);
        cardQuery.setLimit(1);
        cardQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    progressDialog.cancel();
                    for (ParseObject object : objects) {
                        if (!(object.getString("status").equals("calling"))) {
                            // current and online status does not match
                           /* AlertDialog.Builder builder = new AlertDialog.Builder(CallingActivity.this);
                            builder.setTitle("Card is no longer calling");
                            builder.setMessage("status updated by another employee");
                            builder.setPositiveButton("Return Home", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(CallingActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();*/
                            Toast.makeText(context, "status updated by another employee", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(CallingActivity.this, HomeActivity.class);
                            finish();
                            startActivity(intent);
                        } else {
                            final ProgressDialog progressDialog1 = new ProgressDialog(CallingActivity.this);
                            progressDialog1.setTitle("please wait");
                            progressDialog1.setMessage("freeing slot");
                            progressDialog1.setCancelable(false);
                            progressDialog1.show();
                            /// set the start time in the parse server
                            ParseQuery<ParseObject> cardQuery1 = ParseQuery.getQuery("Card");
                            cardQuery1.whereEqualTo("Code", code);
                            cardQuery1.setLimit(1);
                            cardQuery1.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objects, ParseException e) {
                                    if (e == null && objects.size() > 0) {
                                        //change the status and set the start time of the park
                                        for (final ParseObject object : objects) {
                                            String endTime;
                                            Calendar calendar = Calendar.getInstance();
                                            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                                            endTime = format.format(calendar.getTime());
                                            object.put("status", "free");
                                            object.put("endTime", endTime);
                                            progressDialog1.cancel();

                                            // save the updated values
                                        /*    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                            alert.setTitle("Free Slot ?");
                                            alert.setMessage("operation cannot be undone !");
                                            alert.setPositiveButton("free", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    progressDialog1.show();
                                                    object.saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            if (e == null) {
                                                                progressDialog1.cancel();
                                                                Toast.makeText(CallingActivity.this, "slot " + code + " freed , proceed to checkout", Toast.LENGTH_LONG).show();
                                                                Intent intent = new Intent(getApplicationContext(), CheckOutActivity.class);
                                                                String sTime, eTime, branch, code;
                                                                sTime = object.getString("startTime");
                                                                eTime = object.getString("endTime");
                                                                branch = object.getString("branch");
                                                                code = object.getString("Code");
                                                                intent.putExtra("branch", branch);
                                                                intent.putExtra("code", code);
                                                                intent.putExtra("startTime", sTime);
                                                                intent.putExtra("endTime", eTime);
                                                                startActivity(intent);
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                            alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });
                                            alert.show();*/
                                            progressDialog1.show();
                                            object.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        progressDialog1.cancel();
                                                        Toast.makeText(CallingActivity.this, "slot " + code + " freed , proceed to checkout", Toast.LENGTH_LONG).show();
                                                        Intent intent = new Intent(getApplicationContext(), CheckOutActivity.class);
                                                        finish();
                                                        String sTime, eTime, branch, code;
                                                        sTime = object.getString("startTime");
                                                        eTime = object.getString("endTime");
                                                        branch = object.getString("branch");
                                                        code = object.getString("Code");
                                                        intent.putExtra("branch", branch);
                                                        intent.putExtra("code", code);
                                                        intent.putExtra("startTime", sTime);
                                                        intent.putExtra("endTime", eTime);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            });

                        }
                    }
                }
            }
        });

    }

    public void parkFunction(View view) {
        // check if offline status matches the online one
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Checking card status...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        ParseQuery<ParseObject> cardQuery = ParseQuery.getQuery("Card");
        cardQuery.whereEqualTo("Code", code);
        cardQuery.setLimit(1);
        cardQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    progressDialog.cancel();
                    for (ParseObject object : objects) {
                        if (!(object.getString("status").equals("calling"))) {
                            // current and online status does not match
                           /* AlertDialog.Builder builder = new AlertDialog.Builder(CallingActivity.this);
                            builder.setTitle("Card is no longer calling");
                            builder.setMessage("status updated by another employee");
                            builder.setPositiveButton("Return Home", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(CallingActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();*/

                            Toast.makeText(context, "status updated by another employee", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(CallingActivity.this, HomeActivity.class);
                            finish();
                            startActivity(intent);
                        } else {
                            final ProgressDialog progressDialog = new ProgressDialog(CallingActivity.this);
                            progressDialog.setTitle("please wait");
                            progressDialog.setMessage("Parking");
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                            /// set the start time in the parse server
                            ParseQuery<ParseObject> cardQuery = ParseQuery.getQuery("Card");
                            cardQuery.whereEqualTo("Code", code);
                            cardQuery.setLimit(1);
                            cardQuery.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objects, ParseException e) {
                                    if (e == null && objects.size() > 0) {
                                        //change the status and set the start time of the park
                                        for (final ParseObject object : objects) {
                                            object.put("status", "parked");
                                            progressDialog.cancel();

                                            // save the updated values
                                          /*  AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                            alert.setTitle("Finish calling ?");
                                            alert.setMessage("operation cannot be undone !");
                                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    progressDialog.show();
                                                    object.saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            if (e == null) {
                                                                progressDialog.cancel();
                                                                Toast.makeText(CallingActivity.this, "slot " + code + " status changes to 'parked' ", Toast.LENGTH_LONG).show();
                                                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                                                startActivity(intent);
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                            alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });
                                            alert.show();*/
                                            progressDialog.show();
                                            object.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        progressDialog.cancel();
                                                        Toast.makeText(CallingActivity.this, "slot " + code + " status changes to 'parked' ", Toast.LENGTH_LONG).show();
                                                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                                        finish();
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            });

                        }
                    }
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
