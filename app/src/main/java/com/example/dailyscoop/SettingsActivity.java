package com.example.dailyscoop;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class SettingsActivity  extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set up the firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item1:
                        Intent c = new Intent(SettingsActivity.this,HomeActivity.class);
                        finish();
                        startActivity(c);
                        break;
                    case R.id.item2:
                        Intent a = new Intent(SettingsActivity.this,LocationActivity.class);
                        finish();
                        startActivity(a);
                        break;
                    case R.id.item3:
                        Intent b = new Intent(SettingsActivity.this,FlavorActivity.class);
                        finish();
                        startActivity(b);
                        break;
                }
                return false;
            }
        });

        Switch sw = (Switch) findViewById(R.id.switchNotifications);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    turnOnNotifications();
                } else {
                    // The toggle is disabled
                    turnOffNotifications();
                }
            }
        });

        // retrieve shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.dailyscoop", Context.MODE_PRIVATE);
        if(sharedPreferences.contains("dailyNotifications")){
            sw.setChecked(sharedPreferences.getBoolean("dailyNotifications", false));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutMenu:
                logout();
                break;
            case R.id.settingsMenu:
                startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
    }

    private void turnOnNotifications(){
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        // Set the alarm to start at approximately 8:00 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 8);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        // alarm will stay enabled even if the device resets
        ComponentName receiver = new ComponentName(this, AlarmReceiver.class);
        PackageManager pm = this.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        // update sharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.dailyscoop", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("dailyNotifications", true).apply();
    }

    private void turnOffNotifications(){
        // If the alarm has been set, cancel it.
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
            ComponentName receiver = new ComponentName(this, AlarmReceiver.class);
            PackageManager pm = this.getPackageManager();

            // used for device restarts
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }

        // update sharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("com.example.dailyscoop", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("dailyNotifications", false).apply();
    }

    public void SendTestNotification(View v){
        Intent intent = new Intent(this, AlarmReceiver.class);
        new AlarmReceiver().getClosestLocation(this, intent);
    }
}
