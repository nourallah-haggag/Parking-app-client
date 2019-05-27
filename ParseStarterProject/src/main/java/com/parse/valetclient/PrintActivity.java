package com.parse.valetclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.printservice.PrintService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import RT.BluetoothPrint.Bluetooth;

public class PrintActivity extends AppCompatActivity {



    // declare the bluetooth adapter
    public static BluetoothAdapter myBluetoothAdapter;
    String SelectedBDAddress;
     String branch;
     String code;
     String start;
     String status;
     String smsCode;
    // String end;
    static String amount;
    ImageView imageView;
    TextView textView;
    public final static int QRcodeWidth = 500 ;
    Bitmap qrImage;

    DateFormat df;
    String date;

    // ui components related to print
    ListView listView;
    TextView availablePrintersText;
    Button printButton;
    Button refreshButton;
   // Button changeDefault;



    // set the shared preferences  for the default printer

    static SharedPreferences sharedPreferences;


    class PrintInBackGround extends AsyncTask<Void , Void , Void>{
        boolean result;
        ProgressDialog progressDialog = new ProgressDialog(PrintActivity.this);
        @Override
        protected void onPreExecute() {
            imageView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);

            progressDialog.setTitle("Printing...");
            progressDialog.setCancelable(false);
            progressDialog.setIcon(R.drawable.printer);
            progressDialog.setMessage("Printing transaction...");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            result = PrintBlue(SelectedBDAddress);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.cancel();
            imageView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
            ImageView qrImageView = new ImageView(PrintActivity.this);
            qrImageView.setImageBitmap(qrImage);
            AlertDialog.Builder builder =new AlertDialog.Builder(PrintActivity.this);
            builder.setTitle("QR code");
            builder.setView(qrImageView);
            builder.setNeutralButton("Hide", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
            if(result)
            {
                Toast.makeText(PrintActivity.this, "success", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(PrintActivity.this, "failed , try again !", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.black):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    // configure byte data with ESC/POS command
    public static byte[] qrCode(String content) {
        HashMap commands = new HashMap();
        String[] commandSequence = {"model", "size", "error", "store", "content", "print"};
        int contentLen = content.length();
        int resultLen = 0;
        byte[] command;

        // QR Code: Select the model
        //              Hex     1D      28      6B      04      00      31      41      n1(x32)     n2(x00) - size of model
        // set n1 [49 x31, model 1] [50 x32, model 2] [51 x33, micro qr code]
        // https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=140
        command = new byte[]{(byte) 0x1d, (byte) 0x28, (byte) 0x6b, (byte) 0x04, (byte) 0x00, (byte) 0x31, (byte) 0x41, (byte) 0x32, (byte) 0x00};
        commands.put("model", command);
        resultLen += command.length;

        // QR Code: Set the size of module
        // Hex      1D      28      6B      03      00      31      43      n
        // n depends on the printer
        // https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=141
        command = new byte[]{(byte) 0x1d, (byte) 0x28, (byte) 0x6b, (byte) 0x03, (byte) 0x00, (byte) 0x31, (byte) 0x43, (byte) 0x0A};
        commands.put("size", command);
        resultLen += command.length;

        //          Hex     1D      28      6B      03      00      31      45      n
        // Set n for error correction [48 x30 -> 7%] [49 x31-> 15%] [50 x32 -> 25%] [51 x33 -> 30%]
        // https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=142
        command = new byte[]{(byte) 0x1d, (byte) 0x28, (byte) 0x6b, (byte) 0x03, (byte) 0x00, (byte) 0x31, (byte) 0x45, (byte) 0x33};
        commands.put("error", command);
        resultLen += command.length;

        // QR Code: Store the data in the symbol storage area
        // Hex      1D      28      6B      pL      pH      31      50      30      d1...dk
        // https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=143
        //                        1D          28          6B         pL          pH  cn(49->x31) fn(80->x50) m(48->x30) d1…dk
        int storeLen = contentLen + 3;
        byte store_pL = (byte) (storeLen % 256);
        byte store_pH = (byte) (storeLen / 256);
        command = new byte[]{(byte) 0x1d, (byte) 0x28, (byte) 0x6b, store_pL, store_pH, (byte) 0x31, (byte) 0x50, (byte) 0x30};
        commands.put("store", command);
        resultLen += command.length;

        // QR Code content
        command = content.getBytes();
        commands.put("content", command);
        resultLen += command.length;

        // QR Code: Print the symbol data in the symbol storage area
        // Hex      1D      28      6B      03      00      31      51      m
        // https://reference.epson-biz.com/modules/ref_escpos/index.php?content_id=144
        command = new byte[]{(byte) 0x1d, (byte) 0x28, (byte) 0x6b, (byte) 0x03, (byte) 0x00, (byte) 0x31, (byte) 0x51, (byte) 0x30};
        commands.put("print", command);
        resultLen += command.length;

        int cnt = 0;
        int commandLen = 0;
        byte[] result = new byte[resultLen];
        for (String currCommand : commandSequence) {
            command = (byte[]) commands.get(currCommand);
            commandLen = command.length;
            System.arraycopy(command, 0, result, cnt, commandLen);
            cnt += commandLen;
        }

        return result;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        // init the shared preferences
        sharedPreferences = LoginActivity.sharedPreferences;

        // check shared preferences value if default is set print right away and hide the list
        // if not show the list and let the user select
        // if default is set hide the main text , the list and the button and print directly
        // TODO: check shared preferences




        df = new SimpleDateFormat("HH:mm");
        date = df.format(Calendar.getInstance().getTime());
        Log.i("time" , date);

        // init the ui components related to print
        listView = (ListView) findViewById(R.id.list_view_devices);
        availablePrintersText = (TextView)findViewById(R.id.textView);
        printButton = (Button)findViewById(R.id.print_button) ;
        refreshButton = (Button)findViewById(R.id.refresh_btn) ;
       // changeDefault = (Button)findViewById(R.id.default_printer) ;






        imageView = (ImageView)findViewById(R.id.printing_img);
        textView = (TextView)findViewById(R.id.printing_txt) ;
        imageView.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);

        SelectedBDAddress = "";
        branch = getIntent().getStringExtra("branch");
        code = getIntent().getStringExtra("code");
        start = getIntent().getStringExtra("start");
        status = getIntent().getStringExtra("status");
      //  end = getIntent().getStringExtra("end");
        final ProgressDialog  progressDialog = new ProgressDialog(PrintActivity.this);
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
            public void done(List<ParseObject> objects, ParseException e) {
                progressDialog.cancel();
                if( e==null && objects.size()!=0)
                {
                    for(ParseObject object : objects)
                    {
                        PrintActivity.amount = object.getString("pricing");
                        Toast.makeText(PrintActivity.this, ""+amount, Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(PrintActivity.this, "error", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PrintActivity.this , HomeActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });

       // Toast.makeText(this, ""+start+""+code+""+branch+""+amount, Toast.LENGTH_SHORT).show();


        // show devices
        ListBluetoothDevice();

        // convert code to qr
        try {
            qrImage =  TextToImageEncode(code);
        } catch (WriterException e) {
            e.printStackTrace();
        }


        checkSharedPref();


    }


    public  boolean ListBluetoothDevice()
    {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Bluetooth");
        progressDialog.setMessage("Finding Paired Bluetooth Devices...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        final List<Map<String,String>> list=new ArrayList<Map<String, String>>();

        SimpleAdapter m_adapter = new SimpleAdapter( this,list,
                android.R.layout.simple_list_item_2,
                new String[]{"DeviceName","BDAddress"},
                new int[]{android.R.id.text1,android.R.id.text2}
        );
        progressDialog.cancel();
        listView.setAdapter(m_adapter);

        if((myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter())==null)
        {
            progressDialog.cancel();
            Toast.makeText(this,"Did not find the Bluetooth adapter", Toast.LENGTH_LONG).show();//Ã»ÓÐÕÒµ½À¶ÑÀÊÊÅäÆ÷
            return false;
        }
        if(!myBluetoothAdapter.isEnabled())
        {
            progressDialog.cancel();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 2);
        }

        Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() <= 0)return false;
        for (BluetoothDevice device : pairedDevices)
        {
            Map<String,String> map=new HashMap<String, String>();
            map.put("DeviceName", device.getName());
            map.put("BDAddress", device.getAddress());
            list.add(map);
        }
        listView.setOnItemClickListener(new ListView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                SelectedBDAddress = list.get(position).get("BDAddress");
                if (((ListView)parent).getTag() != null){
                    ((View)((ListView)parent).getTag()).setBackgroundDrawable(null);
                }
                ((ListView)parent).setTag(view);
                view.setBackgroundColor(getResources().getColor(R.color.orange));
            }
        });
        return true;
    }//ListBluetoothDevice

    public boolean PrintBlue(String BDAddress)
    {

        // at this point the print is successful so the printer will be set as default
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("printer" , BDAddress);
        editor.commit(); // if commit causes problems change to apply





        if(!Bluetooth.OpenPrinter(BDAddress))
        {
            // showMessage(Bluetooth.ErrorMessage);
          // Toast.makeText(PrintActivity.this, Bluetooth.ErrorMessage, Toast.LENGTH_LONG).show();

            Bluetooth.close();

            // change views from another thread other than the one responsible for printing
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    availablePrintersText.setText("Choose default printer");

                    // show ui components
                    listView.setVisibility(View.VISIBLE);
                    refreshButton.setVisibility(View.VISIBLE);
                    printButton.setVisibility(View.VISIBLE);

                }
            });




            // handle the error
          /*  AlertDialog.Builder builder = new AlertDialog.Builder(PrintActivity.this);
            builder.setTitle("Retry");
            builder.setIcon(R.drawable.ic_refresh_black_24dp);
            builder.setMessage("Cannot proceed unless an invoice is printed , please make sure you have selected a working bluetooth printer !!");

            builder.setCancelable(false);
            builder.setPositiveButton("retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;

                }
            });
            builder.show();*/
            return false;
            // statusBox.Close();

        }

