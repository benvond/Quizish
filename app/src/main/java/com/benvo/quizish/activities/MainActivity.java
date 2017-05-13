package com.benvo.quizish.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.benvo.quizish.R;
import com.benvo.quizish.adapters.TabPagerAdapter;
import com.benvo.quizish.flashcard.Card;
import com.benvo.quizish.flashcard.Deck;
import com.benvo.quizish.fragments.DeckRecyclerFragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.benvo.quizish.fragments.DeckRecyclerFragment.DECKS;
import static com.benvo.quizish.fragments.DeckRecyclerFragment.USERS;

public class MainActivity extends AppCompatActivity {

    public static final String DECK_NAME = "DECK_NAME";
    public static final String DECK_ID = "DECK_ID";
    public static final String DECK = "DECK";
    public static final String ALL_DECKS = "ALL_DECKS";
    public static final int NEW_DECK_CODE = 0;
    public static final int EDIT_DECK_CODE = 1;
    public static final int RC_SIGN_IN = 2;

    private FloatingActionButton newDeckFab;
    private ViewPager viewPager;
    private TabPagerAdapter tabPagerAdapter;
    private EditText nameEditText;
    private Button createButton;
    private Button cancelButton;
    private DeckRecyclerFragment myDecksFragment;
    private DeckRecyclerFragment allDecksFragment;
    private MenuItem menuItem;

