package com.mad.metronome;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends AppCompatActivity {

    private static final String TAG = PlayActivity.class.getSimpleName();
    ProgressDialog progressDialog;
    File metronomeSong;
    byte[] metronomeBytes;
    Button btnStop;
    Button btnReady;
    String groupId;
    BroadcastReceiver timeStampReceiver;
    public static final String CHANNEL_PREFIX = "ch";
    String channelName;
    TextView tvTimer;
    Button btnPlaySong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        btnStop = (Button) findViewById(R.id.btnStopSong);
        btnReady = (Button) findViewById(R.id.readyButton);
        groupId = getIntent().getExtras().getString(GroupDetailActivity.GROUP_ID);
        tvTimer = (TextView) findViewById(R.id.timerTextView);
        btnPlaySong = (Button) findViewById(R.id.playSongButton);
        channelName = CHANNEL_PREFIX + groupId;
        String from = getIntent().getExtras().getString("From");
        if (from != null && from.equals("Push")) {
            Log.d(TAG, "Came from push");
            return;
        }

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

        timeStampReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String ts = intent.getExtras().getString(PushReceiver.TIMESTAMP);
                long currentTime = new Date().getTime();
                Log.d(TAG, "Received pus555h from play activity " + ts);

                long timerStartTime = Long.parseLong(ts);
                Toast.makeText(PlayActivity.this, "Timer will start after " + (timerStartTime - currentTime), Toast.LENGTH_LONG).show();
                Timer countDownStart = new Timer();
                countDownStart.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Timer started");
                        for (int i = 10; i >= 1; i--) {
                            runOnUiThread(new MyThread(i + ""));
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playSong();
                            }
                        });
                    }
                }, new Date(timerStartTime));
            }
        };
        btnPlaySong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSong();
            }
        });
    }

    class MyThread extends Thread {
        String label;

        public MyThread(String label) {
            this.label = label;
        }

        @Override
        public void run() {
            tvTimer.setText(label);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(timeStampReceiver, new IntentFilter("com.mad.metronome.TimeStampPush"));
        ParsePush.subscribeInBackground(channelName);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(timeStampReceiver);
        Log.d(TAG, "Unsubscribing from channel " + channelName);
        ParsePush.unsubscribeInBackground(channelName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Test method, don't use
    private void callHelloWorld() {
        Map<String, ParseObject> params = new HashMap<String, ParseObject>();
        ParseCloud.callFunctionInBackground("hello", params, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e == null) {
//                            String msg = o.getString("msg");mike
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
