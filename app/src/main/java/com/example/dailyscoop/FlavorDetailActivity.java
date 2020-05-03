package com.example.dailyscoop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

public class FlavorDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flavor_detail);


        // Get the flavor name from the intent
        String flavorName;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                flavorName= null;
            } else {
                flavorName= extras.getString("Name");
            }
        } else {
            flavorName= (String) savedInstanceState.getSerializable("Name");
        }

        // Update title of the flavor name
        TextView title = (TextView) findViewById(R.id.flav1);
        title.setText(flavorName);

        flavorName = getPathForFlavor(flavorName);
        updatePicture(flavorName);


        // TODO Some functionality to retrieve restaurants that have this flavor

        // TODO Input this data in an arrayList that is passed into the listView

        // Construct the data source
        ArrayList<String> restaurants = new ArrayList<>();
        // Create the adapter to convert the array to views
        UsersAdapter adapter = new UsersAdapter(this, restaurants);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);


        BottomNavigationView navigation;
        navigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item1:
                        Intent a = new Intent(FlavorDetailActivity.this, HomeActivity.class);
                        finish();
                        startActivity(a);
                        break;
                    case R.id.item2:
                        Intent b = new Intent(FlavorDetailActivity.this, LocationActivity.class);
                        finish();
                        startActivity(b);
                        break;
                    case R.id.item3:
                        Intent c = new Intent(FlavorDetailActivity.this, FlavorActivity.class);
                        finish();
                        startActivity(c);
                        break;
                }
                return false;
            }
        });
    }


    private String getPathForFlavor(String name) {
        String pathToImage = name.toLowerCase();
        pathToImage = pathToImage.trim();
        pathToImage = pathToImage.replaceAll(" ", "");
        pathToImage = pathToImage.replaceAll("’", "");

        return pathToImage;
    }


    private void updatePicture(String newImage) {

        String uri = newImage;

        int imageResource = getResources().getIdentifier(uri, "drawable", getPackageName()); //get image  resource

        Drawable res = getResources().getDrawable(imageResource); // convert into drawble

        ImageView img = findViewById(R.id.img_rest1);

        if (img != null) {
            img.setImageDrawable(res); // set as image
        }

    }

    public class UsersAdapter extends ArrayAdapter<String> {
        public UsersAdapter(Context context, ArrayList<String> users) {
            super(context, 0, users);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            String restaurant = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.flavor_details_list_view, parent, false);
            }
            // Lookup view for data population
            TextView firstLine = (TextView) convertView.findViewById(R.id.firstLine);
            TextView secondLine = (TextView) convertView.findViewById(R.id.secondLine);
            // Populate the data into the template view using the data object
            //firstLine.setText();
            //secondLine.setText();
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