           /* ProgressDialog progressDialog = new ProgressDialog(PrintActivity.this);
            progressDialog.setTitle("Bluetooth printer");
            progressDialog.setMessage("printing...");
            progressDialog.setCancelable(false);
            progressDialog.show();*/
          //  Toast.makeText(PrintActivity.this, "Branch: "+branch+"\n Code: "+code+"\n Start Time: "+start+" \n End Time: "+end+"\n Amount: "+amount, Toast.LENGTH_SHORT).show();

           // Bluetooth.printString("Branch: "+branch+"\n Code: "+code+"\n Start Time: "+start+"\n Amount: "+amount);
           // Bluetooth.printQRCode(code);
           /* ByteArrayOutputStream baos = new ByteArrayOutputStream();
            qrImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();
            Bluetooth.printByteData(data);*/
           // get the current date
        // log in the current day
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayformat = new SimpleDateFormat("dd/MM/yy");
        String day = dayformat.format(calendar.getTime());



        //send centering command for printer
            byte[] center = new byte[]{ 0x1b, 0x61, 0x01 };
            Bluetooth.SPPWrite(center);
            Bluetooth.printString(day+" "+date+" "+"-{"+code+"}-");
        Bluetooth.printString("");
            Bluetooth.printString("GOLDEN VALET welcomes you to");
            Bluetooth.printString(branch);
        Bluetooth.printString("");
            //TODO: generate code to send via sms to the fixed number
            Bluetooth.printString("Price: "+amount+" KWD");
        Bluetooth.printString("");
            Bluetooth.printString("to collect car,");//, SMS "+smsCode+" to 55001128");
            Bluetooth.printString("SMS "+smsCode);
            Bluetooth.printString("to 97171771");
            Bluetooth.printString("");
            byte[] qrToPrint = qrCode(code);
            Bluetooth.printByteData(qrToPrint);
            Bluetooth.printString("");
            Bluetooth.printString("");



