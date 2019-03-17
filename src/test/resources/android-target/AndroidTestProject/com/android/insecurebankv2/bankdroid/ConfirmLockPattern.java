package com.android.insecurebankv2.bankdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.insecurebankv2.R;

import java.util.List;

/**
 * Launch this when you want the user to confirm their lock pattern.
 *
 * Sets an activity result of {@link Activity#RESULT_OK} when the user
 * successfully confirmed their pattern.
 */
public class ConfirmLockPattern extends Activity {

    /**
     * Names of {@link CharSequence} fields within the originating {@link Intent}
     * that are used to configure the keyguard confirmation view's labeling.
     * The view will use the system-defined resource strings for any labels that
     * the caller does not supply.
     */
    public static final String HEADER_TEXT = "com.android.insecurebankv2.bankdroid.header";

    public static final String FOOTER_TEXT = "com.android.insecurebankv2.bankdroid.footer";

    public static final String HEADER_WRONG_TEXT = "com.android.insecurebankv2.bankdroid.header_wrong";

    public static final String FOOTER_WRONG_TEXT = "com.android.insecurebankv2.bankdroid.footer_wrong";

    // how long we wait to clear a wrong pattern
    private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 2000;

    private static final String KEY_NUM_WRONG_ATTEMPTS = "num_wrong_attempts";

    private LockPatternView mLockPatternView;

    private LockPatternUtils mLockPatternUtils;

    private int mNumWrongConfirmAttempts;

    private CountDownTimer mCountdownTimer;

    private TextView mHeaderTextView;

    private TextView mFooterTextView;

    // caller-supplied text for various prompts
    private CharSequence mHeaderText;

    private CharSequence mFooterText;

    private CharSequence mHeaderWrongText;

    private CharSequence mFooterWrongText;

    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };

    /**
     * The pattern listener that responds according to a user confirming
     * an existing lock pattern.
     */
    private LockPatternView.OnPatternListener mConfirmExistingLockPatternListener
            = new LockPatternView.OnPatternListener() {

        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            if (mLockPatternUtils.checkPattern(pattern)) {
                setResult(RESULT_OK);
                finish();
            } else {
                if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL &&
                        ++mNumWrongConfirmAttempts
                                >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
                    long deadline = mLockPatternUtils.setLockoutAttemptDeadline();
                    handleAttemptLockout(deadline);
                } else {
                    updateStage(Stage.NeedToUnlockWrong);
                    postClearPatternRunnable();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLockPatternUtils = new LockPatternUtils(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.confirm_lock_pattern);

        mHeaderTextView = (TextView) findViewById(R.id.headerText);
        mLockPatternView = (LockPatternView) findViewById(R.id.lockPattern);
        mFooterTextView = (TextView) findViewById(R.id.footerText);

        // make it so unhandled touch events within the unlock screen go to the
        // lock pattern view.
        final LinearLayoutWithDefaultTouchRecepient topLayout
                = (LinearLayoutWithDefaultTouchRecepient) findViewById(
                R.id.topLayout);
        topLayout.setDefaultTouchRecepient(mLockPatternView);

        Intent intent = getIntent();
        if (intent != null) {
            mHeaderText = intent.getCharSequenceExtra(HEADER_TEXT);
            mFooterText = intent.getCharSequenceExtra(FOOTER_TEXT);
            mHeaderWrongText = intent.getCharSequenceExtra(HEADER_WRONG_TEXT);
            mFooterWrongText = intent.getCharSequenceExtra(FOOTER_WRONG_TEXT);
        }

        mLockPatternView.setTactileFeedbackEnabled(mLockPatternUtils.isTactileFeedbackEnabled());
        mLockPatternView.setInStealthMode(!mLockPatternUtils.isVisiblePatternEnabled());
        mLockPatternView.setOnPatternListener(mConfirmExistingLockPatternListener);
        updateStage(Stage.NeedToUnlock);

        if (savedInstanceState != null) {
            mNumWrongConfirmAttempts = savedInstanceState.getInt(KEY_NUM_WRONG_ATTEMPTS);
        } else {
            // on first launch, if no lock pattern is set, then finish with
            // success (don't want user to get stuck confirming something that
            // doesn't exist).
            if (!mLockPatternUtils.savedPatternExists()) {
                setResult(RESULT_OK);
                finish();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // deliberately not calling super since we are managing this in full
        outState.putInt(KEY_NUM_WRONG_ATTEMPTS, mNumWrongConfirmAttempts);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCountdownTimer != null) {
            mCountdownTimer.cancel();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // if the user is currently locked out, enforce it.
        long deadline = mLockPatternUtils.getLockoutAttemptDeadline();
        if (deadline != 0) {
            handleAttemptLockout(deadline);
        }
    }

    private void updateStage(Stage stage) {

        switch (stage) {
            case NeedToUnlock:
                if (mHeaderText != null) {
                    mHeaderTextView.setText(mHeaderText);
                } else {
                    mHeaderTextView.setText(R.string.lockpattern_need_to_unlock);
                }
                if (mFooterText != null) {
                    mFooterTextView.setText(mFooterText);
                } else {
                    mFooterTextView.setText(R.string.lockpattern_need_to_unlock_footer);
                }

                mLockPatternView.setEnabled(true);
                mLockPatternView.enableInput();
                break;
            case NeedToUnlockWrong:
                if (mHeaderWrongText != null) {
                    mHeaderTextView.setText(mHeaderWrongText);
                } else {
                    mHeaderTextView.setText(R.string.lockpattern_need_to_unlock_wrong);
                }
                if (mFooterWrongText != null) {
                    mFooterTextView.setText(mFooterWrongText);
                } else {
                    mFooterTextView.setText(R.string.lockpattern_need_to_unlock_wrong_footer);
                }

                mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                mLockPatternView.setEnabled(true);
                mLockPatternView.enableInput();
                break;
            case LockedOut:
                mLockPatternView.clearPattern();
                // enabled = false means: disable input, and have the
                // appearance of being disabled.
                mLockPatternView.setEnabled(false); // appearance of being disabled
                break;
        }
    }

    // clear the wrong pattern unless they have started a new one
    // already
    private void postClearPatternRunnable() {
        mLockPatternView.removeCallbacks(mClearPatternRunnable);
        mLockPatternView.postDelayed(mClearPatternRunnable, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
    }

    private void handleAttemptLockout(long elapsedRealtimeDeadline) {
        updateStage(Stage.LockedOut);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        mCountdownTimer = new CountDownTimer(
                elapsedRealtimeDeadline - elapsedRealtime,
                LockPatternUtils.FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS) {

            @Override
            public void onTick(long millisUntilFinished) {
                mHeaderTextView
                        .setText(R.string.lockpattern_too_many_failed_confirmation_attempts_header);
                final int secondsCountdown = (int) (millisUntilFinished / 1000);
                mFooterTextView.setText(getString(
                        R.string.lockpattern_too_many_failed_confirmation_attempts_footer,
                        secondsCountdown));
            }

            @Override
            public void onFinish() {
                mNumWrongConfirmAttempts = 0;
                updateStage(Stage.NeedToUnlock);
            }
        }.start();
    }

    @Override
    public void finish() {
        super.finish();
        //Helpers.setActivityAnimation(this, R.anim.zoom_enter, R.anim.zoom_exit);
    }

    private enum Stage {
        NeedToUnlock,
        NeedToUnlockWrong,
        LockedOut
    }
}
