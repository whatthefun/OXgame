package com.example.user.oxgame;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Draw draw = new Draw(this);
        setContentView(draw);
    }


}
