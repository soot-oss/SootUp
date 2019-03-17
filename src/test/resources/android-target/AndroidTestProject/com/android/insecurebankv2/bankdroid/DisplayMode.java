package com.android.insecurebankv2.bankdroid;


/**
 * How to display the current pattern.
 */
public enum DisplayMode {

    /**
     * The pattern drawn is correct (i.e draw it in a friendly color)
     */
    Correct,

    /**
     * Animate the pattern (for demo, and help).
     */
    Animate,

    /**
     * The pattern is wrong (i.e draw a foreboding color)
     */
    Wrong
}
