package com.benvo.quizish.activities;

import android.content.Intent;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.benvo.quizish.R;
import com.benvo.quizish.adapters.CardPagerAdapter;
import com.benvo.quizish.flashcard.Card;
import com.benvo.quizish.flashcard.Deck;

import java.util.ArrayList;
import java.util.Collections;

public class DeckViewerActivity extends AppCompatActivity {

    private static final int PAGER_PADDING = 64;
    private static final float PROGRESS_BAR_SCALE = 2f;

    private ViewPager pager;
    private CardPagerAdapter pagerAdapter;
    private ProgressBar deckProgressBar;
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

        // Setting the pager padding and margins to see the last and next card on the sides
        pager.setClipToPadding(false);
        pager.setPaddingRelative(PAGER_PADDING, 0, 0, 0);

        // Creating the progress bar
        deckProgressBar = (ProgressBar) findViewById(R.id.deckProgressBar);
        deckProgressBar.setMax(cards.size()-1);
        deckProgressBar.setScaleY(PROGRESS_BAR_SCALE);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                setProgressBar();
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
        setProgressBar();

        return true;
    }

    /**
     * Sets the progress bar correctly to the fill out how far the user is into the deck.
     */
    private void setProgressBar() {
        deckProgressBar.setMax(pagerAdapter.getCount() - 1);
        // Allows the progress bar to animate for API 24 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            deckProgressBar.setProgress(pager.getCurrentItem(), true);
        } else {
            deckProgressBar.setProgress(pager.getCurrentItem());
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

