package com.example.client3;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;
import android.provider.Settings.Secure;

public class SenderImages extends AsyncTask<String, Void, String> {

    List<String> imagesPathes;
    Socket socket;
    String personName;
    Button send_button;
    Context context;
    SenderImages(List<String> imagesPathes, String personName, Button send_button, Context context){
        this.imagesPathes = imagesPathes;
        this.personName = personName;
        this.send_button = send_button;
        this.context = context;
    }
    void changeButtonState(Button btn, boolean state){
        final boolean finalState = state;
        final Button finalBtn = btn;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                finalBtn.setEnabled(finalState);
            }
        });
    }
    @Override
    protected String doInBackground(String... params){
        String output = "";
        try {
            socket = new Socket(params[0], 2154);
            changeButtonState(send_button, false);

            String myDeviceName = Build.MODEL + "_" + Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
            DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
            DataInputStream reader = new DataInputStream(socket.getInputStream());

            int readCount;
            File currentImage;
            FileInputStream fileReader;
            int imagesFromPreviusCopyCount;
            Vector<String> imagesFromPreviusCopy = new Vector<>();
            int countOfImage = imagesPathes.size();
            if (countOfImage > 20) countOfImage = 20;//////////////!!!!!!!!!!!!!!!!!!!!!!!!for debug
            ///// Logic of write

            writer.writeUTF(myDeviceName);
            writer.writeUTF(personName);
            writer.writeInt(countOfImage);
            writer.flush();
            imagesFromPreviusCopyCount = reader.readInt();
            for (int i = 0; i < imagesFromPreviusCopyCount; i++) {
                imagesFromPreviusCopy.add(reader.readUTF().replace("\\", "/"));
            }

            for(int i = 0; i < countOfImage; i++){
                String currentImagePath = imagesPathes.get(i);
                if (!imagesFromPreviusCopy.contains(currentImagePath)) {
                    currentImage = new File(currentImagePath);

                    long fileLength = currentImage.length();

                    writer.writeLong(fileLength);
                    writer.writeUTF(currentImagePath);
                    fileReader = new FileInputStream(currentImage);
                    byte[] sendBuffer = new byte[64 * 1024];
                    while ((readCount = fileReader.read(sendBuffer)) != -1) {
                        writer.write(sendBuffer, 0, readCount);
                    }
                    writer.flush();
                    fileReader.close();
                }
            }
            writer.writeLong(0);
            writer.writeUTF("null");
            wait(5000);
            socket.close();
            changeButtonState(send_button, true);
        }
        catch(UnknownHostException ex){
            System.out.println("UnknownHostException");
        }catch(IOException ex){
            System.out.println("IOException");
        }
        catch (Exception ex){
            System.out.println("Exception");
        }

        return null;
    }
}
