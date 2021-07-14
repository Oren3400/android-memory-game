package com.example.orenmemorygame;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Handler uiHandler; // To make the GUI work smooth
    private ImageView[] backCardsArray; // The question marks in this case (ImageViews)
    private List<Integer> frontCardsList; // The car symbols (Drawables)

    private int firstClickedCardIndex; // Hold the index of the first flipped card
    private int SecondClickedCardIndex; // Hold the index of the second flipped card
    private int movesLeft = Constants.MAX_MOVES_ALLOWED; // Initialize with 10 and counting down
    private int countOfMatchPairs = 0; // Need to get 6 match pairs to win
    private int score = Constants.START_SCORE;

    private TextView remainMoves; // To show how much moves remain
    private TextView winOrLose; // Prints message if the player Won or Lost
    private TextView scoreView;

    private boolean firstFromPair = true; // To know to compare this card to the second (when firstFromPair=false)
    private boolean guiBusy = false; // To avoid clicks on another cards while waiting for animation to end
    private final boolean isFlipped = true; // To mark which card is already flipped
    private boolean isFreeze = false; // To make the game freeze when movesLeft is 0

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiHandler = new Handler(); // Main Thread

        backCardsArray = new ImageView[] {
                findViewById(R.id.q1),
                findViewById(R.id.q2),
                findViewById(R.id.q3),
                findViewById(R.id.q4),
                findViewById(R.id.q5),
                findViewById(R.id.q6),
                findViewById(R.id.q7),
                findViewById(R.id.q8),
                findViewById(R.id.q9),
                findViewById(R.id.q10),
                findViewById(R.id.q11),
                findViewById(R.id.q12)
        };

        frontCardsList = Arrays.asList(
                R.drawable.mercedes,
                R.drawable.bmw,
                R.drawable.purshe,
                R.drawable.mercedes,
                R.drawable.bmw,
                R.drawable.purshe,
                R.drawable.bentley,
                R.drawable.lamborghini,
                R.drawable.mazda,
                R.drawable.bentley,
                R.drawable.lamborghini,
                R.drawable.mazda
        );

        winOrLose = findViewById(R.id.winOrLose);
        remainMoves = findViewById(R.id.movesRemain);
        scoreView = findViewById(R.id.scoreView);

        for (ImageView imageView : backCardsArray) { // Init back cards array
            imageView.setImageResource(R.drawable.back);
            imageView.setTag(false);
        }

        Collections.shuffle(frontCardsList); // Shuffling the cards (car symbols)

        winOrLose.setText(Constants.EMPTY); // Clearing the Win\Lose message to the user
        remainMoves.setText(Constants.EMPTY); // Clearing the Moves left message to the user
        scoreView.setText(Constants.EMPTY);
    }

    public void flipCard(View view)
    {
        if (!isFreeze){
            ImageView card = (ImageView) view;
            if (guiBusy) return; // To disable crush when clicking on different card

            try { // Try&Catch to avoid crush
                if (card.getTag().equals(isFlipped)) return; // To disable flip option to flipped cards
            } catch (Exception e){};

            if (firstFromPair) // The first card from pair to compare
            {
                guiBusy = true; // Disable clicks
                card.setTag(isFlipped); // Marks the card

                for (int i = 0; i < backCardsArray.length; i++) // Find index of clicked imageView (question marks)
                {
                    if(card.getId() == backCardsArray[i].getId()) {
                        firstClickedCardIndex = i;}
                }

                card.animate().rotationYBy(Constants.HALF_TURN).setDuration(Constants.HALF_SECOND); // Rotate card (question mark) by 90 degrees

                uiHandler.postDelayed(() -> {
                    card.setImageResource(frontCardsList.get(firstClickedCardIndex)); // Change image on card
                    card.animate().rotationYBy(Constants.HALF_TURN).setDuration(Constants.HALF_SECOND); // Rotate card (car symbol) by 90 degrees to complete 180
                }, Constants.HALF_SECOND); // The first card animation

                firstFromPair = false; // Meaning the second card that comparing to the first one to complete pair
                guiBusy = false; // Enable clicks
            }

            else { // The second card from pair to compare
                guiBusy = true; // Disable clicks

                card.setTag(isFlipped); // Marks the card

                for (int i = 0; i < backCardsArray.length; i++) { // Find index of clicked imageView (question marks)
                    if(card.getId() == backCardsArray[i].getId()) {
                        SecondClickedCardIndex = i;}
                }

                card.animate().rotationYBy(Constants.HALF_TURN).setDuration(Constants.HALF_SECOND);

                uiHandler.postDelayed(() -> {
                    card.setImageResource(frontCardsList.get(SecondClickedCardIndex));
                    card.animate().rotationYBy(Constants.HALF_TURN).setDuration(Constants.HALF_SECOND);

                }, Constants.HALF_SECOND); // The second card animation

                if (!frontCardsList.get(SecondClickedCardIndex).equals(frontCardsList.get(firstClickedCardIndex))) { // Player was wrong
                    backCardsArray[firstClickedCardIndex].setTag(false); // Clears the "flipped" mark
                    backCardsArray[SecondClickedCardIndex].setTag(false); // Clears the "flipped" mark

                    uiHandler.postDelayed(() -> {
                        backCardsArray[firstClickedCardIndex].animate().rotationYBy(Constants.HALF_TURN).setDuration(Constants.HALF_SECOND);
                        backCardsArray[SecondClickedCardIndex].animate().rotationYBy(Constants.HALF_TURN).setDuration(Constants.HALF_SECOND);
                    }, Constants.SECOND); // First animation in "IF" of main thread

                    uiHandler.postDelayed(() -> {
                        backCardsArray[firstClickedCardIndex].animate().rotationYBy(Constants.HALF_TURN).setDuration(Constants.HALF_SECOND);
                        backCardsArray[SecondClickedCardIndex].animate().rotationYBy(Constants.HALF_TURN).setDuration(Constants.HALF_SECOND);
                        backCardsArray[firstClickedCardIndex].setImageResource(R.drawable.back);
                        backCardsArray[SecondClickedCardIndex].setImageResource(R.drawable.back);
                        guiBusy = false; // Enable clicks
                    }, Constants.SECOND_AND_HALF); // Second animation in "IF" of main thread

                    firstFromPair = true; // Next iteration First will choose first from pair and start another comparison
                    score-=100;
                }
                else { // Player was right
                    countOfMatchPairs++;

                    uiHandler.postDelayed(() -> {
                        backCardsArray[firstClickedCardIndex].animate().rotation(Constants.FULL_TURN).setDuration(Constants.HALF_SECOND);
                        backCardsArray[SecondClickedCardIndex].animate().rotation(Constants.FULL_TURN).setDuration(Constants.HALF_SECOND);

                        guiBusy = false; // Enable clicks
                    }, Constants.HALF_SECOND); // First animation in "ELSE" of main thread

                    firstFromPair = true; // Next iteration First will choose first from pair and start another comparison
                    score+=200;
                }
                movesLeft--;
                if (movesLeft == 0) {
                    winOrLose.setText("You Lost !");
                    isFreeze = true;
                }
                if (countOfMatchPairs == Constants.PAIRS_TO_WIN && movesLeft >=0) // Win message only if complete 6 pairs and moves equal or more then 0
                {
                    winOrLose.setText("You Won !");
                }
                remainMoves.setText("Moves left : " + movesLeft);
                scoreView.setText("Score: " + score);
            }
        }
    }

    public void resetGame(View view)
    {
        isFreeze = false;
        countOfMatchPairs = 0;
        movesLeft = Constants.MAX_MOVES_ALLOWED;
        score = Constants.START_SCORE;

        for (ImageView imageView : backCardsArray) {
            imageView.setImageResource(R.drawable.back);
            imageView.setTag(false);
        }

        Collections.shuffle(frontCardsList);

        winOrLose.setText(Constants.EMPTY);
        remainMoves.setText(Constants.EMPTY);
        scoreView.setText(Constants.EMPTY);
    }
}