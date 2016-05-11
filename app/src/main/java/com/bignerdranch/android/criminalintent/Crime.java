package com.bignerdranch.android.criminalintent;

import java.util.Date;
import java.util.UUID;

public class Crime {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;
    private String mSuspectPhone;

    public Crime() {
        // Generate unique identifier
        this(UUID.randomUUID());
    }
    public Crime(UUID id) {
        // Generate unique identifier
        mId = id;
        mDate = new Date();
    }
    public String getSuspect(){
        return mSuspect;
    }
    public void setSuspect(String suspect){
        mSuspect=suspect;
    }
    public String getSuspectPhone(){
        return mSuspectPhone;
    }
    public void setSuspectPhone(String suspectPhone){
        mSuspectPhone=suspectPhone;
    }

    public UUID getId() {
        return mId;
    }
    public String getTitle() {
        return mTitle;
    }
    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }
    public void setDate(Date date) {
        mDate = date;
    }
    public boolean isSolved() {
        return mSolved;
    }
    public void setSolved(boolean solved) {
        mSolved = solved;
    }
    public String getPhotoFilename(){
        return "IMG_"+ getId().toString()+".jpg";
    }
}
