package com.parse.valetclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class CardToggleActivity extends AppCompatActivity {

    // declare the views
    TextView codeTxt;
    TextView statusTxt;
    TextView startTimeTxt;
    TextView endTimeTxt;

    Button checkOut;
    Button toggleButton;

    // values
    String code;
    String status;
    String startTime;
    String endTime;


    // set the start and end time
    public void setTimes()
    {
        ParseQuery<ParseObject> cardQuery = ParseQuery.getQuery("Card");
        cardQuery.whereEqualTo("Code" , code);
        cardQuery.setLimit(1);
        cardQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if( e == null && objects.size()>0)
                {
                    for(ParseObject object : objects)
                    {
                        String sTime , eTime;
                        sTime = object.getString("startTime");
                        eTime = object.getString("endTime");


                            startTimeTxt.setText("start time: "+sTime);





                            endTimeTxt.setText("end time: "+eTime);



                    }
                }
            }
        });

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_toggle);

        // init values
        code = getIntent().getStringExtra("cardCode");
        status = getIntent().getStringExtra("cardStatus");

        // init the views
        codeTxt = (TextView)findViewById(R.id.card_code_txt_toggle);
        checkOut = (Button)findViewById(R.id.checkOutButton);
        toggleButton = (Button)findViewById(R.id.toggleButton);
        statusTxt = (TextView)findViewById(R.id.card_status_toggle);
        checkOut.setVisibility(View.INVISIBLE);
        startTimeTxt = (TextView)findViewById(R.id.start_time_txt);
        endTimeTxt = (TextView)findViewById(R.id.end_time_txt);



        // set the default views
        codeTxt.setText("code: "+code);
        statusTxt.setText(status);

        // set the toggle and check out buttons
        if(status.equals("free"))
        {

            toggleButton.setText("Park");
        }
        else
        {
            //checkOut.setVisibility(View.VISIBLE);
            toggleButton.setText("free");
        }

        // toggle button function
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(toggleButton.getText().toString().equals("Park"))
                {



                    // start the timer and chnage the status
                    // get the specified card obeject from the parse server to modify
                    ParseQuery<ParseObject> cardQuery = ParseQuery.getQuery("Card");
                    cardQuery.whereEqualTo("Code" , code);
                    cardQuery.setLimit(1);
                    cardQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if( e == null && objects.size()>0)
                            {
                                toggleButton.setText("free");
                                statusTxt.setText("parked");
                                for(ParseObject object : objects)
                                {
                                    object.put("status" , "parked");
                                    // put the start date
                                    Calendar calendar = Calendar.getInstance();
                                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                                    startTime = format.format(calendar.getTime());
                                    object.put("startTime" , startTime);
                                    object.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if( e == null)
                                            {
                                                Toast.makeText(CardToggleActivity.this, "operation successful", Toast.LENGTH_SHORT).show();
                                                setTimes();
                                            }

                                        }

                                    });
                                }

                            }
                        }
                    });
                }
                else  if(toggleButton.getText().toString().equals("free"))
                {

                    ParseQuery<ParseObject> cardQuery = ParseQuery.getQuery("Card");
                    cardQuery.whereEqualTo("Code" , code);
                    cardQuery.setLimit(1);
                    cardQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if(e == null && objects.size()>0)
                            {
                                toggleButton.setText("Park");
                                statusTxt.setText("free");
                                for(ParseObject object : objects)
                                {
                                    object.put("status" , "free");
                                    // put the end date
                                    Calendar calendar = Calendar.getInstance();
                                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                                    endTime = format.format(calendar.getTime());
                                    object.put("endTime" , endTime);
                                    object.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if( e == null)
                                            {
                                                Toast.makeText(CardToggleActivity.this, "operation successful", Toast.LENGTH_SHORT).show();
                                                // erase start and end time
                                                toggleButton.setVisibility(View.INVISIBLE);
                                                setTimes();

                                            }
                                        }
                                    });

                                }
                                checkOut.setVisibility(View.VISIBLE);
                                setTimes();
                            }
                        }
                    });

                }
            }
        });
        setTimes();
    }
}
