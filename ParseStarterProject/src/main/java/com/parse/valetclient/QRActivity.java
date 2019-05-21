package com.parse.valetclient;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class QRActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private static int camId = Camera.CameraInfo.CAMERA_FACING_BACK;
    String branch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        branch = getIntent().getStringExtra("branch");


        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        int currentApiVersion = Build.VERSION.SDK_INT;

        if(currentApiVersion >=  Build.VERSION_CODES.M)
        {
            if(checkPermission())
            {
                Toast.makeText(getApplicationContext(), "Permission already granted!", Toast.LENGTH_LONG).show();
            }
            else
            {
                requestPermission();
            }
        }
    }

    private boolean checkPermission()
    {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if(scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted){
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(QRActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {
        final String myResult = result.getText();
        Log.i("QRCodeScanner", result.getText());
        Log.i("QRCodeScanner", result.getBarcodeFormat().toString());
        final ProgressDialog progressDialog = new ProgressDialog(QRActivity.this);
        progressDialog.setTitle("Searching...");
        progressDialog.setMessage("searching for a matching card code");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Toast.makeText(this, "code: "+result.getText(), Toast.LENGTH_SHORT).show();
        // if code is found go to the corressponding activity according to its status
        ParseQuery<ParseObject> cardQuery = ParseQuery.getQuery("Card");
        cardQuery.whereEqualTo("Code" , result.getText());
        cardQuery.whereEqualTo("branch" , branch );
        cardQuery.setLimit(1);
        cardQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if( e==null && objects.size()!=0)
                {
                    progressDialog.cancel();
                    for(ParseObject object : objects)
                    {
                        if(object.getString("status").equals("free"))
                        {
                            Intent intent = new Intent(QRActivity.this , ParkActivity.class);
                            finish();
                            intent.putExtra("cardCode" , object.getString("Code"));
                            intent.putExtra("cardStatus" , object.getString("status"));
                            startActivity(intent);

                        }else if(object.getString("status").equals("parked"))
                        {
                            Intent intent = new Intent(QRActivity.this , FreeActivity.class);
                            finish();
                            intent.putExtra("cardCode" , object.getString("Code"));
                            intent.putExtra("cardStatus" , object.getString("status"));
                            startActivity(intent);

                        }
                        else {
                            Intent intent = new Intent(QRActivity.this , CallingActivity.class);
                            finish();
                            intent.putExtra("cardCode" , object.getString("Code"));
                            intent.putExtra("cardStatus" , object.getString("status"));
                            startActivity(intent);

                        }

                    }
                }
                else
                {
                    progressDialog.cancel();
                    Toast.makeText(QRActivity.this, "no match found", Toast.LENGTH_SHORT).show();
                    scannerView.resumeCameraPreview(QRActivity.this);
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
