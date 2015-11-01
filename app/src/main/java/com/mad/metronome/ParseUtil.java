package com.mad.metronome;

import android.media.MediaPlayer;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by tarun on 10/25/15.
 */
public class ParseUtil extends MediaPlayer {

    private static final String TAG = ParseUtil.class.getSimpleName();
    public static final String TB_GROUP_STRENGTH = "GRP_STRENGTH";
    public static final String CL_GROUP_ID = "GroupId";
    public static final String CL_GROUP_STRENGTH = "Strength";
    private static final String TB_GROUP_STATUS = "GRP_STATUS";
    private static final String CL_USER_ID = "UserId";
    private static final String CL_STATUS = "Status";
    public static final String TB_SONGS = "Songs";

    public enum UserStatus {
        READY("Ready"), NOT_READY("Not Ready");
        private String status;

        UserStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    public static void updateStrengthTable(final String groupId, final int strength) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(TB_GROUP_STRENGTH);
        query.whereEqualTo(CL_GROUP_ID, groupId);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject == null) {
                    ParseObject strengthTable = new ParseObject(TB_GROUP_STRENGTH);
                    strengthTable.put(CL_GROUP_ID, groupId);
                    strengthTable.put(CL_GROUP_STRENGTH, strength);
                    strengthTable.saveInBackground();
                } else {
                    parseObject.put(CL_GROUP_STRENGTH, strength);
                    parseObject.saveInBackground();
                }
            }
        });
    }

    public static ParseQuery<ParseObject> getGroupStrengthQuery(String groupId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TB_GROUP_STRENGTH);
        query.whereEqualTo(CL_GROUP_ID, groupId);
        return query;
    }

    public static void setMemberStatus(final String groupId, final String userId, final UserStatus status) {

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(TB_GROUP_STATUS);
        query.whereEqualTo(CL_GROUP_ID, groupId);
        query.whereEqualTo(CL_USER_ID, userId);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject != null) {
                    parseObject.put(CL_STATUS, status.getStatus());
                    parseObject.saveInBackground();
                } else {
                    ParseObject statusTable = new ParseObject(TB_GROUP_STATUS);
                    statusTable.put(CL_GROUP_ID, groupId);
                    statusTable.put(CL_USER_ID, userId);
                    statusTable.put(CL_STATUS, status.getStatus());
                    statusTable.saveInBackground();
                }
            }
        });
    }

    public static void clearGroup(String groupId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TB_GROUP_STATUS);
        query.whereEqualTo(CL_GROUP_ID, groupId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                deleteAll(list, e);
            }
        });

        query = ParseQuery.getQuery(TB_GROUP_STRENGTH);
        query.whereEqualTo(CL_GROUP_ID, groupId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                deleteAll(list, e);
            }
        });
    }

    private static void deleteAll(List<ParseObject> list, ParseException e) {
        if (e == null) {
            if (list != null && !list.isEmpty()) {
                for (ParseObject obj : list) {
                    obj.deleteInBackground();
                }
            }
        }
    }

    public static void getSong(final String groupId, final GetDataCallback receiveSong) {
        Log.d(TAG, "Get song called");
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TB_SONGS);
        query.whereEqualTo("GroupID", groupId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Found song for id " + groupId);
                    Log.d(TAG, "Number of results " + list.size());
                    ParseObject poObject = (ParseObject) list.get(0);
                    ParseFile songPFFile = (ParseFile) poObject.get("songFile");
                    songPFFile.getDataInBackground(receiveSong);

                } else {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    public static void isChannelSetup(String groupId, final FindCallback<ParseObject> findCallback) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(TB_SONGS);
        query.whereEqualTo("GroupID", groupId);
        query.findInBackground(findCallback);
    }

}
