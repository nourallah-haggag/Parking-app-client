package com.parse.valetclient;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    ProgressDialog progressDialog;


    // list components
    RecyclerView recyclerView ;
    RecyclerCardsAdapter adapter;
    List<CardModel> cardsList;
    String name , branch;

    // views
    TextView nameView;
    TextView branchView;
    FloatingActionButton floatingActionButton;
    FloatingActionButton qrfloating;
    FloatingActionButton printFloating;
    ImageView errorImage;
    TextView errorText;

    int noOfTrans =0;
    int amountCollected =0;
    View v;

    EditText searchTxt;

    // get the regards when returning to the activity
    @Override
    protected void onPostResume() {
        super.onPostResume();
        getCards();
    }


    // exit the app on back press


    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit App");
        builder.setMessage("Are you sure you want to exit ?");
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // actually exit the app

               /* Intent intent = new Intent(HomeActivity.this, MainActivity.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("shouldFinish", true);
                startActivity(intent);*/

              HomeActivity.super.onBackPressed();



            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //  set the progress
         progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setMessage("loading data ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // init the cards componnets
        recyclerView = (RecyclerView) findViewById(R.id.recycler_cards);
        cardsList = new ArrayList<>();
        adapter = new RecyclerCardsAdapter(this , cardsList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this , 3 , GridLayoutManager.VERTICAL , false));

        // employee info
        name = ParseUser.getCurrentUser().getUsername();
        branch = ParseUser.getCurrentUser().getString("branch");
       // Toast.makeText(this, ""+branch, Toast.LENGTH_SHORT).show();

        // init the info views
        nameView = (TextView)findViewById(R.id.employee_name_txt_home);
        branchView = (TextView)findViewById(R.id.branch_name_txt_home);
        searchTxt = (EditText)findViewById(R.id.editText);

        // init the error views
        errorImage = (ImageView)findViewById(R.id.error_image_home);
        errorText = (TextView) findViewById(R.id.error_ttext_home);

        // search the cards

        //adding a TextChangedListener
        //to call a method whenever there is some change on the EditText
        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //after the change calling the method and passing the search input
                filter(editable.toString());
            }
        });

        // floating buyyon action for qr scan
        qrfloating = (FloatingActionButton)findViewById(R.id.qr_floating);
        qrfloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this , QRActivity.class);
                finish();
                intent.putExtra("branch" , branch);
                startActivity(intent);
            }
        });

        floatingActionButton = (FloatingActionButton)findViewById(R.id.floatingActionButton);
        // log out on floating btn click
        floatingActionButton.setOnClickListener(new View.OnClickListener() {


            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                final ProgressDialog progressDialog = new ProgressDialog(HomeActivity.this);
                progressDialog.setTitle("Loading");
                progressDialog.setMessage("Getting data...");
                progressDialog.setCancelable(false);
                progressDialog.show();


                final AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Log-out");
                builder.setMessage("Are you sure you want to log out ?");
                builder.setIcon(R.drawable.ic_exit_to_app_black_24dp);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // get the number of transactions
                    ParseQuery<ParseObject> transQuery = ParseQuery.getQuery("Trans");
                    // get all the transactions done by the current user in today's date
                    transQuery.whereEqualTo("user" , ParseUser.getCurrentUser().getUsername());
                    // current day calculation
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat dayformat = new SimpleDateFormat("dd/MM/yy");
                    String day = dayformat.format(calendar.getTime());
                    // retrieve only current date
                    transQuery.whereEqualTo("day" , day);
                    transQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {

                            v = LayoutInflater.from(HomeActivity.this).inflate(R.layout.log_out_dialog , null);
                            TextView no = (TextView) v.findViewById(R.id.no_of_transactions);
                            final TextView amountV = (TextView)v.findViewById(R.id.amount_collected);
                            if( e == null)
                            {
                                if(objects.size()>=0)
                                {
                                    //Toast.makeText(HomeActivity.this, ""+objects.size(), Toast.LENGTH_SHORT).show();
                                    noOfTrans = objects.size();
                                    no.setText("Number of transactions: "+ noOfTrans);
                                    // get the amount
                                    ParseQuery<ParseObject> pricingQuery = ParseQuery.getQuery("Branch");
                                    pricingQuery.whereEqualTo("name" , branch);
                                    pricingQuery.setLimit(1);
                                    pricingQuery.findInBackground(new FindCallback<ParseObject>() {
                                        @Override
                                        public void done(List<ParseObject> objects, ParseException e) {
                                            progressDialog.cancel();
                                            if(e == null)
                                            {
                                                for(ParseObject object :  objects)
                                                {
                                                    amountCollected = Integer.parseInt(object.getString("pricing")) * noOfTrans;
                                                    amountV.setText("Amount Collected: "+amountCollected+" KWD");
                                                }

                                                // set the view then
                                                builder.setView(v);
                                                builder.setPositiveButton("Log-out", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                        // start the progress load
                                                        final ProgressDialog progressDialog = new ProgressDialog(HomeActivity.this);
                                                        progressDialog.setTitle("Log-Out");
                                                        progressDialog.setMessage("logging out...");
                                                        progressDialog.setCancelable(false);
                                                        progressDialog.show();

                                                        // log out and return to the log in screen build an alert dialog
                                                        ParseUser.getCurrentUser().logOutInBackground(new LogOutCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if( e == null)
                                                                {
                                                                    // remove saved creds
                                                                    SharedPreferences.Editor editor = LoginActivity.sharedPreferences.edit();
                                                                    editor.clear();
                                                                    editor.commit();
                                                                    progressDialog.cancel();
                                                                    Intent intent = new Intent(HomeActivity.this , LoginActivity.class);
                                                                    finish();
                                                                    startActivity(intent);
                                                                }
                                                                else
                                                                {
                                                                    progressDialog.cancel();
                                                                    Toast.makeText(HomeActivity.this, "failed to log-out , check internet connection", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });

                                                    }
                                                });
                                                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                    }
                                                });
                                                builder.show();

                                            }
                                            else {
                                                progressDialog.cancel();
                                                Toast.makeText(HomeActivity.this, "Failed , please chaeck internet connection and try again", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                            }
                            }
                            else {
                                progressDialog.cancel();
                                Toast.makeText(HomeActivity.this, "Failed , please check internet connection and try again", Toast.LENGTH_LONG).show();
                            }
                        }
                    });




                }



            }
        });
        nameView.setText("Name: "+name);
        branchView.setText("Branch: "+branch);

        // get cards from parse server
        getCards();

        printFloating = (FloatingActionButton)findViewById(R.id.clear_printer);
        printFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = LoginActivity.sharedPreferences.edit();
                editor.putString("printer", null);
                editor.commit();

                Toast.makeText(HomeActivity.this, "Default printer cleared !", Toast.LENGTH_SHORT).show();
            }
        });





    }

    // search filter
    private void filter(String text) {
        //new array list that will hold the filtered data
        ArrayList<CardModel> filterdNames = new ArrayList<>();

        //looping through existing elements
        for (CardModel s : cardsList) {
            //if the existing elements contains the search input
            if (s.code.toLowerCase().contains(text.toLowerCase())) {
                //adding the element to filtered list
                filterdNames.add(s);
            }
        }

        if(filterdNames.size() == 0)
        {

            errorImage.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.VISIBLE);

        }
        else errorImage.setVisibility(View.INVISIBLE);
        errorText.setVisibility(View.INVISIBLE);

        //calling a method of the adapter class and passing the filtered list
        adapter.filterList(filterdNames);
    }



    public void getCards()
    {

        // populate the list from parse server
        ParseQuery<ParseObject> cardsQuery = ParseQuery.getQuery("Card");
        cardsQuery.whereEqualTo("branch" , branch);
        cardsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                progressDialog.cancel();
                cardsList.clear();
                if(e == null && objects.size() > 0)
                {
                    if(objects.size()>0) {
                        for (ParseObject object : objects) {
                            cardsList.add(new CardModel("ID: " + object.getObjectId(), object.getString("status"), object.getString("Code")));
                        }

                        adapter.notifyDataSetChanged();
                    }else {
                        // no cards found
                        Toast.makeText(HomeActivity.this, "No cards have been added yet !", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    // attempt to refresh
                    Toast.makeText(HomeActivity.this, "failed to get cards , please check your internet connection and refresh", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}
