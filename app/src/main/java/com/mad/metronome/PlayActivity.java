package com.mad.metronome;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlayActivity extends AppCompatActivity {

    private static final String TAG = PlayActivity.class.getSimpleName();
    ProgressDialog progressDialog;
    File metronomeSong;
    byte[] metronomeBytes;
    Button btnStop;
    Button btnReady;
    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        btnStop = (Button) findViewById(R.id.btnStopSong);
        btnReady = (Button) findViewById(R.id.readyButton);
        groupId = getIntent().getExtras().getString(GroupDetailActivity.GROUP_ID);

        showProgressDialog("Downloading Metronome Song.");

        ParseUtil.getSong(groupId, new GetDataCallback() {
            @Override
            public void done(byte[] bytes, ParseException e) {
                if (e != null) {
                    Log.d(TAG, e.getMessage());
                    return;
                }
                try {
                    metronomeBytes = bytes;
                    // create temp file that will hold byte array
                    metronomeSong = File.createTempFile("metronome", "mp3", getApplicationContext().getCacheDir());
                    metronomeSong.deleteOnExit();
                    FileOutputStream fos = new FileOutputStream(metronomeSong);
                    fos.write(bytes);
                    fos.close();
                    Log.d(TAG, "Downloaded song of size " + metronomeSong.length() / 1024 + " KB");

                } catch (IOException ex) {
                    String s = ex.toString();
                    ex.printStackTrace();
                }
                dismissProgressDialog();
            }
        });

        btnReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUtil.setMemberStatus(groupId, ParseUser.getCurrentUser().getUsername(), ParseUtil.UserStatus.READY);
                Toast.makeText(PlayActivity.this, "Updated DB", Toast.LENGTH_SHORT).show();
//                showProgressDialog("Waiting for others.");
            }
        });


    }


    // Test method, don't use
    private void callHelloWorld() {
        Map<String, ParseObject> params = new HashMap<String, ParseObject>();
        ParseCloud.callFunctionInBackground("hello", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
//                            String msg = o.getString("msg");
                    Log.d(TAG, "Method called");
                } else {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    private void showProgressDialog(String message) {
        showProgressDialog(message, ProgressDialog.STYLE_SPINNER);
    }

    private void showProgressDialog(String message, int style) {
        progressDialog = new ProgressDialog(PlayActivity.this, style);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        progressDialog.dismiss();
    }


    //playSong is a test method and should not be used for anything else
    MediaPlayer mp;

    private void playSong() {
        mp = new MediaPlayer();
        try {
            FileInputStream fis = new FileInputStream(metronomeSong);
            mp.setDataSource(fis.getFD());
            mp.prepare();
            mp.start();
            btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mp.stop();
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
