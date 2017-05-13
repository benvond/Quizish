package com.benvo.quizish.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.benvo.quizish.R;
import com.benvo.quizish.adapters.CardCreatorAdapter;
import com.benvo.quizish.flashcard.Card;

import java.util.ArrayList;

public class DeckCreatorActivity extends AppCompatActivity {

    public static final String CARDS = "CARDS";

    private ArrayList<Card> cards;
    private String deckId;
    private TextView noCardsText;
    private RecyclerView cardRecycler;
    private CardCreatorAdapter cardCreatorAdapter;
    private FloatingActionButton newCardFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_creation);

        Intent intent = getIntent();
        this.setTitle(intent.getStringExtra(MainActivity.DECK_NAME));

        noCardsText = (TextView) findViewById(R.id.noCardsText);
        noCardsText.setVisibility(View.GONE);

        cardRecycler = (RecyclerView) findViewById(R.id.cardRecycler);
        cardRecycler.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false));

        // Get deck if it has already been created or
        // maintain the cards if the user has changed the device orientation
        cards = intent.getParcelableArrayListExtra(MainActivity.DECK);
        deckId = intent.getStringExtra(MainActivity.DECK_ID);
        if (savedInstanceState != null) {
            cards = savedInstanceState.getParcelableArrayList(CARDS);
        } else if (cards == null){
            cards = new ArrayList<>();
            noCardsText.setVisibility(View.VISIBLE);
        }
        cardCreatorAdapter = new CardCreatorAdapter(cards);
        cardRecycler.setAdapter(cardCreatorAdapter);

        newCardFab = (FloatingActionButton) findViewById(R.id.newCardFab);
        newCardFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noCardsText.setVisibility(View.GONE);
                Card card = new Card("", "");
                cardCreatorAdapter.showCardDialog(view, card);
                cardRecycler.scrollToPosition(cardCreatorAdapter.getItemCount() - 1);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ArrayList<Card> cards = cardCreatorAdapter.getCards();
        outState.putParcelableArrayList(CARDS, cards);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.deck_creation_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.doneButton) {
            if (cards.size() > 0) {
                Intent resultIntent = new Intent();
                resultIntent.putParcelableArrayListExtra(CARDS, cardCreatorAdapter.getCards());
                resultIntent.putExtra(MainActivity.DECK_NAME,
                        getIntent().getStringExtra(MainActivity.DECK_NAME));
                resultIntent.putExtra(MainActivity.DECK_ID, deckId);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(DeckCreatorActivity.this,
                        "Please enter at least one card.",
                        Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
