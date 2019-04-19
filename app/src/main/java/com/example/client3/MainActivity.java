package com.example.client3;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView informationText;
    private TextView titleText;
    private ProgressBar sendProgressBar;
    private Button cancelButton;

    private EditText ipAddressEditText;
    private EditText messageEditText;
    private static ArrayList<String> stringsList;
    private static List<String> imagesPaths;
    private static boolean permission = false;
    private  static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipAddressEditText = (EditText)findViewById(R.id.editTextIP);
        messageEditText = (EditText)findViewById(R.id.editTextMessage);
        informationText = (TextView)findViewById(R.id.informationText);
        titleText = (TextView)findViewById(R.id.titleText);
        sendProgressBar = (ProgressBar)findViewById(R.id.sendProgressBar);
        cancelButton = (Button)findViewById(R.id.cancelButton);
        stringsList = new ArrayList<String>();
        if (checkPermissionREAD_EXTERNAL_STORAGE(this) && checkPermissionWRITE_EXTERNAL_STORAGE(this)) {
            getImagePathes(this);
            permission = true;
        }
        setLocalIp();
    }

    private void setLocalIp(){
        AsyncTask<Void, Void, Void> getLocalIp = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

                WifiInfo connectionInfo = wm.getConnectionInfo();
                int ipAddress = connectionInfo.getIpAddress();
                String myIp = Formatter.formatIpAddress(ipAddress);
                final String finalMyIp = new String(myIp);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ipAddressEditText.setText(finalMyIp);
                    }
                });
                return null;
            }
        };
        getLocalIp.execute();
        return;
    }

    private List<String> getImagePathes(Context context) {
        String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED};

        final Cursor cursor = context.getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
        imagesPaths = new ArrayList<String>(cursor.getCount());
        if (cursor.moveToFirst()) {
            final int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            do {
                imagesPaths.add(cursor.getString(columnIndex));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return imagesPaths;
    }

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 234;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    grantResults[0] = PackageManager.PERMISSION_GRANTED;
                    Toast.makeText(this, "GET_ACCOUNTS Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

    public boolean checkPermissionWRITE_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
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

    public void send_images(View v){
        LoaderImages(v, true);
    }
    public void download_images(View v){
        LoaderImages(v, false);
    }
    public void LoaderImages(View v, boolean isSend){
        try{
            closeKeyboard(v);
            String ipAddress = ipAddressEditText.getText().toString();
            String name = messageEditText.getText().toString().replace('@', 'a');
            if (!permission) return;
            ArrayList<Button> buttons = new ArrayList<>();
            buttons.add((Button)findViewById(R.id.sendButton));
            buttons.add((Button)findViewById(R.id.downloadButton));
            SenderImages send = new SenderImages(imagesPaths, name, buttons, isSend, this,
                    informationText, titleText, sendProgressBar, cancelButton, messageEditText);
            send.execute(ipAddress);
        }catch (Exception e){}
    }

    public void closeKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

}
