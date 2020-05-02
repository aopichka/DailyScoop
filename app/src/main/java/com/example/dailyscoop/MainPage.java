package com.example.dailyscoop;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

public class MainPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
    }


    void updatePicture(String newImage, int index){

        String uri = "@drawable/" + newImage;

        int imageResource = getResources().getIdentifier(uri, null, getPackageName()); //get image  resource

        Drawable res = getResources().getDrawable(imageResource); // convert into drawble

        if(index == 0){
            ImageView img = findViewById(R.id.imageView);
            img.setImageDrawable(res); // set as image
        }
        if(index == 1){
            ImageView img = findViewById(R.id.imageView2);
            img.setImageDrawable(res); // set as image
        }
        if(index == 2){
            ImageView img = findViewById(R.id.imageView3);
            img.setImageDrawable(res); // set as image
        }
        if(index == 3){
            ImageView img = findViewById(R.id.imageView4);
            img.setImageDrawable(res); // set as image
        }
        if(index == 4){
            ImageView img = findViewById(R.id.imageView5);
            img.setImageDrawable(res); // set as image
        }
        if(index == 5){
            ImageView img = findViewById(R.id.imageView6);
            img.setImageDrawable(res); // set as image
        }
        if(index == 6){
            ImageView img = findViewById(R.id.imageView7);
            img.setImageDrawable(res); // set as image
        }
    }

}
