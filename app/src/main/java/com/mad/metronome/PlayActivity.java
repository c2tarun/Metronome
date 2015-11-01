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

import com.parse.FunctionCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
    Button btnLastLoop;
    Button btnReady;
    String groupId;
    BroadcastReceiver timeStampReceiver;
    BroadcastReceiver lastLoopReceiver;
    public static final String CHANNEL_PREFIX = "ch";
    String channelName;
    TextView tvTimer;
    boolean isLeader;
    boolean enableLooping = true;
    private static final int COUNTER_LIMIT = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        btnLastLoop = (Button) findViewById(R.id.btnLastLoop);
        btnReady = (Button) findViewById(R.id.readyButton);
        groupId = getIntent().getExtras().getString(GroupDetailActivity.GROUP_ID);
        tvTimer = (TextView) findViewById(R.id.timerTextView);
        channelName = CHANNEL_PREFIX + groupId;
        String from = getIntent().getExtras().getString("From");
        if (from != null && from.equals("Push")) {
            Log.d(TAG, "Came from push");
            return;
        }
        isLeader = getIntent().getExtras().getBoolean(GroupDetailActivity.IS_LEADER);

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
                btnReady.setEnabled(false);
                ParseUtil.setMemberStatus(groupId, ParseUser.getCurrentUser().getUsername(), ParseUtil.UserStatus.READY);
                showProgressDialog("Waiting for others.");
            }
        });

        timeStampReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String ts = intent.getExtras().getString(PushReceiver.TIMESTAMP);
                String st = intent.getExtras().getString(PushReceiver.SERVERTIME);
                long currentTime = new Date().getTime();
                Log.d(TAG, "Received push from play activity " + ts);

                long timerStartTime = Long.parseLong(ts);

                // Code to fix issue if all are on different network -- START
                long serverTime = Long.parseLong(st);
                long timeDiff = currentTime - serverTime;
                timerStartTime += timeDiff;
                // -- END

                Timer countDownStart = new Timer();
                countDownStart.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Timer started");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgressDialog();
                            }
                        });
                        for (int i = COUNTER_LIMIT; i >= 1; i--) {
                            runOnUiThread(new MyThread(i + ""));
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        runOnUiThread(new MyThread("Go.."));
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

        lastLoopReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG,"Received push for last loop");
                enableLooping = false;
                tvTimer.setTextSize(100);
                tvTimer.setText("Last Loop");
            }
        };


        btnLastLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnLastLoop.setEnabled(false);

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("GroupId", groupId);
                ParseCloud.callFunctionInBackground("last_loop", params);
                ParseUtil.clearGroup(groupId);
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
        registerReceiver(lastLoopReceiver, new IntentFilter("com.mad.metronome.LastLoop"));
        ParsePush.subscribeInBackground(channelName, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Registered with " + channelName);
                } else {
                    Log.d(TAG, "Channel registration failed " + e.getMessage());
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(timeStampReceiver);
        unregisterReceiver(lastLoopReceiver);
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


    MediaPlayer mp;

    private void playSong() {
        if (isLeader) {
            btnLastLoop.setVisibility(View.VISIBLE);
        }
        mp = new MediaPlayer();
        mp.setScreenOnWhilePlaying(true);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "Loop completed, continue " + enableLooping);
                if (enableLooping) {
                    mp.start();
                } else {
                    Intent intent = new Intent(PlayActivity.this, GroupDetailActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        try {
            FileInputStream fis = new FileInputStream(metronomeSong);
            mp.setDataSource(fis.getFD());
            mp.prepare();
            mp.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