            Bluetooth.close();
            return true;
            // statusBox.Close();
           // progressDialog.cancel();
            /*Intent intent = new Intent(PrintActivity.this , HomeActivity.class);
            startActivity(intent);*/




    }//print2

    public  void print(View v)
    {
       // Toast.makeText(this, ""+amount, Toast.LENGTH_SHORT).show();
        if(SelectedBDAddress.equals(""))
        {
            Toast.makeText(this, "please select a bluetooth printer", Toast.LENGTH_SHORT).show();
        }
        else {
            generateSmsCode();
        }




    }

    @Override
    public void onBackPressed() {


        AlertDialog.Builder builder = new AlertDialog.Builder(PrintActivity.this);
        builder.setTitle("Abort Printing ?!");
        builder.setMessage("The transaction will not be printed");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(PrintActivity.this , HomeActivity.class);
                finish();
                startActivity(intent);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();


    }

    public void refresh(View v)
    {
        ListBluetoothDevice();
    }


    public void generateSmsCode()
    {
        final ProgressDialog progressDialog = new ProgressDialog(PrintActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Generating SMS...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        int aNumber = 0;
        aNumber = (int)((Math.random() * 9000000)+1000000);

        // query the parse databse
        ParseQuery<ParseObject> codeQuery = ParseQuery.getQuery("SMS");
        codeQuery.whereEqualTo("code" , aNumber);
        codeQuery.setLimit(1);
        final int finalANumber = aNumber;
        codeQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                progressDialog.cancel();
                if( e == null)
                {
                    if(objects.size()>0)
                    {
                        generateSmsCode(); // recursive function keeps calling until random number is not in the database

                    }
                    else
                    {
                        // fi code is not add it to the parse server
                      final ParseObject smsObject = new ParseObject("SMS");
                      smsObject.put("smsCode" , Integer.toString(finalANumber));
                      smsObject.put("code" , code);
                      smsObject.put("branch" , branch);
                      smsObject.put("status" , status);
                      smsObject.saveInBackground(new SaveCallback() {
                          @Override
                          public void done(ParseException e) {
                              if(e  == null)
                              {
                                  Toast.makeText(PrintActivity.this, "SMS code generated ", Toast.LENGTH_SHORT).show();
                                  smsCode = Integer.toString(finalANumber);
                                  // sms has a value -- now we can print
                                  PrintInBackGround printInBackGround =new PrintInBackGround();
                                  printInBackGround.execute();
                              }
                              else
                              {
                                  smsObject.saveEventually();
                              }
                          }
                      });
                    }
                }
            }
        });
       // return aNumber;
    }


    // check shared preferences value
    public void checkSharedPref()
    {
        String printerAddress = sharedPreferences.getString("printer" , null);
        if(printerAddress!=null)
        {
            // an address is saved in the shared pref

            // hide ui components
            availablePrintersText.setText("Printing reciept");
            listView.setVisibility(View.INVISIBLE);
            refreshButton.setVisibility(View.INVISIBLE);
            printButton.setVisibility(View.INVISIBLE);
            //changeDefault.setVisibility(View.INVISIBLE);


            // print directly
            SelectedBDAddress = printerAddress;
            generateSmsCode(); // print directly

        }
        else {

            // show alert dialog for the first time
            availablePrintersText.setText("Choose default printer");

            // show ui components
            listView.setVisibility(View.VISIBLE);
            refreshButton.setVisibility(View.VISIBLE);
            printButton.setVisibility(View.VISIBLE);


        }
    }

   /* public void changeDefaultPrinter(View v)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("printer", null);
        editor.commit();

        availablePrintersText.setText("Choose default printer");
        listView.setVisibility(View.VISIBLE);
        refreshButton.setVisibility(View.VISIBLE);
    }*/
}








