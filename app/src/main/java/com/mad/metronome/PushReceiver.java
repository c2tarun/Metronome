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
    private static final String TAG = PushReceiver.class.getSimpleName();

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);
        Log.d(TAG, "Received push");
        Intent broadcastIntent = new Intent("com.mad.metronome.TimeStampPush");
        JSONObject data = null;
        try {
            data = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            broadcastIntent.putExtra(TIMESTAMP, data.getString(TIMESTAMP));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        context.sendBroadcast(broadcastIntent);

    }
}
