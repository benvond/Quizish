package com.benvo.quizish.flashcard;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by benvo on 4/17/2017.
 */

public class Card implements Parcelable {

    private String frontText;
    private String backText;
    private boolean isStarred;

    public Card() {
    }

    public Card(String frontText, String backText) {
        this.frontText = frontText;
        this.backText = backText;
        this.isStarred = false;
    }

    public String getFrontText() {
        return frontText;
    }

    public String getBackText() {
        return backText;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void toggleStarred() {
        isStarred = !isStarred;
    }

    public void setFrontText(String frontText) {
        this.frontText = frontText;
    }

    public void setBackText(String backText) {
        this.backText = backText;
    }

    /**
     * All code below was autogenerated by Android Studio to implement Parcelable
     */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(frontText);
        parcel.writeString(backText);
        parcel.writeByte((byte) (isStarred ? 1 : 0));
    }

    protected Card(Parcel in) {
        frontText = in.readString();
        backText = in.readString();
        isStarred = in.readByte() != 0;
    }

    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };
}

