/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.valetclient;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

import com.parse.ParseAnalytics;


public class MainActivity extends AppCompatActivity {


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // close the app entirely
   /* if (getIntent().getBooleanExtra("shouldFinish", false)) {
      finish();
    }*/

    final ImageView imageView = (ImageView)findViewById(R.id.logo_splash);

    // timer --> change activity after some time
    new CountDownTimer(2000 , 1 ){

      @Override
      public void onTick(long l) {
        //Toast.makeText(SplashScreenActivity.this, "timeeee", Toast.LENGTH_SHORT).show();

      }


      @TargetApi(Build.VERSION_CODES.LOLLIPOP)
      @Override
      public void onFinish() {
        Intent intent = new Intent(MainActivity.this , LoginActivity.class);
        finish();
        // transistion
        Pair[] pairs = new Pair[1];
        pairs[0]= new Pair<View, String>(imageView , "imgTrans");

        //pairs[3]= new Pair<View , String>(layout1 , "layoutTrans");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this ,pairs );
        startActivity(intent , options.toBundle());

      }
    }.start();

    
    ParseAnalytics.trackAppOpenedInBackground(getIntent());
  }

}