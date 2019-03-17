package com.android.insecurebankv2.bankdroid;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.android.insecurebankv2.R;

public class ChooseLockPatternTutorial extends Activity implements View.OnClickListener {

    private static final int REQUESTCODE_EXAMPLE = 1;

    private View mNextButton;

    private View mSkipButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Don't show the tutorial if the user has seen it before.
        LockPatternUtils lockPatternUtils = new LockPatternUtils(this);
        if (savedInstanceState == null && lockPatternUtils.isPatternEverChosen()) {
            Intent intent = new Intent(this, ChooseLockPattern.class);
            startActivity(intent);
            finish();
        } else {
            initViews();
        }
    }

    private void initViews() {
        setContentView(R.layout.choose_lock_pattern_tutorial);
        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(this);
        mSkipButton = findViewById(R.id.skip_button);
        mSkipButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v == mSkipButton) {
            // Canceling, so finish all
            setResult(ChooseLockPattern.RESULT_FINISHED);
            finish();
        } else if (v == mNextButton) {
            startActivityForResult(new Intent(this, ChooseLockPatternExample.class),
                    REQUESTCODE_EXAMPLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUESTCODE_EXAMPLE && resultCode == ChooseLockPattern.RESULT_FINISHED) {
            setResult(resultCode);
            finish();
        }
    }

}


