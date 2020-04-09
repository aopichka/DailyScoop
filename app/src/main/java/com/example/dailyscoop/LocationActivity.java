package com.example.dailyscoop;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class LocationActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item1:
                        Intent a = new Intent(LocationActivity.this,HomeActivity.class);
                        startActivity(a);
                        break;
                    case R.id.item2:
                        break;
                    case R.id.item3:
                        Intent b = new Intent(LocationActivity.this,FlavorActivity.class);
                        startActivity(b);
                        break;
                }
                return false;
            }
        });
    }

    private void logout() {
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(LocationActivity.this, LoginActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutMenu: {
                logout();
                break;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
