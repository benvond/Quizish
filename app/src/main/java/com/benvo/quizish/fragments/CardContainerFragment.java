package com.benvo.quizish.fragments;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;

import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.benvo.quizish.R;
import com.benvo.quizish.adapters.CardPagerAdapter;
import com.benvo.quizish.flashcard.Card;

import java.util.Locale;

/**
 * Created by benvo on 4/10/2017.
 */

public class CardContainerFragment extends Fragment {

    public static final String FLIPPED = "FLIPPED";
    private static final float DISTANCE = 17000;

    private CardFragment frontCardFragment;
    private CardFragment backCardFragment;
    private boolean cardFlipped;

    public CardContainerFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Create the card fragment view
        final View rootView = inflater.inflate(
                R.layout.card_fragment, container, false);

        this.cardFlipped = false;
        Bundle bundle = this.getArguments();

        // Make the front and back cards
        Bundle frontBundle = (Bundle) bundle.clone();
        Bundle backBundle = (Bundle) bundle.clone() ;

        frontCardFragment = new CardFragment();
        frontBundle.putByte(FLIPPED, (byte) 0);
        frontCardFragment.setArguments(frontBundle);

        backCardFragment = new CardFragment();
        backBundle.putByte(FLIPPED, (byte) 1);
        backCardFragment.setArguments(backBundle);

        // Set the first viewed fragment to be the the front of the card
        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.container, frontCardFragment)
                .commit();

        // Flip the card once touched
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCard();
            }
        });

        return rootView;
    }

    /**
     * Swaps the visible fragment with the other fragment (the other side of the card)
     * Animates the transition between the change with a flip animation.
     */
    public void flipCard() {
        Fragment fragment;
        if (cardFlipped) {
            fragment = frontCardFragment;
        } else {
            fragment = backCardFragment;
        }


        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out,
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out)
                .replace(R.id.container, fragment)
                .commit();

        cardFlipped = !cardFlipped;
    }

    public static class CardFragment extends Fragment {

        private TextView cardText;
        private ImageButton starButton;
        private ImageButton speakButton;

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            // Create the card fragment view and get the card object
            View view = inflater.inflate(R.layout.card_view, container, false);

            cardText = (TextView) view.findViewById(R.id.cardText);
            starButton = (ImageButton) view.findViewById(R.id.starButton);
            speakButton = (ImageButton) view.findViewById(R.id.speakButton);

            final Bundle bundle = this.getArguments();
            final Card card = bundle.getParcelable(CardPagerAdapter.CARD);

            boolean cardFlipped = bundle.getByte(FLIPPED) != 0;

            // Change camera perspective to not have the flip animation be distorted
            float scale = getResources().getDisplayMetrics().density;
            view.setCameraDistance(DISTANCE * scale);

            // Set the text from the card to the fragment's textview
            if (cardFlipped) {
                cardText.setText(card.getBackText());
            } else {
                cardText.setText(card.getFrontText());
            }

            // Make the star button and set it based on if the card is starred
            if (card.isStarred()) {
                starButton.setImageResource(R.drawable.ic_star_white_48dp);
            } else {
                starButton.setImageResource(R.drawable.ic_star_border_black_48dp);
            }
            starButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!card.isStarred()) {
                        starButton.setImageResource(R.drawable.ic_star_white_48dp);
                    } else {
                        starButton.setImageResource(R.drawable.ic_star_border_black_48dp);
                    }
                    card.toggleStarred();
                    bundle.putParcelable(CardPagerAdapter.CARD, card);
                }
            });

            // Makes the TextToSpeech object and sets its language to the current locale
            final TextToSpeech textToSpeech = new TextToSpeech(view.getContext(),
                    new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                }
            });
            final Locale currentLocale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                currentLocale = getResources().getConfiguration().getLocales().get(0);
            } else {
                //noinspection deprecation
                currentLocale = getResources().getConfiguration().locale;
            }
            textToSpeech.setLanguage(currentLocale);

            speakButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak(cardText.getText().toString(),
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "Speak");
                    } else {
                        textToSpeech.speak(cardText.getText().toString(),
                                TextToSpeech.QUEUE_FLUSH,
                                null);
                    }
                }
            });

            return view;
        }
    }
}
