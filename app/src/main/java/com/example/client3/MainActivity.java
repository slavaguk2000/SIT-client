package com.example.client3;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private static Socket s;
    private static PrintWriter printWriter;
    private static EditText ipAdressEditText;
    private static EditText messageEditText;


    String message = "Android";
    private static String ip = "192.168.43.188";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipAdressEditText = (EditText)findViewById(R.id.editTextIP);
        messageEditText = (EditText)findViewById(R.id.editTextMessage);
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
