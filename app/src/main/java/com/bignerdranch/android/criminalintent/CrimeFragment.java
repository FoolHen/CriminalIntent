package com.bignerdranch.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment{

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String ARG_SUBTITLE_VISIBLE = "subtitle_visible";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME =1;
    private static final int REQUEST_CONTACT =2;

    private static final int REQUEST_PERMISSION = 3;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallButton;

    public static CrimeFragment newInstance(UUID crimeId,boolean subtitleVisible){
        Bundle args =  new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        args.putSerializable(ARG_SUBTITLE_VISIBLE, subtitleVisible);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId =(UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This space intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This one too
            }
        });

        mDateButton = (Button)v.findViewById(R.id.crime_date);

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this,REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this,REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Set the crime's solved property
                mCrime.setSolved(isChecked);
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setChooserTitle(getString(R.string.send_report))
                        .setSubject(getString(R.string.crime_report_subject))
                        .createChooserIntent();
                startActivity(i);

                /*
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i,getString(R.string.send_report));
                startActivity(i);
                */
            }
        });
        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_CONTACTS);
                if (permissionCheck!=PackageManager.PERMISSION_GRANTED){
                    CrimeFragment.this.requestPermissions(new String[]
                                    {Manifest.permission.READ_CONTACTS},REQUEST_PERMISSION);
                } else {
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }
            }
        });

        if (mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }
        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY)==null){
            mSuspectButton.setEnabled(false);
        }


        updateDateAndTime();

        mCallButton = (Button) v.findViewById(R.id.crime_call);
        mCallButton.setText(R.string.suspect_call_text);

        if(mCrime.getSuspect()!=null){
            updateCallButton();

        }else{
            mCallButton.setAlpha(.5f);
            mCallButton.setEnabled(false);

        }
        mCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri contentUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String selectClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";

                String[] fields = {ContactsContract.CommonDataKinds.Phone.NUMBER};
                String[] selectParams = {mCrime.getSuspectPhone()};

                Cursor cursor = getActivity().getContentResolver().query(contentUri, fields, selectClause, selectParams, null);

                if(cursor != null && cursor.getCount() > 0){
                    try{
                        cursor.moveToFirst();
                        String number = cursor.getString(0);

                        Uri phoneNumber = Uri.parse("tel:" + number);
                        Intent intent = new Intent(Intent.ACTION_DIAL, phoneNumber);
                        startActivity(intent);
                    }finally{
                        cursor.close();
                    }
                }
            }
        });


        return v;
    }
    private void updateCallButton(){
        mCallButton.setEnabled(true);
        mCallButton.setAlpha(1f);

    }

    private void updateDateAndTime() {
        mDateButton.setText(DateFormat.format("EEEE, MMM dd, yyyy", mCrime.getDate()).toString());
        mTimeButton.setText(DateFormat.format("h:mm a", mCrime.getDate()).toString());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!= Activity.RESULT_OK){
            return;
        }
        if(requestCode==REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
        }
        else if(requestCode == REQUEST_TIME) {
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);

            Calendar newInfo = Calendar.getInstance();
            newInfo.setTime(date);

            Calendar cal = Calendar.getInstance();
            cal.setTime(mCrime.getDate());

            cal.set(Calendar.HOUR_OF_DAY, newInfo.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, newInfo.get(Calendar.MINUTE));
            mCrime.setDate(cal.getTime());
        }else if (requestCode==REQUEST_CONTACT && data!=null){
            Uri contactUri = data.getData();
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME ,ContactsContract.Contacts._ID};
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri,queryFields,null,null,null);
            if(c==null){
                return;
            }
            try {
                if (c.getCount() ==0){
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(0);
                String number = c.getString(1);
                mCrime.setSuspect(suspect);
                mCrime.setSuspectPhone(number);
                mSuspectButton.setText(suspect);

                updateCallButton();
            }finally {
                c.close();
            }/*
            /*if(mCrime.getSuspect()==null){
                return;
            Uri commonDataKindPhoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            queryFields = new String[]{
                    ContactsContract.CommonDataKinds.Phone.NUMBER};
            c = getActivity().getContentResolver().query(commonDataKindPhoneUri,
                    queryFields,null ,null , null);
            if (c == null) {
                return;
            }

            try {
                if (c.getCount() > 0) {
                    c.moveToFirst();

                    String phoneNumber = c.getString(
                            c.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                    /*mCrime.getSuspect().setPhoneNumber(phoneNumber);
                    CrimeLab.getInstance(getContext()).updateSuspect(mCrime.getSuspect());

                    mDialButton.setText(phoneNumber);
                }
            } finally {
                c.close();
            }*/


        }
        updateDateAndTime();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menu_item_delete_crime){
            CrimeLab.get(getActivity()).deleteCrime(mCrime);

            boolean subtitleVisible =  getArguments().getBoolean(ARG_SUBTITLE_VISIBLE,false);
            Intent intent = CrimeListActivity.newIntent(getActivity() ,subtitleVisible);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        }
    }

    private String getCrimeReport(){
        String solvedString = null;
        if(mCrime.isSolved()){
            solvedString=getString(R.string.crime_report_solved);
        }
        else solvedString=getString(R.string.crime_report_unsolved);

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null){
            suspect = getString(R.string.crime_report_no_suspect);
        }
        else suspect = getString(R.string.crime_report_suspect,suspect);

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);
        return report;
    }

}
