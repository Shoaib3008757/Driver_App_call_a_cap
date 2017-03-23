package com.texi;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Typeface regularRoboto,boldRoboto;
    TextView tv_newuser,tv_signin;
    RelativeLayout layout_sign_up,layout_sign_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        regularRoboto = Typeface.createFromAsset(getAssets(), getString(R.string.font_regular_roboto));
        boldRoboto=Typeface.createFromAsset(getAssets(), getString(R.string.font_bold_roboto));

        //UI
        tv_newuser=(TextView)findViewById(R.id.tv_newuser);
        tv_newuser.setTypeface(boldRoboto);

        tv_signin=(TextView)findViewById(R.id.tv_signin);
        tv_signin.setTypeface(boldRoboto);

        layout_sign_up = (RelativeLayout)findViewById(R.id.layout_sign_up);
        layout_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent li = new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(li);
            }
        });
        layout_sign_in = (RelativeLayout)findViewById(R.id.layout_sign_in);
        layout_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent li = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(li);
            }
        });


    }

}
