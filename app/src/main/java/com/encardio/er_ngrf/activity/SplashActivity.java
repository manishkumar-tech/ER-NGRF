package com.encardio.er_ngrf.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author Sandeep
 */

public class SplashActivity extends AppCompatActivity {

    Animation top, bottom, left, right;
    ImageView iv;
//    TextView tv_splash_1, tv_splash_2, tv_splash_3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        iv = findViewById(R.id.iv);
//        tv_splash_1 = findViewById(R.id.tv_splash_1);
//        tv_splash_2 = findViewById(R.id.tv_splash_2);
//        tv_splash_3 = findViewById(R.id.tv_splash_3);

        top = AnimationUtils.loadAnimation(this, R.anim.top_to_bottom);
        bottom = AnimationUtils.loadAnimation(this, R.anim.bottom_to_top);
        left = AnimationUtils.loadAnimation(this, R.anim.left_to_right);
        right = AnimationUtils.loadAnimation(this, R.anim.right_to_left);

        iv.setAnimation(top);
//        tv_splash_1.setAnimation(bottom);
//        tv_splash_2.setAnimation(bottom);
//        tv_splash_3.setAnimation(bottom);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent result = new Intent(SplashActivity.this, AllPermissionsActivity.class);
                        startActivity(result);
                        finish();

            }
        }, 4000);
    }
}