package com.example.sttranfer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 1;

    private ImageView img;
    private int port = 4747;
    private String ip;
    //private String ImagePath = "/storage/emulated/0/ST_Transfer";
    private String selectedImagePath;
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
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(MainActivity.this);
        img = (ImageView) findViewById(R.id.ivPic);
        ((Button) findViewById(R.id.bBrowse)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/");
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

            try {
                ip = N.getText().toString();
                sock = new Socket(ip, 4747);
                System.out.println("Connection...");

                //SENDFILE
                File myFile = new File (selectedImagePath);
                byte[] myarray = new byte [(int) myFile.length()];
                FileInputStream fis = new FileInputStream(myFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(myarray,0,myarray.length);
                OutputStream os = sock.getOutputStream(); //outputstream
                System.out.println("Sending...");
                os.write(myarray, 0, myarray.length);
                os.flush();

                sock.close();   //chiusura socket
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//SELECT IMAGE
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    }

    public String getPath(Uri uri){
        String[] projection = { MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
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