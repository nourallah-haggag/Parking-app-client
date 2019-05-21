package com.parse.valetclient;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    // declare the views
    EditText nameTxt;
    EditText passTxt;


    //name and pass
    String nameString;
    String passString;

    // shared pref manager tpoo save the user name and password
    static SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);

        // init views
        nameTxt = (EditText)findViewById(R.id.name_txt);
        passTxt = (EditText)findViewById(R.id.pass_txt);

        // strings to hold user name and password
        nameString = nameTxt.getText().toString();
        passString = passTxt.getText().toString();


        // check login status
        checkSharedPref(nameString , passString);





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

               LoginActivity.super.onBackPressed();



            }
        });
        builder.setNegativeButton("Cance", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    public void loginFunction(View view)
    {
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("please wait");
        progressDialog.setMessage("logging in");
        progressDialog.setCancelable(false);
        progressDialog.show();
        ParseUser.logInInBackground(nameTxt.getText().toString(), passTxt.getText().toString(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if ( e == null && user != null)
                {
                    progressDialog.cancel();
                    // save the user name and password for the next login

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("name" , nameTxt.getText().toString());
                    editor.putString("pass" , passTxt.getText().toString());
                    editor.commit();
                    // check if admin or not

                   // Toast.makeText(LoginActivity.this, "welcome "+user.getUsername(), Toast.LENGTH_SHORT).show();
                    // if successful go to home activity
                    Intent intent = new Intent(getBaseContext() , HomeActivity.class);
                    finish();
                    // pass user info to the next activity
                    intent.putExtra("name" , user.getUsername() );
                    intent.putExtra("branch" , user.getString("branch") );
                    startActivity(intent);
                }
                else
                {
                    progressDialog.cancel();
                    // login failed
                    Toast.makeText(LoginActivity.this, "login failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void checkSharedPref(String name  , String pass)
    {
        if(sharedPreferences.getString("name" , null) !=null)
        {
            // user saved in shared pref log in using his creds
            name = sharedPreferences.getString("name" , null);
            pass = sharedPreferences.getString("pass" , null);

            // login to parse server using saved creds
            final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setTitle("please wait");
            progressDialog.setMessage("logging in");
            progressDialog.setCancelable(false);
            progressDialog.show();
            final String finalName = name;
            final String finalPass = pass;
            ParseUser.logInInBackground(name, pass, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if ( e == null && user != null)
                    {
                        progressDialog.cancel();
                        // check if admin or not
                        // Toast.makeText(LoginActivity.this, "welcome "+user.getUsername(), Toast.LENGTH_SHORT).show();
                        // if successful go to home activity
                        Intent intent = new Intent(getBaseContext() , HomeActivity.class);
                        finish();
                        // pass user info to the next activity
                        intent.putExtra("name" , finalName);
                        intent.putExtra("branch" , finalPass );
                        startActivity(intent);
                    }
                    else
                    {
                        progressDialog.cancel();
                        // login failed
                        Toast.makeText(LoginActivity.this, "login failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}
