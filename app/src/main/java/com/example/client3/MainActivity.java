package com.example.client3;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static Socket s;
    private static PrintWriter printWriter;
    private static EditText ipAdressEditText;
    private static EditText messageEditText;
    private static ListView listView;
    private static ArrayList<String> stringsList;


    String message = "Android";
    private static String ip = "192.168.0.107";

    /**
     * Cursor used to access the results from querying for images on the SD card.
     */
    //private Cursor cursor;
    /*
     * Column index for the Thumbnails Image IDs.
     */
    //private int columnIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipAdressEditText = (EditText)findViewById(R.id.editTextIP);
        messageEditText = (EditText)findViewById(R.id.editTextMessage);
        listView = (ListView)findViewById(R.id.textListView);
        stringsList = new ArrayList<String>();

        addToListView("34");
        if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
            // do your stuff..

            String[] imageStrings = {MediaStore.Images.Thumbnails._ID, MediaStore.Images.Media.DATA};

            Uri uri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;

            Cursor cursor = getContentResolver().query(uri, imageStrings, null,
                    null, MediaStore.Images.Thumbnails.IMAGE_ID);


            System.out.println("onActivityCreated: " + cursor.getCount());
            int countImages = cursor.getCount();
            addToListView(new Integer(countImages).toString());
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            for (int i = 0; i < countImages; i++) {
                cursor.moveToPosition(i);
                String imagePath = cursor.getString(columnIndex);
                Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI
                addToListView(imagePath);
            }
        }else addToListView("everything is bad");
        listView.setAdapter(new ArrayAdapter<String>(listView.getContext(),android.R.layout.simple_list_item_1,stringsList));
    }

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    Toast.makeText(this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    void addToListView(String addingString){
        final String addStr = new String(addingString);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stringsList.add(addStr);
            }
        });
    }


    private class SendRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params){
            String output = "";
            try{
                System.out.println("In sending");
                output += "In sending on ip: ";
                output += params[0];
                output += ":5000; ";
                s = new Socket(params[0], 5000);
                System.out.println("Client Connected");
                output += "Client Connected";
                addToListView(message);
                printWriter = new PrintWriter(s.getOutputStream());
                printWriter.write(message);
                printWriter.flush();
                printWriter.close();
                s.close();

            }
            catch(UnknownHostException ex){
                System.out.println("UnknownHostException");
                output += "UnknownHostException";
            }catch(IOException ex){
                System.out.println("IOException");
                output += "IOException";
            }
            catch (Exception ex){
                System.out.println("Exception");
                output += "Exception";
            }
            finally {
               // Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
            }
            return null;
        }
    }

    public void send_text(View v){
        String ipAdress = ipAdressEditText.getText().toString();
        message = messageEditText.getText().toString();
        SendRequest send = new SendRequest();
        send.execute(ipAdress);
    }


}
