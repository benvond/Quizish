package com.benvo.quizish.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.benvo.quizish.R;
import com.benvo.quizish.activities.DeckCreatorActivity;
import com.benvo.quizish.activities.DeckViewerActivity;
import com.benvo.quizish.activities.MainActivity;
import com.benvo.quizish.flashcard.Deck;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static com.benvo.quizish.activities.MainActivity.DECK;

/**
 * Created by benvo on 5/1/2017.
 */

public class DeckRecyclerFragment extends Fragment {

    public static final long VIBRATE_TIME = 70;
    public static final int PINK_RED = 255;
    public static final int PINK_GREEN = 105;
    public static final int PINK_BLUE = 180;
    public static final String USERS = "users";
    public static final String DECKS = "decks";
    public static final String FAVORITES = "favorites";

    private TextView noDecksText;
    private RecyclerView decksRecycler;
    private FirebaseRecyclerAdapter deckAdapter;
    private DatabaseReference rootRef;
    private DatabaseReference deckRef;
    private DatabaseReference userRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.decks_recycler_fragment, container, false);

        noDecksText = (TextView) view.findViewById(R.id.noDecksText);
        decksRecycler = (RecyclerView) view.findViewById(R.id.decksRecycler);

        decksRecycler.setLayoutManager(new LinearLayoutManager(container.getContext(),
                LinearLayoutManager.VERTICAL,
                false));

        rootRef = FirebaseDatabase.getInstance().getReference();
        deckRef = rootRef.child(DECKS);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userRef = rootRef.child(USERS).child(user.getUid());
        }

        // Determines whether the fragment displays all the decks or just the users decks
        boolean isAllDecks = getArguments().getBoolean(MainActivity.ALL_DECKS);
        if (isAllDecks) {
            noDecksText.setText("There are no decks.");
            deckAdapter = new FirebaseRecyclerAdapter<Deck,
                    DeckViewHolder>(Deck.class,
                    R.layout.list_item_deck, DeckViewHolder.class, deckRef) {
                @Override
                protected void populateViewHolder(DeckViewHolder viewHolder,
                                                  Deck model, int position) {
                    viewHolder.bind(model, user);
                }
            };

            deckRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        noDecksText.setVisibility(View.GONE);
                    } else {
                        noDecksText.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            decksRecycler.setAdapter(deckAdapter);
        } else if (user != null) {
            userRef = rootRef.child(USERS).child(user.getUid());
            deckAdapter = new FirebaseRecyclerAdapter<Deck,
                    DeckViewHolder>(Deck.class,
                    R.layout.list_item_deck, DeckViewHolder.class, userRef) {
                @Override
                protected void populateViewHolder(DeckViewHolder viewHolder,
                                                  Deck model, int position) {
                    viewHolder.bind(model, user);
                }
            };

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        noDecksText.setVisibility(View.GONE);
                    } else {
                        noDecksText.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            decksRecycler.setAdapter(deckAdapter);
        } else {
            decksRecycler.setAdapter(null);
            noDecksText.setVisibility(View.VISIBLE);
        }

        return view;
    }


    /**
     * Opens a new activity to view the deck
     * @param deck The deck that the user wants to view
     */
    public void openDeck(View v, Deck deck) {
        Intent intent = new Intent(v.getContext(), DeckViewerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(DECK, deck);
        v.getContext().startActivity(intent);
    }

    /**
     * A ViewHolder for each list item
     */
    public static class DeckViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private TextView deckName;
        private TextView deckSize;
        private TextView creatorText;
        private TextView favoriteText;
        private Button renameButton;
        private Button editButton;
        private Button deleteButton;
        private Button createButton;
        private Button cancelButton;
        private EditText nameEditText;
        private ImageView userImage;
        private ImageButton favoriteButton;


        public DeckViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            deckName = (TextView) itemView.findViewById(R.id.deckName);
            deckSize = (TextView) itemView.findViewById(R.id.deckSize);
            creatorText = (TextView) itemView.findViewById(R.id.creatorText);
            favoriteText = (TextView) itemView.findViewById(R.id.favoriteText);
            userImage = (ImageView) itemView.findViewById(R.id.userImage);
            favoriteButton = (ImageButton) itemView.findViewById(R.id.favoriteButton);
        }

        public void bind(final Deck deck, final FirebaseUser user) {
            deckName.setText(deck.getName());
            favoriteText.setText("0");

            if (deck.getCards() != null && deck.getCards().size() == 1) {
                deckSize.setText("1 Card");
            } else if (deck.getCards() != null){
                deckSize.setText(deck.getCards().size() + " Cards");
            }

            creatorText.setText(deck.getCreator());
            Picasso.with(view.getContext()).load(deck.getCreatorImageURL()).into(userImage);

            if (user == null) {
                favoriteButton.setEnabled(false);
            } else if (deck.getCreator().equals(user.getDisplayName())) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_black_48dp);
                favoriteButton.setEnabled(false);
            }

            final DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference()
                    .child(FAVORITES)
                    .child(deck.getId());
            updateUI(favoritesRef, user, true);

            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateUI(favoritesRef, user, false);
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), DeckViewerActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(DECK, deck);
                    v.getContext().startActivity(intent);
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                                .child(USERS)
                                .child(user.getUid());


                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                GenericTypeIndicator<HashMap<String, Deck>> t =
                                        new GenericTypeIndicator<HashMap<String, Deck>>() {};
                                HashMap<String, Deck> userDecks = dataSnapshot.getValue(t);

                                if (userDecks != null && userDecks.containsKey(deck.getId())) {
                                    Vibrator vibrator =
                                            (Vibrator) view.getContext()
                                                    .getSystemService(Context.VIBRATOR_SERVICE);
                                    vibrator.vibrate(VIBRATE_TIME);
                                    showLongPressedDialog(view, deck, user);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    return false;
                }
            });
        }

        /**
         * Updates the favorite button and text correctly
         * @param favoritesRef The database referencec containing favorites info
         * @param user The user that is attempting to update favorites
         * @param isFirstTime Used to see if updating the button/text on the creation of the
         *                    Viewholder, there are different behaviors acoordingly
         */
        private void updateUI(final DatabaseReference favoritesRef,
                              final FirebaseUser user,
                              final boolean isFirstTime) {
            favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<HashMap<String, String>> t =
                            new GenericTypeIndicator<HashMap<String, String>>() {};
                    HashMap<String, String> deckLikes = dataSnapshot.getValue(t);

                    int numOfLikes;
                    if (deckLikes != null) {
                        numOfLikes = deckLikes.size();
                    } else {
                        numOfLikes = 0;
                    }

                    if (favoriteButton.isEnabled()) {
                        if (deckLikes == null || !deckLikes.containsValue(user.getDisplayName())) {
                            if (isFirstTime) {
                                turnLikeButtonGray();
                            } else {
                                turnLikeButtonPink();
                                favoritesRef.child(user.getUid()).setValue(user.getDisplayName());
                                numOfLikes++;
                            }
                        } else {
                            if (isFirstTime) {
                                turnLikeButtonPink();
                            } else {
                                turnLikeButtonGray();
                                favoritesRef.child(user.getUid()).removeValue();
                                deckLikes.remove(user.getUid());
                                numOfLikes--;
                            }
                        }
                    }

                    favoriteText.setText(Integer.toString(numOfLikes));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        /**
         * Turns favorite button pink
         */
        private void turnLikeButtonPink() {
            favoriteButton.setImageResource(R.drawable.ic_favorite_white_48dp);
            favoriteButton.setColorFilter(Color.rgb(PINK_RED, PINK_GREEN, PINK_BLUE));
        }

        /**
         * Turns favorite button gray with border
         */
        private void turnLikeButtonGray() {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border_black_48dp);
            favoriteButton.setColorFilter(Color.GRAY);
        }

        /**
         * Dialog that appears when user longpresses a deck
         * @param v View for Toast/Snackbar purposes
         * @param deck The deck that is pressed
         * @param user The user pressing the deck
         */
        public void showLongPressedDialog(final View v, final Deck deck, final FirebaseUser user) {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            LayoutInflater inflater =
                    (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View longPressView = inflater.inflate(R.layout.dialog_long_press_deck, null);

            editButton = (Button) longPressView.findViewById(R.id.editDeckButton);
            deleteButton = (Button) longPressView.findViewById(R.id.deleteDeckButton);
            renameButton = (Button) longPressView.findViewById(R.id.renameDeckButton);

            builder.setView(longPressView);
            final AlertDialog dialog = builder.create();
            dialog.show();

            renameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    showDeckRenameDialog(v, deck, user);
                }
            });

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), DeckCreatorActivity.class);
                    intent.putExtra(MainActivity.DECK_NAME, deck.getName());
                    intent.putExtra(MainActivity.DECK_ID, deck.getId());
                    intent.putParcelableArrayListExtra(DECK, deck.getCards());
                    ((Activity) view.getContext()).startActivityForResult(intent,
                            MainActivity.EDIT_DECK_CODE);
                    dialog.dismiss();
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                    rootRef.child(FAVORITES)
                            .child(deck.getId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            GenericTypeIndicator<HashMap<String, String>> t =
                                    new GenericTypeIndicator<HashMap<String, String>>() {};
                            final HashMap<String, String> deckLikes = dataSnapshot.getValue(t);

                            rootRef.child(DECKS).child(deck.getId()).removeValue();
                            rootRef.child(USERS).child(user.getUid()).child(deck.getId())
                                    .removeValue();
                            rootRef.child(FAVORITES).child(deck.getId()).removeValue();
                            dialog.dismiss();

                            Snackbar.make(v,
                                    deck.getName() + " deleted.",
                                    Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    rootRef.child(DECKS).child(deck.getId()).setValue(deck);
                                    rootRef.child(USERS).child(user.getUid()).child(deck.getId())
                                            .setValue(deck);
                                    if (deckLikes != null) {
                                        rootRef.child(FAVORITES)
                                                .child(deck.getId())
                                                .setValue(deckLikes);
                                    }
                                }
                            }).show();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });
        }

        /**
         * Dialog for a user renaming a deck
         * @param v The view for Toast/Snackbar purposes
         * @param deck Deck to rename
         * @param user User doing it
         */
        public void showDeckRenameDialog(final View v, final Deck deck, final FirebaseUser user) {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            LayoutInflater inflater =
                    (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View newDeckView = inflater.inflate(R.layout.dialog_new_deck, null);

            nameEditText = (EditText) newDeckView.findViewById(R.id.nameEditText);
            createButton = (Button) newDeckView.findViewById(R.id.createButton);
            cancelButton = (Button) newDeckView.findViewById(R.id.cancelButton);

            builder.setView(newDeckView);
            final AlertDialog dialog = builder.create();
            dialog.show();

            nameEditText.setText(deck.getName());
            createButton.setText("Rename");

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            createButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String nameInput = nameEditText.getText().toString();
                    if (!nameInput.isEmpty()) {
                        String oldName = deck.getName();
                        deck.setName(nameInput);
                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                        rootRef.child(DECKS).child(deck.getId()).setValue(deck);
                        rootRef.child(USERS).child(user.getUid()).child(deck.getId()).setValue(deck);
                        dialog.dismiss();

                        Snackbar.make(v,
                                "\"" + oldName + "\" renamed to \"" + nameInput + "\"",
                                Snackbar.LENGTH_LONG).setAction("View", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(v.getContext(), DeckViewerActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra(DECK, deck);
                                v.getContext().startActivity(intent);
                            }
                        }).show();
                    } else {
                        Toast.makeText(v.getContext(),
                                "Enter a name for the deck.",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }


    }
}
