package com.mad.metronome;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class GroupDetailActivity extends AppCompatActivity {

    EditText groupIdET;
    CheckBox leaderCB;
    EditText strengthTV;
    Button nextButton;
    View strengthLayout;
    public static final String GROUP_ID = "Groupid";
    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        groupIdET = (EditText) findViewById(R.id.groupIdTV);
        leaderCB = (CheckBox) findViewById(R.id.leaderCB);
        strengthTV = (EditText) findViewById(R.id.strengthTV);
        nextButton = (Button) findViewById(R.id.nextButton);
        strengthLayout = findViewById(R.id.strengthLayout);

        leaderCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    strengthLayout.setVisibility(View.VISIBLE);
                } else {
                    strengthLayout.setVisibility(View.GONE);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean allGood = true;

                groupId = groupIdET.getText().toString();
                if (groupId.isEmpty()) {
                    allGood = false;
                }

                int groupStrength = 0;
                if (leaderCB.isChecked()) {
                    try {
                        groupStrength = Integer.parseInt(strengthTV.getText().toString());
                    } catch (NumberFormatException e) {
                        Toast.makeText(GroupDetailActivity.this, "Enter valid strength", Toast.LENGTH_LONG).show();
                        allGood = false;
                    }
                    if (groupStrength <= 0) {
                        Toast.makeText(GroupDetailActivity.this, "Enter valid strength", Toast.LENGTH_LONG).show();
                        allGood = false;
                    }
                    if (allGood) {
                        ParseUtil.updateStrengthTable(groupId, groupStrength);
                        ParseUtil.insertMemberStatus(ParseUser.getCurrentUser().getUsername(), groupId, "NR");
                        nextActivity();
                    }

                } else {
                    ParseQuery<ParseObject> query = ParseUtil.getGroupStrengthQuery(groupId);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (parseObject == null || e != null) {
                                Toast.makeText(GroupDetailActivity.this, "Wait for Leader to log in", Toast.LENGTH_SHORT).show();
                            } else {
                                ParseUtil.insertMemberStatus(groupId, ParseUser.getCurrentUser().getUsername(), "NR");
                                nextActivity();
                            }
                        }
                    });
                }
//                ParseUtil.getSong(groupId,GroupDetailActivity.this);
            }
        });
    }

    public void nextActivity() {
        Intent intent = new Intent(GroupDetailActivity.this, PlayActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        startActivity(intent);

    }
}
