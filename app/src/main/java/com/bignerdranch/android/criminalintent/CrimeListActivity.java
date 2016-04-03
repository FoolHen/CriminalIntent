package com.bignerdranch.android.criminalintent;

import android.support.v4.app.Fragment;


public class CrimeListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        Boolean subtitleVisible = getIntent().getBooleanExtra(CrimePagerActivity.EXTRA_SUBTITLE_VISIBLE, false);
        return CrimeListFragment.newInstance(subtitleVisible);
    }

}