package com.example.sttranfer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.pm.PackageManager;
import android.os.Bundle;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Executable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 1;

    private ImageView img;
    private int port = 4747;
    private String ip;
    File myFile;
    FileInputStream fis;

    private String imageEncoded;
    private List<String> imagesEncodedList;

    Thread Thread1 = null;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opening);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(MainActivity.this);
        img = (ImageView) findViewById(R.id.ivPic);
        ((Button) findViewById(R.id.bBrowse)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); //per selezionare più immagini alla volta
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
            }
        });
        Button send = (Button) findViewById(R.id.bSend);
        final TextView status = (TextView) findViewById(R.id.tvStatus);

        send.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Thread1 = new Thread(new Thread1());
                Thread1.start();
            }
        });
    }

    class Thread1 implements Runnable{
        @Override
        public void run(){
            Socket sock;
            EditText N = findViewById(R.id.IP);
            //TextView path = (TextView) findViewById(R.id.tvPath);

            try {
                ip = N.getText().toString();
                //sock = new Socket(ip, 4747);
                //System.out.println("Connection...");

                //SENDFILE
                for(int i=0; i < imagesEncodedList.size(); i++){    //scorro la lista delle immagini selezionate
                    sock = new Socket(ip, 4747);
                    System.out.println("Connection...");
                    System.out.println("Sequenza: " + i + "~Percorso:" + imagesEncodedList.get(i));
                    myFile = new File (imagesEncodedList.get(i));
                    //path.setText("Image Path: " + imagesEncodedList.get(i));
                    byte[] myarray = new byte [(int) myFile.length()];
                    fis = new FileInputStream(myFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    bis.read(myarray,0,myarray.length);
                    System.out.println("Sending...");
                    System.out.println("l'array è lungo: " + myarray.length);
                    OutputStream os=sock.getOutputStream();

                    //invio il nome del file e la sua dimensione
                    DataOutputStream dos = new DataOutputStream(os);
                    dos.writeUTF(myFile.getName());
                    dos.writeLong(myarray.length);
                    dos.write(myarray, 0, myarray.length);
                    dos.flush();

                    //invio della immagine
                    os.write(myarray, 0, myarray.length);
                    System.out.println("Sono qui");
                    os.flush();
                    //Thread.sleep(1000);
                    sock.close();
                    /*if(aspetta(sock) != true){//aspetto che il computer abbia salvato l'immagine
                        System.out.println("Si è verificato un errore nella ricezione dell'ok");
                        break;
                    }*/
                }

                /*File myFile = new File (imagesEncodedList.get(0));
                byte[] myarray = new byte [(int) myFile.length()];
                FileInputStream fis = new FileInputStream(myFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(myarray,0,myarray.length);
                OutputStream os = sock.getOutputStream(); //outputstream
                System.out.println("Sending...");
                os.write(myarray, 0, myarray.length);
                os.flush();*/
                /*
                //DO IL CHECK CHE HO FINITO AL PC
                DataOutputStream dataOutputStream = new DataOutputStream(os);
                System.out.println("Sending FINISH...");
                dataOutputStream.writeInt(1);
                dataOutputStream.flush();
                dataOutputStream.close();
                */
                //sock.close();   //chiusura socket
            } catch (IOException e) {
                e.printStackTrace();
            } //catch (InterruptedException e) {
               // e.printStackTrace();
           // }
        }
    }

    public boolean aspetta(Socket sock) throws IOException {

        InputStream input = sock.getInputStream();
        DataInputStream data = new DataInputStream(input);
        System.out.println("Waiting OK...");
        if(data.readInt() == 1){
            return true;
        }
        System.out.println("Errore!");
        return false;
    }

//SELECT IMAGE
    /*public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                TextView path = (TextView) findViewById(R.id.tvPath);
                path.setText("Image Path: " + selectedImagePath);
                img.setImageURI(selectedImageUri);
            }
        }
    }*/

    //SELECT MULTIPLE IMAGE
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            //Quando prendiamo le immagini
            if (resultCode == RESULT_OK) {
                if (requestCode == SELECT_PICTURE && data != null) {
                    // Prendiamo le immagini da data

                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    imagesEncodedList = new ArrayList<String>();
                    if (data.getData() != null) {
                        Uri selectedImageUri = data.getData();

                        //Get the cursor
                        Cursor cursor = getContentResolver().query(selectedImageUri,
                                filePathColumn, null, null, null);
                        //Move to first row
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imageEncoded = cursor.getString(columnIndex);
                        cursor.close();
                    } else {
                        if (data.getClipData() != null) {
                            ClipData mClipData = data.getClipData();
                            ArrayList<Uri> mArrayUri = new ArrayList<Uri>();

                            for (int i = 0; i < mClipData.getItemCount(); i++) {
                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                mArrayUri.add(uri);
                                //Get the cursor
                                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                                //Move to first row
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                imageEncoded = cursor.getString(columnIndex);
                                System.out.println("Aggiungo: " + imageEncoded);
                                imagesEncodedList.add(imageEncoded);
                                cursor.close();
                            }
                            Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                        }
                    }
                }
                } else {
                    Toast.makeText(this, "You haven't picked Image",
                            Toast.LENGTH_LONG).show();
                }
            }catch(Exception e){
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }

    /*public String getPath(Uri uri){
        String[] projection = { MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }*/
/*
    * Checks if the app has permission to write to device storage
    * If the app does not has permission then the user will be prompted to grant permissions
 */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}