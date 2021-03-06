package com.benvo.quizish.flashcard;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by benvo on 4/17/2017.
 */

public class Deck implements Parcelable {

    private String name;
    private ArrayList<Card> cards;
    private String id;
    private String creator;
    private String creatorImageURL;

    public Deck() {}

    public Deck(String name, ArrayList<Card> cards) {
        this.name = name;
        this.cards = cards;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatorImageURL() {
        return creatorImageURL;
    }

    public void setCreatorImageURL(String creatorImage) {
        this.creatorImageURL = creatorImage;
    }

    /**
     * All code below was autogenerated by Android Studio to implement Parcelable
     */
    protected Deck(Parcel in) {
        name = in.readString();
        cards = in.createTypedArrayList(Card.CREATOR);
        id = in.readString();
    }

    public static final Creator<Deck> CREATOR = new Creator<Deck>() {
        @Override
        public Deck createFromParcel(Parcel in) {
            return new Deck(in);
        }

        @Override
        public Deck[] newArray(int size) {
            return new Deck[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeTypedList(cards);
        parcel.writeString(id);
    }
}
