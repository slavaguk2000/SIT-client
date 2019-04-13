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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {


    private static Socket imageSocket;
    private static PrintWriter printWriter;
    private static EditText ipAdressEditText;
    private static EditText messageEditText;
    private static ListView listView;
    private static ArrayList<String> stringsList;
    private static List<String> imagesPathes;
    private static boolean permission = false;
    private static int currentImage = 0;

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
        if (checkPermissionREAD_EXTERNAL_STORAGE(this)) {
            getImagePathes(this);
            permission = true;
        }else {
             addToListView("Can't write exception");
        }
        listView.setAdapter(new ArrayAdapter<String>(listView.getContext(),android.R.layout.simple_list_item_1,stringsList));
    }


    public List<String> getImagePathes(Context context) {
        // The list of columns we're interested in:
        String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED};

        final Cursor cursor = context.getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // Specify the provider
                        columns, // The columns we're interested in
                        null, // A WHERE-filter query
                        null, // The arguments for the filter-query
                        MediaStore.Images.Media.DATE_ADDED + " DESC" // Order the results, newest first
                );

        imagesPathes = new ArrayList<String>(cursor.getCount());

        if (cursor.moveToFirst()) {
            final int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                imagesPathes.add(cursor.getString(columnIndex));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return imagesPathes;
    }

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    grantResults[0] = PackageManager.PERMISSION_GRANTED;
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
        alertBuilder.setTitle("Необходимо разрешение");
        alertBuilder.setMessage("Для корректной работы приложения необходимо это разрешение");
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
            if (!permission) return null;
            String output = "";
            try {
                imageSocket = new Socket(params[0], 5000);
                String myDeviceName = Build.MODEL;
                DataOutputStream writer = new DataOutputStream(imageSocket.getOutputStream());
                DataInputStream reader = new DataInputStream(imageSocket.getInputStream());

                byte[] sendBuffer = new byte[16 * 1024];
                int readCount;
                File currentImage;
                FileInputStream fileReader;
                int imagesFromPreviusCopyCount;
                Vector<String> imagesFromPreviusCopy = new Vector<>();
                ///// Logic of write
                writer.writeUTF(myDeviceName);
                addToListView(myDeviceName);
                writer.writeUTF(message);
                writer.flush();
                addToListView(message);
                imagesFromPreviusCopyCount = reader.readInt();
                for (int i = 0; i < imagesFromPreviusCopyCount; i++) {
                    imagesFromPreviusCopy.add(reader.readUTF());
                }

                for(int i = 0; i < 10; i++){
                    String currentImagePath = imagesPathes.get(i);
                    if (!imagesFromPreviusCopy.contains(currentImagePath)) {
                        currentImage = new File(currentImagePath);
                        fileReader = new FileInputStream(currentImage);
                        long fileLength = currentImage.length();
                        ///////////////
                        writer.writeUTF(currentImagePath);
                        addToListView(currentImagePath);
                        writer.writeLong(fileLength);
                        addToListView(fileLength + "");


                        int sendCountBytes = 0;
                        while ((readCount = fileReader.read(sendBuffer)) != -1) {
                            writer.write(sendBuffer, 0, readCount);
                            sendCountBytes+=readCount;
                            addToListView("Was writed: " + sendCountBytes);
                        }

                        ////////////////////
                        writer.flush();
                        fileReader.close();

                    }
                }
                writer.writeUTF("null");
                imageSocket.close();

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
        try{
            currentImage++;
            String ipAdress = ipAdressEditText.getText().toString();
            message = messageEditText.getText().toString().replace('@', 'a');
            SendRequest send = new SendRequest();
            addToListView("sendRequest created");
            send.execute(ipAdress, imagesPathes.get(currentImage));
        }catch (Exception e){
            addToListView("sendTextException");
        }
    }


}
