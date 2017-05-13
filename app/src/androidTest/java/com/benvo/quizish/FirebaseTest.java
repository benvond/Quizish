package com.benvo.quizish;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import com.benvo.quizish.flashcard.Card;
import com.benvo.quizish.flashcard.Deck;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class FirebaseTest {

    final CountDownLatch writeSignal = new CountDownLatch(1);

    DatabaseReference rootRef;
    DatabaseReference userRef;
    DatabaseReference deckRef;
    ArrayList<Card> cards;
    Deck deck;
    String id;

    @Before
    public void setUp() {
        rootRef = FirebaseDatabase.getInstance().getReference();
        deckRef = rootRef.child("decks");
        userRef = rootRef.child("users");

        Card cardOne = new Card("Front", "Back");
        Card cardTwo = new Card("Front", "Back");
        cards = new ArrayList<>();
        cards.add(cardOne);
        cards.add(cardTwo);
        deck = new Deck("testDeck", cards);

        id = deckRef.push().getKey();
    }

    /**
     * Tests creating a deck that is attached to the first user
     * @throws Exception
     */
    @Test
    public void testCreateDeck() throws Exception {
        // Create a deck for userOne
        deck.setId(id);
        deck.setCreator("userOne");
        deckRef.child(id).setValue(deck).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                writeSignal.countDown();
            }
        });
        writeSignal.await(1, TimeUnit.SECONDS);
        // Add the Firebase generated key to the deck to the decks of userOne
        userRef.child("userOne").child(id).setValue(deck).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                writeSignal.countDown();
            }
        });
        writeSignal.await(1, TimeUnit.SECONDS);
    }

    /**
     * Tests that the user can edit/delete their own deck properly
     * @throws Exception
     */
    @Test
    public void testEditDeck() throws Exception {
        final DatabaseReference userOneRef = rootRef.child("userOne").child(id);

        // Code from stack overflow on how to retrieve an ArrayList from Firebase
        // http://stackoverflow.com/questions/30744224/how-to-retrieve-a-list-object-from-the-firebase-in-android
        userOneRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String, Deck>> t = null;
                if (dataSnapshot != null) {
                    t = new GenericTypeIndicator<HashMap<String, Deck>>() {};
                }
                HashMap<String, Deck> userDeckNames = dataSnapshot.getValue(t);

                // Makes sure the user can have decks
                if (userDeckNames != null) {
                    // If the user is trying to access another users decks, then it will be caught
                    // A user accessing their own decks should pass
                    if (!userDeckNames.containsKey(id)) {
                        fail();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Tests to make sure that a user who did not create the deck (doesn't have it attached to them)
     * cannot edit/delete said deck.
     * @throws Exception
     */
    @Test
    public void testFailEditDeck() throws Exception {
        final DatabaseReference userTwoRef = rootRef.child("userTwo").child(id);

        // Code from stack overflow on how to retrieve an ArrayList from Firebase
        // http://stackoverflow.com/questions/30744224/how-to-retrieve-a-list-object-from-the-firebase-in-android
        userTwoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String, Deck>> t = null;
                if (dataSnapshot != null) {
                    t = new GenericTypeIndicator<HashMap<String, Deck>>() {};
                }
                HashMap<String, Deck> userDeckNames = dataSnapshot.getValue(t);

                // Makes sure the user can have decks
                if (userDeckNames != null) {
                    // If the user is trying to access another users decks, then it will be caught
                    // A user accessing their own decks should pass
                    if (!userDeckNames.containsKey(id)) {
                        fail();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
