package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;


public class CrimeListActivity extends SingleFragmentActivity {
    private static final String EXTRA_SUBTITLE_VISIBLE = "com.bignerdranch.android.criminalintent.subtitle_visible";

    @Override
    protected Fragment createFragment() {
        Boolean subtitleVisible = getIntent().getBooleanExtra(EXTRA_SUBTITLE_VISIBLE, false);

        return CrimeListFragment.newInstance(subtitleVisible);
    }
    public static Intent newIntent(Context packageContext, boolean subtitleVisible) {
        Intent intent = new Intent(packageContext, CrimeListActivity.class);
        intent.putExtra(EXTRA_SUBTITLE_VISIBLE, subtitleVisible);

        return intent;
    }


}