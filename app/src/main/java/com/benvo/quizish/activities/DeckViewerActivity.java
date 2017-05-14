package com.benvo.quizish.activities;

import android.content.Intent;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.benvo.quizish.R;
import com.benvo.quizish.adapters.CardPagerAdapter;
import com.benvo.quizish.flashcard.Card;
import com.benvo.quizish.flashcard.Deck;

import java.util.ArrayList;
import java.util.Collections;

public class DeckViewerActivity extends AppCompatActivity {

    private ViewPager pager;
    private CardPagerAdapter pagerAdapter;
    private SeekBar deckSeekBar;
    private ArrayList<Card> cards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_viewer);

        Intent intent = getIntent();
        Deck deck = intent.getParcelableExtra(MainActivity.DECK);
        cards = deck.getCards();
        this.setTitle(deck.getName());

        // Creating the ViewPager with the CardAdapter
        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new CardPagerAdapter(getFragmentManager(), cards);
        pager.setAdapter(pagerAdapter);

        // Creating the seek bar
        deckSeekBar = (SeekBar) findViewById(R.id.deckSeekBar);
        deckSeekBar.setMax(cards.size()-1);

        // Change the seek bar progress when cards are swiped
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                setDeckSeekBar();
            }
        });

        // Change the current card when the seek bar is dragged
        deckSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                pager.setCurrentItem(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.deck_viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.shuffleButton:
                pager.setAdapter(null);
                Collections.shuffle(cards);
                pagerAdapter = new CardPagerAdapter(getFragmentManager(), cards);
                pager.setAdapter(pagerAdapter);
                break;
            case R.id.starButton:
                toggleStarred();
                break;
            case R.id.backButton:
                toggleBackside();
                break;
        }

        // Reset the progress bar to display at the beginning
        setDeckSeekBar();

        return true;
    }

    /**
     * Sets the seek bar correctly to the fill out how far the user is into the deck.
     */
    private void setDeckSeekBar() {
        deckSeekBar.setMax(pagerAdapter.getCount() - 1);
        // Allows the progress bar to animate for API 24 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            deckSeekBar.setProgress(pager.getCurrentItem(), true);
        } else {
            deckSeekBar.setProgress(pager.getCurrentItem());
        }
    }

    /**
     * Resets the ViewPager to only display the starred cards
     */
    private void toggleStarred() {
        if (pagerAdapter.isShowingStarred()) {
            pager.setAdapter(null);
            pagerAdapter = new CardPagerAdapter(getFragmentManager(), cards);
            pager.setAdapter(pagerAdapter);
        } else {
            ArrayList<Card> starredCards = pagerAdapter.getStarredCards();
            if (starredCards.size() > 0) {
                pager.setAdapter(null);
                pagerAdapter = new CardPagerAdapter(getFragmentManager(), starredCards);
                pager.setAdapter(pagerAdapter);
            } else {
                Toast.makeText(this, "There are no starred cards.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Resets the ViewPager to display the backs of cards first
     */
    private void toggleBackside() {
        pager.setAdapter(null);
        for (Card card : cards) {
            String oldFront = card.getFrontText();
            card.setFrontText(card.getBackText());
            card.setBackText(oldFront);
        }
        pagerAdapter = new CardPagerAdapter(getFragmentManager(), cards);
        pager.setAdapter(pagerAdapter);
    }
}

