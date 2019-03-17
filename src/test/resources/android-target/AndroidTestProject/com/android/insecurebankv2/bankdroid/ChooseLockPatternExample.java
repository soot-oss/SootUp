package com.android.insecurebankv2.bankdroid;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.android.insecurebankv2.R;

public class ChooseLockPatternExample extends Activity implements View.OnClickListener {

    protected static final String TAG = "Settings";

    private static final int REQUESTCODE_CHOOSE = 1;

    private static final long START_DELAY = 1000;

    private View mNextButton;

    private View mSkipButton;

    private View mImageView;

    private AnimationDrawable mAnimation;

    private Runnable mRunnable = new Runnable() {
        public void run() {
            startAnimation(mAnimation);
        }
    };

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_lock_pattern_example);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(mRunnable, START_DELAY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAnimation(mAnimation);
    }

    public void onClick(View v) {
        if (v == mSkipButton) {
            // Canceling, so finish all
            setResult(ChooseLockPattern.RESULT_FINISHED);
            finish();
        } else if (v == mNextButton) {
            stopAnimation(mAnimation);
            Intent intent = new Intent(this, ChooseLockPattern.class);
            startActivityForResult(intent, REQUESTCODE_CHOOSE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUESTCODE_CHOOSE && resultCode == ChooseLockPattern.RESULT_FINISHED) {
            setResult(resultCode);
            finish();
        }
    }

    private void initViews() {
        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(this);

        mSkipButton = findViewById(R.id.skip_button);
        mSkipButton.setOnClickListener(this);

        mImageView = (ImageView) findViewById(R.id.lock_anim);
        mImageView.setBackgroundResource(R.drawable.lock_anim);
        mImageView.setOnClickListener(this);
        mAnimation = (AnimationDrawable) mImageView.getBackground();
    }

    protected void startAnimation(final AnimationDrawable animation) {
        if (animation != null && !animation.isRunning()) {
            animation.run();
        }
    }

    protected void stopAnimation(final AnimationDrawable animation) {
        if (animation != null && animation.isRunning()) {
            animation.stop();
        }
    }
}


