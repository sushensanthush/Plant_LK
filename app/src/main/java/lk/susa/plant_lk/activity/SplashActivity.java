package lk.susa.plant_lk.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import lk.susa.plant_lk.R;

public class SplashActivity extends BaseActivity {

    private View dot1, dot2, dot3;
    private AnimatorSet dotSet1, dotSet2, dotSet3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        ImageView logo = findViewById(R.id.splashLogo);
        View greenOverlay = findViewById(R.id.greenOverlay);
        ImageView splashBg = findViewById(R.id.splashBg);

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        dotSet1 = startProfessionalDotAnimation(dot1, 0);
        dotSet2 = startProfessionalDotAnimation(dot2, 250);
        dotSet3 = startProfessionalDotAnimation(dot3, 500);

        Glide.with(this)
                .load(R.drawable.splash_logo_white)
                .into(logo);

        greenOverlay.post(() -> {
            int width = findViewById(R.id.main).getWidth();
            int height = findViewById(R.id.main).getHeight();

            float scale = (float) Math.hypot(width, height) / (greenOverlay.getWidth() / 2f);

            ObjectAnimator overlayScaleX = ObjectAnimator.ofFloat(greenOverlay, "scaleX", 0f, scale);
            ObjectAnimator overlayScaleY = ObjectAnimator.ofFloat(greenOverlay, "scaleY", 0f, scale);

            ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logo, "scaleX", 1f, 1.4f);
            ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logo, "scaleY", 1f, 1.4f);

            ObjectAnimator bgScaleX = ObjectAnimator.ofFloat(splashBg, "scaleX", 1f, 1.2f);
            ObjectAnimator bgScaleY = ObjectAnimator.ofFloat(splashBg, "scaleY", 1f, 1.2f);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(overlayScaleX, overlayScaleY, logoScaleX, logoScaleY, bgScaleX, bgScaleY);
            set.setDuration(800);
            set.start();

            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fadeOutDotsAndNavigate();
                }
            });
        });
    }

    private void fadeOutDotsAndNavigate() {
        View dotLoader = findViewById(R.id.dotLoader);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(dotLoader, "alpha", 1f, 0f);
        fadeOut.setDuration(100);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (dotSet1 != null) dotSet1.cancel();
                if (dotSet2 != null) dotSet2.cancel();
                if (dotSet3 != null) dotSet3.cancel();

                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        });
        fadeOut.start();
    }

    private AnimatorSet startProfessionalDotAnimation(View dot, long startDelay) {
        ObjectAnimator translateY = ObjectAnimator.ofFloat(dot, "translationY", 0f, -15f, 0f);
        translateY.setDuration(900);
        translateY.setRepeatCount(ValueAnimator.INFINITE);
        translateY.setRepeatMode(ValueAnimator.RESTART);
        translateY.setStartDelay(startDelay);
        translateY.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(dot, "scaleX", 0.9f, 1f, 0.9f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(dot, "scaleY", 0.9f, 1f, 0.9f);
        scaleX.setDuration(900);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.RESTART);
        scaleX.setStartDelay(startDelay);
        scaleX.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        scaleY.setDuration(900);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.RESTART);
        scaleY.setStartDelay(startDelay);
        scaleY.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());

        ObjectAnimator alpha = ObjectAnimator.ofFloat(dot, "alpha", 0.7f, 1f, 0.7f);
        alpha.setDuration(900);
        alpha.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatMode(ValueAnimator.RESTART);
        alpha.setStartDelay(startDelay);
        alpha.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.playTogether(translateY, scaleX, scaleY, alpha);
        set.start();
        return set;
    }
}