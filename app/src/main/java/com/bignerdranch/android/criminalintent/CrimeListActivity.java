package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class CrimeListActivity extends SingleFragmentActivity
        implements CrimeListFragment.CallBacks, CrimeFragment.CallBacks{
    private static final String EXTRA_SUBTITLE_VISIBLE =
            "com.bignerdranch.android.criminalintent.subtitle_visible";

    @Override
    protected Fragment createFragment() {
        Boolean subtitleVisible = getIntent().getBooleanExtra(EXTRA_SUBTITLE_VISIBLE, false);

        return CrimeListFragment.newInstance(subtitleVisible);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    public static Intent newIntent(Context packageContext, boolean subtitleVisible) {
        Intent intent = new Intent(packageContext, CrimeListActivity.class);
        intent.putExtra(EXTRA_SUBTITLE_VISIBLE, subtitleVisible);
        return intent;
    }

    @Override
    public void onCrimeSelected(Crime crime,boolean isSubtitleVisible) {
        if (findViewById(R.id.detail_fragment_container)==null){
            //Phone
            Intent intent = CrimePagerActivity.newIntent(this,crime.getId(),isSubtitleVisible);
            intent.putExtra(EXTRA_SUBTITLE_VISIBLE,isSubtitleVisible);
            startActivity(intent);
        }else {
            //Tablet
            Fragment newDetail = CrimeFragment.newInstance(crime.getId(),isSubtitleVisible);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container,newDetail)
                    .commit();
            onCrimeUpdated();
        }
    }

    @Override
    public void onCrimeUpdated() {
        CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }

    @Override
    public void onCrimeDeleted(boolean subtitleVisible) {
        //Tablet
        CrimeFragment fragment = (CrimeFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detail_fragment_container);
        getSupportFragmentManager().beginTransaction()
                .remove(fragment)
                .commit();
    }
}