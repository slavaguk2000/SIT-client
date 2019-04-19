package com.example.client3;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import android.provider.Settings.Secure;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SenderImages extends AsyncTask<String, Void, String> {
    DataOutputStream writer;
    DataInputStream reader;
    List<String> imagesPathes;
    Socket socket;
    boolean isSend;
    String personName;
    final ArrayList<Button> buttons;//0-send, 1-download
    Context context;
    private static TextView informationText;
    private static TextView titleText;
    private static ProgressBar sendProgressBar;
    private static Button cancelButton;
    private static View nameText;
    private static boolean isWorking = false;
    SenderImages(List<String> imagesPathes, String personName, ArrayList<Button> buttons, boolean isSend, Context context,
                 TextView informationText, TextView titleText, ProgressBar sendProgressBar, Button cancelButton, View nameText){
        this.imagesPathes = imagesPathes;
        this.personName = personName;
        this.context = context;
        this.buttons = buttons;
        this.isSend = isSend;
        this.informationText = informationText;
        this.titleText = titleText;
        this.sendProgressBar = sendProgressBar;
        this.cancelButton = cancelButton;
        this.nameText = nameText;
    }
    private void setButtonsEnable(boolean state){
        final boolean finalState = state;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                for (Button btn: buttons) {
                    btn.setEnabled(finalState);
                }
            }
        });
    }
    private void setInformationVisible(boolean state){
        final int finalState = state? View.VISIBLE:View.GONE;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                informationText.setVisibility(finalState);
                titleText.setVisibility(finalState);
                sendProgressBar.setVisibility(finalState);
                cancelButton.setVisibility(finalState);
            }
        });
    }
    private void setProgress(final int progress, final String information){
        final int finalProgress = progress;
        final String finalInformation;
        if (progress > 0) finalInformation = information + progress + "%";
        else finalInformation = information;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                if (progress > 0) sendProgressBar.setProgress(finalProgress);
                informationText.setText(finalInformation);
            }
        });
    }
    private void changeButton(boolean state){
        final String buttonText;
        final View.OnClickListener onClickListener;
        final String finalTitleText;
        if (state){
            buttonText = "OK";
            finalTitleText = "Copying files was comlited";
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setInformationVisible(false);
                }
            };
        }else{
            buttonText = "CANCEL";
            isWorking = true;
            finalTitleText = "Copying files...";
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isWorking = false;
                    changeButton(true);
                }
            };
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                setInformationVisible(true);
                cancelButton.setText(buttonText);
                cancelButton.setOnClickListener(onClickListener);
                titleText.setText(finalTitleText);
            }
        });
    }
    private Vector<String> getImagesFromPreviousCopy() throws IOException{
        Vector<String> imagesFromPreviousCopy = new Vector<>();
        int imagesFromPreviousCopyCount = reader.readInt();
        for (int i = 0; i < imagesFromPreviousCopyCount; i++) {
            imagesFromPreviousCopy.add(reader.readUTF().replace("\\", "/"));
        }
        return imagesFromPreviousCopy;
    }
    @Override
    protected String doInBackground(String... params){
         try {
            socket = new Socket(params[0], 4242);
            setButtonsEnable(false);

            String myDeviceName = Build.MODEL + "_" + Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
            writer = new DataOutputStream(socket.getOutputStream());
            reader = new DataInputStream(socket.getInputStream());

            //Initialising
            writer.writeUTF(myDeviceName);
            writer.writeUTF(personName);
            writer.writeBoolean(isSend);
            writer.flush();
            changeButton(false);
            if (isSend) send_images();
            else download_images();
            changeButton(true);
            socket.close();
        }catch(UnknownHostException ex){
            System.out.println("UnknownHostException");
        }catch(IOException ex){
            System.out.println("IOException");
        }catch (Exception ex){
            System.out.println("Exception");
        }
        setButtonsEnable(true);
        return null;
    }

    private void send_images()throws IOException {
        String information = "Sending files: ";
        setProgress(0, information);
        Vector<String> imagesFromPreviousCopy = getImagesFromPreviousCopy();
        FileInputStream fileReader;
        File currentImage;
        int readCount;
        ///// Logic of write
        int countOfImage = imagesPathes.size();
        if (countOfImage > 100) countOfImage = 100; /////////////for debugging
        writer.writeInt(countOfImage);
        String failedInformation = "Copying was failed";
        try {
            long endFileLengty = 0;
            for (int i = 0; i < countOfImage; i++) {
                setProgress(100 * i / countOfImage, information);
                String currentImagePath = imagesPathes.get(i);
                if (!imagesFromPreviousCopy.contains(currentImagePath)) {
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
                    if (!isWorking) {
                        endFileLengty = 1;
                        break;
                    }
                } else {
                    writer.writeLong(1);
                    writer.writeUTF("exist");
                }
            }
            if (endFileLengty == 1) setProgress(-1, failedInformation);
            else setProgress(100, information);
            writer.writeLong(endFileLengty);
            writer.writeUTF("null");
            try {
                wait(5000);
            } catch (Exception ex) {}
        }catch(IOException ex){
            setProgress(-1, failedInformation);
        }
    }

    private void download_images() throws IOException{
        String information = "Downloading files: ";
        setProgress(0, information);
        boolean existSaving = reader.readBoolean();
        if (existSaving) {
            Vector<String> imagesFromCurrentCopy = getImagesFromPreviousCopy();
            FileOutputStream fileWriter;
            Vector<Integer> numbersOfNeeds = new Vector<>();
            int imagesFromCurrentCopyCount = imagesFromCurrentCopy.size();
            for (int i = 0; i < imagesFromCurrentCopyCount; i++) {
                if (!imagesPathes.contains(imagesFromCurrentCopy.get(i))) {
                    numbersOfNeeds.add(i);
                }
            }
            int numberOfNeedsCount = numbersOfNeeds.size();
            writer.writeInt(numberOfNeedsCount);
            for (int number : numbersOfNeeds) {
                writer.writeInt(number);
            }
            writer.flush();
            for (int i = 0; i < numberOfNeedsCount; i++) {
                setProgress(100*i/numberOfNeedsCount, information);
                long fileLength = reader.readLong();
                String filePath = reader.readUTF();
                byte[] readBuffer = new byte[64 * 1024];
                File image = new File(filePath);
                image.getParentFile().mkdirs();
                fileWriter = new FileOutputStream(filePath);
                int count, total = 0;
                while ((count = reader.read(readBuffer, 0, (int) Math.min(readBuffer.length, fileLength - total))) != -1) {
                    total += count;
                    fileWriter.write(readBuffer, 0, count);

                    if (total == fileLength) {
                        break;
                    }
                }
                fileWriter.flush();
                fileWriter.close();
                if(!isWorking) break;
            }
            setProgress(100, information);
        }else{
            PopupMenu popupMenu = new PopupMenu(context, nameText);
            Vector<String> existNames= new Vector<>();
            int namesSize = reader.readInt();//количество сохранений
            for (int i = 0; i < namesSize; i++) {
                String currentName = reader.readUTF();
                existNames.add(currentName);
                popupMenu.getMenu().add(currentName);
            }//передача сохранений
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(final MenuItem item) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            EditText nameEditText = (EditText)nameText;
                            nameEditText.setText(item.getTitle().toString());
                        }
                    });
                    return false;
                }
            });
            popupMenu.show();
        }
    }
}
