package com.dgistalk.dgtalk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dgistalk.dgtalk.Fragment.PeopleFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new PeopleFragment()).commit(); // main activity 안의 frame_layout을 fragment로 채우는 코드.
    }
}
