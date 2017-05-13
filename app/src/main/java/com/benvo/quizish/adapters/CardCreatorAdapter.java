package com.benvo.quizish.adapters;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.benvo.quizish.R;
import com.benvo.quizish.flashcard.Card;

import java.util.ArrayList;

/**
 * Created by benvo on 4/24/2017.
 */

public class CardCreatorAdapter extends RecyclerView.Adapter<CardCreatorAdapter.ViewHolder> {

    private ArrayList<Card> cards;

    private EditText frontEditText;
    private EditText backEditText;
    private Button cancelButton;
    private Button addButton;

    public CardCreatorAdapter(ArrayList<Card> cards) {
        this.cards = cards;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View cardListItem = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_view_new, parent, false);
        return new ViewHolder(cardListItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Card card = cards.get(position);

        holder.frontText.setText(card.getFrontText());
        holder.backText.setText(card.getBackText());

        holder.deleteCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeCard(position);
            }
        });

        holder.editCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCardDialog(view, card);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void addCard(Card card) {
        cards.add(card);
        notifyItemInserted(cards.size()-1);
    }

    public void removeCard(int position) {
        cards.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, cards.size());
    }

    /**
     * Creates a popup dialog for the user to create a new card
     * @param v View for Toast/Snackbar purposes
     * @param card The card that the user is creating
     */
    public void showCardDialog(View v, final Card card) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        // Getting an inflater without being in the activity
        // Code from StackOverflow:
        // http://stackoverflow.com/questions/7803771/call-to-getlayoutinflater-in-places-not-in-activity
        LayoutInflater inflater =
                (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newCardView = inflater.inflate(R.layout.dialog_new_card, null);

        frontEditText = (EditText) newCardView.findViewById(R.id.frontEditText);
        backEditText = (EditText) newCardView.findViewById(R.id.backEditText);
        cancelButton = (Button) newCardView.findViewById(R.id.cancelCardButton);
        addButton = (Button) newCardView.findViewById(R.id.addButton);

        frontEditText.setText(card.getFrontText());
        backEditText.setText(card.getBackText());

        builder.setView(newCardView);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String frontInput = frontEditText.getText().toString();
                String backInput = backEditText.getText().toString();
                if (!frontInput.isEmpty() && !backInput.isEmpty()) {
                    card.setFrontText(frontInput);
                    card.setBackText(backInput);
                    if (!cards.contains(card)) {
                        addCard(card);
                    } else {
                        int index = cards.indexOf(card);
                        cards.set(index, card);
                        notifyItemChanged(index);
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(view.getContext(),
                            "Please enter both a front and back for the card.",
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

    /**
     * A viewholder for the each CardView in the recycler
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView frontText;
        private TextView backText;
        private ImageButton deleteCardButton;
        private ImageButton editCardButton;

        public ViewHolder(View itemView) {
            super(itemView);
            frontText = (TextView) itemView.findViewById(R.id.cardFrontText);
            backText = (TextView) itemView.findViewById(R.id.cardBackText);
            deleteCardButton = (ImageButton) itemView.findViewById(R.id.deleteCardButton);
            editCardButton = (ImageButton) itemView.findViewById(R.id.editCardButton);
        }
    }
}
