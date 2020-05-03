package com.example.dailyscoop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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


        // TODO Some functionality to retrieve restaurants that have this flavor

        // TODO Input this data in an arrayList that is passed into the listView

        // Construct the data source
        ArrayList<String> restaurants = new ArrayList<>();
        // Create the adapter to convert the array to views
        UsersAdapter adapter = new UsersAdapter(this, restaurants);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.mobile_list);
        listView.setAdapter(adapter);
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