    private GoogleApiClient googleApiClient;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference decksRef;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (savedInstanceState == null) {
            updateUI();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        tabPagerAdapter = new TabPagerAdapter(getFragmentManager());
        Bundle myDecksBundle = new Bundle();
        Bundle allDecksBundle = new Bundle();
        myDecksBundle.putBoolean(ALL_DECKS, false);
        allDecksBundle.putBoolean(ALL_DECKS, true);
        myDecksFragment = new DeckRecyclerFragment();
        allDecksFragment = new DeckRecyclerFragment();
        myDecksFragment.setArguments(myDecksBundle);
        allDecksFragment.setArguments(allDecksBundle);
        tabPagerAdapter.addFragment(myDecksFragment, "My Decks");
        tabPagerAdapter.addFragment(allDecksFragment, "All Decks");

        // Set up the ViewPager with the tab adapter.
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(tabPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        newDeckFab = (FloatingActionButton) findViewById(R.id.newDeckFab);
        newDeckFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(MainActivity.this,
                            "You must be signed in to make a new deck.",
                            Toast.LENGTH_LONG).show();
                    signIn();
                } else {
                    showNewDeckDialog();
                }
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        googleApiClient = new GoogleApiClient.Builder(this)
        .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                Toast.makeText(MainActivity.this,
                        "No internet. Could not connect to Google.",
                        Toast.LENGTH_LONG).show();
            }
        })
        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
        .build();

        decksRef = FirebaseDatabase.getInstance().getReference().child(DECKS);
        usersRef = FirebaseDatabase.getInstance().getReference().child(USERS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menuItem = menu.findItem(R.id.action_sign_in);
        updateMenuItem();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sign_in) {
            if (mAuth.getCurrentUser() == null) {
                signIn();
            } else {
                signOut();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates the text of the menu item to describe correctly the sign in/out action of the button.
     */
    public void updateMenuItem() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            menuItem.setTitle("Sign Out");
        } else {
            menuItem.setTitle("Sign In");
        }
    }

    /**
     * Calls to sign the user in via Google in order to access the Firebase.
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Signs the user out of their Google account and thus doesn't let them write to Firebase
     */
    private void signOut() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        FirebaseAuth.getInstance().signOut();
                        updateMenuItem();
                        updateMyDecksFragment();
                        Toast.makeText(MainActivity.this,
                                "Signed Out.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Used to pass data back from a detail activity into this activity.
     * Code from the onActivityResult implementation from StackOverflow:
     * http://stackoverflow.com/questions/1124548/how-to-pass-the-values-from-one-activity-to-previous-activity
     * @param resultCode The code that gives the information on whether there was a new deck created,
     *                   a deck edited, or the user signed into Google.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            ArrayList<Card> cards =
                    data.getParcelableArrayListExtra(DeckCreatorActivity.CARDS);
            String deckName = data.getStringExtra(DECK_NAME);
            String deckId = data.getStringExtra(DECK_ID);
            final Deck newDeck = new Deck(deckName, cards);

            switch (requestCode) {
                case NEW_DECK_CODE:
                    // Add the new deck to database
                    String key = decksRef.push().getKey();
                    newDeck.setId(key);
                    newDeck.setCreator(user.getDisplayName());
                    newDeck.setCreatorImageURL(user.getPhotoUrl().toString());
                    decksRef.child(key).setValue(newDeck);
                    usersRef.child(user.getUid()).child(key).setValue(newDeck);

                    Snackbar.make(findViewById(R.id.main_content),
                            "Deck Created: " + deckName,
                            Snackbar.LENGTH_LONG).setAction("View", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            myDecksFragment.openDeck(view, newDeck);
                        }
                    }).show();
                    break;
                case EDIT_DECK_CODE:
                    // Update the deck on the database
                    newDeck.setId(deckId);
                    newDeck.setCreator(mAuth.getCurrentUser().getDisplayName());
                    newDeck.setCreatorImageURL(user.getPhotoUrl().toString());
                    decksRef.child(newDeck.getId()).setValue(newDeck);
                    usersRef.child(user.getUid()).child(newDeck.getId()).setValue(newDeck);

                    Snackbar.make(findViewById(R.id.main_content),
                            "Deck Edited: " + deckName,
                            Snackbar.LENGTH_LONG).setAction("View", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            myDecksFragment.openDeck(view, newDeck);
                        }
                    }).show();
                    break;
                case RC_SIGN_IN:
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    if (result.isSuccess()) {
                        // Google Sign In was successful, authenticate with Firebase
                        GoogleSignInAccount account = result.getSignInAccount();
                        firebaseAuthWithGoogle(account);
                    }
            }
        }
    }

    /**
     * Uses a Google account to authenticate the user for Firebase
     * @param acct
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("MainActivity", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("MainActivity", "signInWithCredential:success");
                            user = mAuth.getCurrentUser();
                            updateUI();
                            updateMyDecksFragment();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("MainActivity", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI();
                        }

                        // ...
                    }
                });
    }

    /**
     * Updates the MyDecksFragment to display the current user's decks
     */
    private void updateMyDecksFragment() {
        int position = viewPager.getCurrentItem();
        Bundle myDecksBundle = new Bundle();
        myDecksBundle.putBoolean(ALL_DECKS, false);
        myDecksFragment = new DeckRecyclerFragment();
        tabPagerAdapter.setFragment(0, myDecksFragment, "My Decks");
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.setCurrentItem(position);
    }

    /**
     * Does various UI updates when the user signs in/out
     */
    public void updateUI() {
        if (user != null) {
            if (menuItem != null) {
                updateMenuItem();
            }
            Snackbar.make(findViewById(R.id.main_content),
                    "Signed in as " + user.getDisplayName(),
                    Snackbar.LENGTH_LONG).setAction("Sign Out", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signOut();
                    updateMenuItem();
                }
            }).show();
        }
    }

    /**
     * Creates a new dialog prompting the user for the name of their new deck.
     * Upon completion, a new deck with the given name is added to their local decks.
     */
    private void showNewDeckDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View newDeckView = getLayoutInflater().inflate(R.layout.dialog_new_deck, null);

        nameEditText = (EditText) newDeckView.findViewById(R.id.nameEditText);
        createButton = (Button) newDeckView.findViewById(R.id.createButton);
        cancelButton = (Button) newDeckView.findViewById(R.id.cancelButton);

        builder.setView(newDeckView);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String nameInput = nameEditText.getText().toString();
                if (!nameInput.isEmpty()) {
                    Intent intent = new Intent(view.getContext(),
                            DeckCreatorActivity.class);
                    intent.putExtra(DECK_NAME, nameInput);
                    intent.putParcelableArrayListExtra(DECK, null);
                    startActivityForResult(intent, NEW_DECK_CODE);
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Enter a name for the deck.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}
