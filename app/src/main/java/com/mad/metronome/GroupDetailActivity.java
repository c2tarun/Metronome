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

    EditText etGroupId;
    CheckBox cbIsLeader;
    EditText tvGrpStrength;
    Button btnNext;
    View layoutStrength;
    Button btnLogout;
    public static final String GROUP_ID = "Groupid";
    String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        etGroupId = (EditText) findViewById(R.id.groupIdTV);
        cbIsLeader = (CheckBox) findViewById(R.id.leaderCB);
        tvGrpStrength = (EditText) findViewById(R.id.strengthTV);
        btnNext = (Button) findViewById(R.id.nextButton);
        layoutStrength = findViewById(R.id.strengthLayout);
        btnLogout = (Button) findViewById(R.id.logoutButton);

        cbIsLeader.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layoutStrength.setVisibility(View.VISIBLE);
                } else {
                    layoutStrength.setVisibility(View.GONE);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean allGood = true;

                groupId = etGroupId.getText().toString();
                if (groupId.isEmpty()) {
                    allGood = false;
                }

                int groupStrength = 0;
                if (cbIsLeader.isChecked()) {
                    try {
                        groupStrength = Integer.parseInt(tvGrpStrength.getText().toString());
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
                        ParseUtil.setMemberStatus(groupId, ParseUser.getCurrentUser().getUsername(), ParseUtil.UserStatus.NOT_READY);
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
                                ParseUtil.setMemberStatus(groupId, ParseUser.getCurrentUser().getUsername(), ParseUtil.UserStatus.NOT_READY);
                                nextActivity();
                            }
                        }
                    });
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                Intent intent = new Intent(GroupDetailActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    public void nextActivity() {
        Intent intent = new Intent(GroupDetailActivity.this, PlayActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        startActivity(intent);

    }
}
