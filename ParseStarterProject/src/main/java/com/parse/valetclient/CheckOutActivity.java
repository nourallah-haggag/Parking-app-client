package com.parse.valetclient;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CheckOutActivity extends AppCompatActivity {

    // declare views
    TextView branchTxt;
    TextView codeTxt;
    TextView startTimeTxt;
    TextView endTimeTxt;
    TextView amountPayableTxt;

    // declare the values
    String branch , code , start , end , amount;

    // handle the back press


    @Override
    public void onBackPressed() {
        Toast.makeText(this, "please check out", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        // init the views
        branchTxt = (TextView)findViewById(R.id.branch_txt_c);
        codeTxt = (TextView)findViewById(R.id.code_txt_c);
        startTimeTxt = (TextView)findViewById(R.id.start_time_txt_c);
        endTimeTxt = (TextView)findViewById(R.id.end_time_txt_c);
        amountPayableTxt = (TextView)findViewById(R.id.amount_payable_txt_c);

        // init the values
        branch = getIntent().getStringExtra("branch");
        code = getIntent().getStringExtra("code");
        start = getIntent().getStringExtra("startTime");
        end = getIntent().getStringExtra("endTime");
        final ProgressDialog  progressDialog = new ProgressDialog(CheckOutActivity.this);
        progressDialog.setTitle("Loading...");
        progressDialog.setMessage("loading data");
        progressDialog.setCancelable(false);
        progressDialog.show();
        // get the branch pricing info
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Branch");
        parseQuery.whereEqualTo("name" , branch);
        parseQuery.setLimit(1);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, com.parse.ParseException e) {
                progressDialog.cancel();
                if( e==null && objects.size()!=0)
                {
                    for(ParseObject object : objects)
                    {
                        amount = object.getString("pricing");
                       /// Toast.makeText(CheckOutActivity.this, ""+amount, Toast.LENGTH_SHORT).show();
                        amountPayableTxt.setText(amount+" KWD");
                    }
                }
                else
                {
                    Toast.makeText(CheckOutActivity.this, "error", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CheckOutActivity.this , HomeActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });

        // calculate the amount
       /* try {
            Date d1 = new SimpleDateFormat("HH:mm:ss").parse(start);
            Date d2 = new SimpleDateFormat("HH:mm:ss").parse(end);
           long time = d2.getTime() - d1.getTime();
           double timeFinal =  Math.abs(time / 3600000);
            double amountD = Math.ceil(timeFinal)*5.0;

            if(amountD == 0.0)
            {
                amount = ""+5.0+" EGP";
                amountPayableTxt.setText(amount);
            }
            else
            {
                amount = ""+amountD+" EGP";
                amountPayableTxt.setText(amount);
            }
           ;

        } catch (ParseException e) {
            e.printStackTrace();
        }*/


        // set the views
        branchTxt.setText(branch);
        codeTxt.setText(code);
        startTimeTxt.setText(start);
        endTimeTxt.setText(end);

        ImageView freeCar = (ImageView)findViewById(R.id.imageView3);
        final Animation myAnim = AnimationUtils.loadAnimation(CheckOutActivity.this, R.anim.bounce);
        // Use bounce interpolator with amplitude 0.1 and frequency 15
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 15);
        myAnim.setInterpolator(interpolator);
        freeCar.startAnimation(myAnim);
    }

    public void checkOut(View view)
    {

        // define a progress bar
        final ProgressDialog progressDialog = new ProgressDialog(CheckOutActivity.this);
        progressDialog.setTitle("Saving");
        progressDialog.setMessage("Saving Transaction...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // save the transaction in parse server
        // define the parse object
        ParseObject transactionObject = new ParseObject("Trans");
        // save the data in the parse object
        transactionObject.put("branch" , branch);
        transactionObject.put("code" , code);
        transactionObject.put("start" , start);
        transactionObject.put("end" , end);
        transactionObject.put("amount" , amount);
        transactionObject.put("user" , ParseUser.getCurrentUser().getUsername());
        // log in the current day
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayformat = new SimpleDateFormat("dd/MM/yy");
        String day = dayformat.format(calendar.getTime());
        transactionObject.put("day" , day);

        transactionObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if( e == null)
                {
                    progressDialog.cancel();
                    Toast.makeText(CheckOutActivity.this, "Transaction successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext() , HomeActivity.class);
                    finish();
                    startActivity(intent);
                    // put the print details and send them to the print activity
                 /*   intent.putExtra("branch" , branch);
                    intent.putExtra("code" , code);
                    intent.putExtra("start" , start);
                    intent.putExtra("end" , end);
                    intent.putExtra("amount" , amount);
                    startActivity(intent);*/
                }
                else
                {
                    progressDialog.cancel();

                    Toast.makeText(CheckOutActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });




    }


}
