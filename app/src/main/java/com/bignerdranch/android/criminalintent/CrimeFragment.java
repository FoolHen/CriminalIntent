package com.bignerdranch.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
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
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CrimeFragment extends Fragment{

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String ARG_SUBTITLE_VISIBLE = "subtitle_visible";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_IMAGE = "DialogImage";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME =1;
    private static final int REQUEST_CONTACT =2;
    private static final int REQUEST_TAKE_PHOTO = 3;
    private static final int REQUEST_SELECT_PHOTO = 4;

    private static final int REQUEST_PERMISSION_READ_CONTACTS = 5;
    private static final int REQUEST_PERMISSION_READ_EXT_STORAGE = 6;
    private static final int REQUEST_PERMISSION_WRITE_EXT_STORAGE = 7;


    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallButton;
    private ImageButton mTakePhotoButton;
    private ImageButton mChoosePhotoButton;
    private ImageView mPhotoView;
    private Point mPhotoSize;
    private CallBacks mCallbacks;

    /* Interfaz para que la actividad se encarge de los fragmentos
        para que estos sean totalmente independientes
     */
    public interface CallBacks{
        void onCrimeUpdated(Crime crime);
        //void onCrimeDeleted(boolean subtitleVisible);
    }


    public static CrimeFragment newInstance(UUID crimeId,boolean subtitleVisible){
        Bundle args =  new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        args.putSerializable(ARG_SUBTITLE_VISIBLE, subtitleVisible);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks= (CallBacks) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId =(UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        setHasOptionsMenu(true);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks=null;
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
                updateCrime();
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
                updateCrime();
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mPhotoFile!=null){
                    Intent i = ShareCompat.IntentBuilder.from(getActivity())
                            .setType("image/jpeg")
                            .setText(getCrimeReport())
                            .setChooserTitle(getString(R.string.send_report))
                            .setSubject(getString(R.string.crime_report_subject))
                            .setStream(Uri.fromFile(mPhotoFile))
                            .createChooserIntent();
                    startActivity(i);
                }
                else {
                    Intent i = ShareCompat.IntentBuilder.from(getActivity())
                            .setType("text/plain")
                            .setText(getCrimeReport())
                            .setChooserTitle(getString(R.string.send_report))
                            .setSubject(getString(R.string.crime_report_subject))
                            .createChooserIntent();
                    startActivity(i);
                }


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
                                    {Manifest.permission.READ_CONTACTS},REQUEST_PERMISSION_READ_CONTACTS);
                } else {
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }
            }
        });

        if (mCrime.getSuspect() != null){
            mSuspectButton.setText(mCrime.getSuspect());
        }
        final PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY)==null){
            mSuspectButton.setEnabled(false);
        }


        updateDateAndTime();

        mCallButton = (Button) v.findViewById(R.id.crime_call);
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
        mTakePhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = mPhotoFile!=null &&
                captureImage.resolveActivity(packageManager)!=null;
        if (canTakePhoto){
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        }
        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionCheck!=PackageManager.PERMISSION_GRANTED){
                    CrimeFragment.this.requestPermissions(new String[]
                            {Manifest.permission.READ_EXTERNAL_STORAGE}
                            ,REQUEST_PERMISSION_READ_EXT_STORAGE);
                } else {

                    startActivityForResult(captureImage,REQUEST_TAKE_PHOTO);
                }
            }
        });

        mChoosePhotoButton = (ImageButton) v.findViewById(R.id.crime_gallery);
        mChoosePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck!=PackageManager.PERMISSION_GRANTED){
                    CrimeFragment.this.requestPermissions(new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE}
                            ,REQUEST_PERMISSION_WRITE_EXT_STORAGE);
                } else {
                    final Intent chooseImage = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    chooseImage.setType("image/*");
                    startActivityForResult(chooseImage, REQUEST_SELECT_PHOTO);
                }
            }
        });

        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        final ViewTreeObserver observer = mPhotoView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPhotoSize = new Point();
                mPhotoSize.set(mPhotoView.getWidth(),mPhotoView.getHeight());
                updatePhotoView();
                mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPhotoFile != null && mPhotoFile.exists()) {
                    FragmentManager manager = getFragmentManager();
                    ImageFragment imageFragment = ImageFragment.newInstance(Uri.fromFile(mPhotoFile));
                    imageFragment.show(manager, DIALOG_IMAGE);
                }
            }
        });



        return v;
    }

    //Updaters
    private void updateCallButton(){
        mCallButton.setEnabled(true);
        mCallButton.setAlpha(1f);

    }
    private void updateDateAndTime() {
        mDateButton.setText(DateFormat.format("EEEE, MMM dd, yyyy", mCrime.getDate()).toString());
        mTimeButton.setText(DateFormat.format("h:mm a", mCrime.getDate()).toString());
    }
    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = (mPhotoView == null) ?
                    PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity()) :
                    PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoSize.x, mPhotoSize.y);
            mPhotoView.setImageBitmap(bitmap);;
        }
    }
    private void updateCrime(){
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }

    //Results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!= Activity.RESULT_OK){
            return;
        }
        switch (requestCode){
            case REQUEST_DATE:
                Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                mCrime.setDate(date);
                updateCrime();

                break;
            case REQUEST_TIME:
                Date date2 = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);

                Calendar newInfo = Calendar.getInstance();
                newInfo.setTime(date2);

                Calendar cal = Calendar.getInstance();
                cal.setTime(mCrime.getDate());

                cal.set(Calendar.HOUR_OF_DAY, newInfo.get(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, newInfo.get(Calendar.MINUTE));
                mCrime.setDate(cal.getTime());
                updateCrime();
                break;
            case REQUEST_CONTACT:
                if (data==null) break;
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
                    updateCrime();
                    mSuspectButton.setText(suspect);

                    updateCallButton();
                }finally {
                    c.close();
                }
                break;
            case REQUEST_TAKE_PHOTO:
                updatePhotoView();
                updateCrime();
                break;
            case REQUEST_SELECT_PHOTO:

                try {
                    // When an Image is picked
                    if (null != data) {
                        // Get the Image from data

                        Uri selectedImage = data.getData();
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };

                        // Get the cursor
                        Cursor cursor = getContext().getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);
                        // Move to first row
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String imgDecodableString = cursor.getString(columnIndex);
                        File file = new File(imgDecodableString);
                        copy(file,mPhotoFile);
                        cursor.close();
                        //ImageView imgView = (ImageView) findViewById(R.id.imgView);
                        // Set the Image in ImageView after decoding the String
                        //mPhotoView.setImageBitmap(BitmapFactory
                        //        .decodeFile(imgDecodableString));
                        updatePhotoView();
                        updateCrime();
                    } else {
                    }
                } catch (Exception e) {
                }




                /*if (data==null) break;
                Uri uri = data.getData();


                File file = new File(uri.getPath());
                Bitmap bitmap = PictureUtils.getScaledBitmap(
                        file.getPath(), getActivity());
                mPhotoView.setImageBitmap(bitmap);
                Log.d("FULGEN","mPhotoFile no es null");

                break;*/
            default:
                break;
        }
        updateDateAndTime();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_READ_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }
                break;
            case REQUEST_PERMISSION_READ_EXT_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    //startActivityForResult(pickContact, REQUEST_CONTACT);

                    final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    PackageManager packageManager = getActivity().getPackageManager();

                    boolean canTakePhoto = mPhotoFile!=null &&
                            captureImage.resolveActivity(packageManager)!=null;
                    if (canTakePhoto){
                        Uri uri = Uri.fromFile(mPhotoFile);
                        captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                    }
                    startActivityForResult(captureImage,REQUEST_TAKE_PHOTO);
                }
                break;
            case REQUEST_PERMISSION_WRITE_EXT_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    final Intent chooseImage = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    chooseImage.setType("image/*");
                    startActivityForResult(chooseImage, REQUEST_SELECT_PHOTO);
                }break;

            default: break;


        }

    }

    //Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menu_item_delete_crime){
            if (mPhotoFile!=null&&mPhotoFile.exists()){
                mPhotoFile.delete();
            }
            CrimeLab.get(getActivity()).deleteCrime(mCrime);

            boolean subtitleVisible =  getArguments().getBoolean(ARG_SUBTITLE_VISIBLE,false);
            /*Intent intent = CrimeListActivity.newIntent(getActivity() ,subtitleVisible);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
            */
            updateCrime();
            //mCallbacks.onCrimeDeleted(subtitleVisible);

        }
        return super.onOptionsItemSelected(item);
    }

    //Other methods

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
    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
