package com.bignerdranch.android.criminalintent;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.UUID;

/**
 * Created by Fulgen on 29/03/2016.
 */
public class CrimePagerActivity extends AppCompatActivity implements CrimeFragment.CallBacks{
    private ViewPager mViewPager;
    private List<Crime> mCrimes;
    private static final String EXTRA_CRIME_ID = "com.bignerdranch.android.criminalintent.crime_id";
    public static final String EXTRA_SUBTITLE_VISIBLE = "com.bignerdranch.android.criminalintent.subtitle_visible";

    public static Intent newIntent(Context packageContext, UUID crimeId, boolean subtitleVisible) {
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        intent.putExtra(EXTRA_SUBTITLE_VISIBLE,subtitleVisible);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        UUID crimeId = (UUID) getIntent()
                .getSerializableExtra(EXTRA_CRIME_ID);


        mViewPager = (ViewPager) findViewById(R.id.activity_crime_pager_view_pager);

        mCrimes = CrimeLab.get(this).getCrimes();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                return CrimeFragment.newInstance(crime.getId(),getIntent()
                        .getBooleanExtra(EXTRA_SUBTITLE_VISIBLE,false));
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        for (int i=0; i<mCrimes.size();i++){
            if(mCrimes.get(i).getId().equals(crimeId)){
                mViewPager.setCurrentItem(i);
                break;
            }
        }

    }
    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        Boolean subtitleVisible = getIntent().getBooleanExtra(EXTRA_SUBTITLE_VISIBLE, false);
        return CrimeListActivity.newIntent(this,subtitleVisible);
    }

    @Override
    public void onCrimeUpdated() {

    }

    @Override
    public void onCrimeDeleted(boolean subtitleVisible) {
        //Phone
        Intent intent = CrimeListActivity.newIntent(this ,subtitleVisible);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();
    }
}
