package com.mad.metronome;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tarun on 10/31/15.
 */
public class PushReceiver extends ParsePushBroadcastReceiver {
    public static final String TIMESTAMP = "ts";
    public static final String SERVERTIME = "ct";
    public static final String LAST_LOOP_PUSH = "LastLoop";
    private static final String TAG = PushReceiver.class.getSimpleName();
    public static final String LOOP = "loop";
    public static final String START_TIMER_PUSH = "StartTimer";

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        Log.d(TAG, "Received push");
        Intent broadcastIntent = null;
        JSONObject data = null;
        try {
            data = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            String cause = data.getString("cause");
            if(cause.equals(START_TIMER_PUSH)) {
                broadcastIntent  = new Intent("com.mad.metronome.TimeStampPush");
                broadcastIntent.putExtra(TIMESTAMP, data.getString(TIMESTAMP));
                broadcastIntent.putExtra(SERVERTIME, data.getString(SERVERTIME));
            } else if(cause.equals(LAST_LOOP_PUSH)) {
                broadcastIntent  = new Intent("com.mad.metronome.LastLoop");
                broadcastIntent.putExtra(TIMESTAMP, data.getString(LOOP));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        context.sendBroadcast(broadcastIntent);

    }
}
